package com.joshycode.improvedmobs.entity.ai;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.VilCapabilityMethods;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry.RangeAttackType;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;

public class VillagerAIShootRanged extends EntityAIBase {

	/** The entity the AI instance has been applied to */
	final EntityVillager entityHost;
	private EntityLivingBase attackTarget;
	@Nullable
	private RangeAttackEntry entry;
	private boolean isGunEntryApplicable;
	private boolean isListening;

	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
	 * maxRangedAttackTime.
	 */
	private int rangedAttackTime;
	private int ticksTargetSeen;
	private int attackTimeVariance;

	private float attackRange;
	private float attackRange_2;

	//GUN HANDLING:
	private int burstCount; //shots left in current burst.
	private float speed;

	public VillagerAIShootRanged(EntityVillager shooter, int attackTimeVariance, float attackRange, float speed) {
		this.rangedAttackTime = -1;
		this.entityHost = shooter;
		this.attackTimeVariance = attackTimeVariance;
		this.attackRange = attackRange;
		this.attackRange_2 = attackRange * attackRange;
		this.setMutexBits(3);

		this.burstCount = 0;
		this.isGunEntryApplicable = false;
		this.isListening = false;
		this.speed = speed;
	}

	@Override
	public boolean shouldExecute() {
		if(VilCapabilityMethods.getCommBlockPos(this.entityHost) != null)
			return false;
		if(VilCapabilityMethods.isOutsideHomeDist(this.entityHost))
			return false;
		if(VilCapabilityMethods.isReturning(this.entityHost))
			return false;
		if(VilCapabilityMethods.getMovingIndoors(this.entityHost))
			return false;
		if(this.entityHost.isMating())
    		return false;
		if(VilCapabilityMethods.getFollowing(this.entityHost) && isDistanceTooGreat())
			return false;
		this.entry = villagerGunEntryForItems();
		if(this.entry == null)
			return false;
		if(this.entityHost.getAttackTarget() == null)
			return false;
		EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();

		if(entitylivingbase == null){
			return false;
		}else{
			this.attackTarget = entitylivingbase;
			return true;
		}
	}

	@Nullable
	public RangeAttackEntry villagerGunEntryForItems() {
		Collection<RangeAttackEntry> l = ConfigHandlerVil.configuredGuns.get(this.entityHost.getHeldItemMainhand().getUnlocalizedName());
		for(RangeAttackEntry e : l) {
			if(InventoryUtil.getItemStacksInInventory(this.entityHost.getVillagerInventory(), e.getConsumables()) != null) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * Finds the necessary items in the villagers inventory if they are sufficient.
	 * If insufficient items for the instance's saved GunEntry reference, no items are
	 * consumed
	 * @return whether lookup was successful
	 */
	public boolean findAndConsumeRangeAttackItems() {
		Map<String, Integer> inInventory = InventoryUtil.getItemStacksInInventory(this.entityHost.getVillagerInventory(), this.entry.getConsumables());
		if(inInventory != null) {
			InventoryUtil.consumeItems(this.entityHost.getVillagerInventory(), inInventory, this.entry.getConsumables());
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.attackTarget.isEntityAlive() && (this.shouldExecute() || !this.entityHost.getNavigator().noPath());
	}

	@Override
	public void resetTask() {
		System.out.println("resetting vilAishoot");
		this.attackTarget = null;
		this.ticksTargetSeen = 0;
		this.rangedAttackTime = -1;
		this.entityHost.getNavigator().clearPath();
	}

	@Override
	public void updateTask() {
		double d0 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.posY/*this.attackTarget.boundingBox.minY TODO??*/, this.attackTarget.posZ);
		boolean targetInSight = this.entityHost.getEntitySenses().canSee(this.attackTarget);

		if(targetInSight){
			++this.ticksTargetSeen;
		}else{
			this.ticksTargetSeen = 0;
		}

		if(d0 <= (double) this.attackRange_2 && this.ticksTargetSeen >= 10){
			this.entityHost.getNavigator().clearPath();
		}else{
			BlockPos pos = VilCapabilityMethods.getGuardBlockPos(this.entityHost);
			if(pos != null) {
				if(this.entityHost.getDistanceSq(pos) < CommonProxy.GUARD_IGNORE_LIMIT) {
					Path p = this.entityHost.getNavigator().getPathToEntityLiving(attackTarget);
					if(p != null && p.getFinalPathPoint().distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ()))
							>= CommonProxy.MAX_GUARD_DIST - 31) {
						this.truncatePath(p, pos);
					}
					this.entityHost.getNavigator().setPath(p, .67D);
				}
			} else {
				this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, .67D);
			}
		}

		this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 55.0F);
		float f;

		if(--this.rangedAttackTime == 0){
			if(d0 > (double) this.attackRange_2 || !targetInSight){
				return;
			}

			f = MathHelper.sqrt(d0) / this.attackRange;

			float f1 = f;

			if(f < 0.1F){
				f1 = 0.1F;
			}

			if(f1 > 1.0F){
				f1 = 1.0F;
			}

			if(this.findAndConsumeRangeAttackItems()) {
				if(this.entry.type != RangeAttackType.BOW) {
					System.out.println("shoot! or try to at least...");
					this.attackEntityWithRangedAttackGun(this.attackTarget, f1);
	
					if(this.entry.shotsForBurst > 0)
						this.burstCount--;
					if(this.burstCount > 0){
						this.rangedAttackTime = this.entry.burstCoolDown;
					}else{
						this.burstCount = this.entry.shotsForBurst;
						this.rangedAttackTime = MathHelper.floor(f * (float) (this.entry.coolDown - this.attackTimeVariance) + (float) this.attackTimeVariance);
					}
				} else {
			         attackEntityWithRangedAttack(this.attackTarget, f);
					 this.rangedAttackTime = MathHelper.floor(f * (float) (this.entry.coolDown - this.attackTimeVariance) + (float) this.attackTimeVariance);
				}
			}
		}else if(this.rangedAttackTime < 0){
			f = MathHelper.sqrt(d0) / this.attackRange;
			this.rangedAttackTime = MathHelper.floor(f * (float) (this.entry.coolDown - this.attackTimeVariance) + (float) this.attackTimeVariance);
		}
	}

	private void attackEntityWithRangedAttackGun(EntityLivingBase target, float distanceFactor) {
	
		EnumDifficulty difficulty = this.entityHost.world.getDifficulty();
		float acc = 1.0f;
		//float dmg = 1.0f;
		switch(difficulty){
			case EASY:
				acc = 1.0f;
				//dmg = 0.6f;
				break;
			case NORMAL:
				acc = 1.15f;
				//dmg = 0.8f;
				break;
			case HARD:
				acc = 1.3f;
				//dmg = 1.0f;
				break;
			default:
				break;
		}
		if(this.entry.type != RangeAttackEntry.RangeAttackType.SHOT) {
			//TGuns.boltaction.fireWeaponFromNPC(this.entityHost, this.entry.damage, acc);
		} else {
			for(int i = 0; i < this.entry.projectiles; i++) {
				//TGuns.pistol.fireWeaponFromNPC(this.entityHost, this.entry.damage, acc * .3f);
			}
		}
	}

    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityArrow entityarrow = this.getArrow(distanceFactor);
        if (this.entityHost.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
            entityarrow = ((net.minecraft.item.ItemBow) this.entityHost.getHeldItemMainhand().getItem()).customizeArrow(entityarrow); //TODO arrow customization based on inv itemstack???
        double d0 = target.posX - this.entityHost.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.entityHost.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow = vanillaEnchantmentBow(this.entityHost.getHeldItemMainhand(), entityarrow, getPlayer());
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)this.entityHost.world.getDifficulty().getDifficultyId());
        this.entityHost.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.entityHost.getRNG().nextFloat() * 0.4F + 0.8F));
        this.entityHost.world.spawnEntity(entityarrow);
    }
    
    private EntityArrow vanillaEnchantmentBow(ItemStack stack, EntityArrow arrow, @Nullable EntityPlayer playerIn) {
    	if(EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0) {
	    	 arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
	     } else {
	 	     arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
	     }
         int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

         if (j > 0)
         {
             arrow.setDamage(arrow.getDamage() + (double)j * 0.5D + 0.5D);
         }

         int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

         if (k > 0)
         {
             arrow.setKnockbackStrength(k);
         }

         if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
         {
             arrow.setFire(100);
         }
         
         if(playerIn != null) 
         {
        	 stack.damageItem(1, playerIn);
         }
         return arrow;
	}
    
	private boolean isDistanceTooGreat() {
		try {	
			UUID playerId = VilCapabilityMethods.getPlayerId((EntityVillager) this.entityHost);
			EntityPlayer player = this.entityHost.getEntityWorld().getPlayerEntityByUUID(playerId);
			double followRange = this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
			if(player.getDistanceSq(this.entityHost) > (followRange - 2) * (followRange - 2)) {
				return true;
			}
		} catch(NullPointerException e) {}
		return false;
	}

    protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.entityHost.world, this.entityHost);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this.entityHost, p_190726_1_);
        return entitytippedarrow;
    }
    
    @Nullable
    private EntityPlayer getPlayer() {
    	IImprovedVilCapability cap = this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(cap != null) {
			UUID playerId = cap.getPlayerId();
			return this.entityHost.getWorld().getPlayerEntityByUUID(playerId);
		}
		return null;
    }
    
	private void truncatePath(Path p, BlockPos pos) {
		for(int i = 0; i < p.getCurrentPathLength(); i++) {
			if(p.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) > 
					CommonProxy.MAX_GUARD_DIST - 31 /* 2 x 2*/) {
				p.setCurrentPathLength(i);
			}
		}
	}
}

/*private Map<String, Integer> getTheseItemsCached(Map<String, Integer> items) {
IInventory hostInv = this.entityHost.getVillagerInventory();
Map<String, Integer> consInVilInv = new HashMap();
Map<String, Integer> toBeConsumed = new HashMap();

for(String itemName : this.invCache.keySet()) {
	for(int i : this.invCache.get(itemName)) {
		int number = hostInv.getStackInSlot(i).getCount();
		int val = 0;
		if(consInVilInv.get(itemName) != null)	
			val = consInVilInv.get(itemName);
		consInVilInv.put(itemName, val+number);
		toBeConsumed.put(itemName, i);
	}
}
for(String s : items.keySet()) {
	if(consInVilInv.get(s) != null)
		if(consInVilInv.get(s) < items.get(s))
			return null;
}
return toBeConsumed;
}*/

/*
private static Field AI_attackTime;
private static Field AI_burstCount;
private static Field AI_burstAttackTime;

@Nullable
public static void applyAI(EntityLiving e) {
	if(!ConfigHandler.useTGunsMod)
		return;
	if(AI_attackTime == null)
		AI_attackTime = ReflectionUtils.getField(GenericGun.class, "AI_attackTime");
	if(AI_burstCount == null)
		AI_burstCount = ReflectionUtils.getField(GenericGun.class, "AI_burstCount");
	if(AI_burstAttackTime == null)
		AI_burstAttackTime = ReflectionUtils.getField(GenericGun.class, "AI_burstAttackTime");
	Item item = e.getHeldItemMainhand().getItem();
	if(item instanceof GenericGun){
		GenericGun gun = ((GenericGun) item);
		int time = ReflectionUtils.getFieldValue(AI_attackTime, gun);
		e.tasks.addTask(0, new VillagerAIFireGuns(e, time / 3, time, gun.getAI_attackRange(), ReflectionUtils.getFieldValue(AI_burstCount, gun), ReflectionUtils.getFieldValue(AI_burstAttackTime, gun)));
	}
}
*/


