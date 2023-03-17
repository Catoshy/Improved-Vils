package com.joshycode.improvedmobs.entity.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry.RangeAttackType;
import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import techguns.TGuns;

public class VillagerAIShootRanged extends EntityAIBase implements IInventoryChangedListener{

	/** The entity the AI instance has been applied to */
	private final EntityVillager entityHost;
	private EntityLivingBase attackTarget;
	private RangeAttackEntry entry;
	private Multimap<String, Integer> invCache;
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
		this.invCache = ArrayListMultimap.create();
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
		if(this.isDirty())	
			this.entry = villagerGunEntryForItems();
		if(this.isDirty() || this.entityHost.getAttackTarget() == null)
			return false;
		EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();

		if(entitylivingbase == null){
			return false;
		}else{
			this.attackTarget = entitylivingbase;
			return true;
		}
	}
	
	public RangeAttackEntry villagerGunEntryForItems() {
		Collection<RangeAttackEntry> l = ConfigHandlerVil.configuredGuns.get(this.entityHost.getHeldItemMainhand().getUnlocalizedName());
		for(RangeAttackEntry e : l) {
			if(areTheseItemsInHostInventory(e.getConsumables()) != null) {
				this.setDirty(false);
				return e;
			}
		}
		this.entry = null;
		return null;
	}
	
	private Map<String, Integer> areTheseItemsInHostInventory(Map<String, Integer> items) {
		IInventory hostInv = this.entityHost.getVillagerInventory();
		Map<String, Integer> consInVilInv = new HashMap();
		Map<String, Integer> toBeConsumed = new HashMap();	
		for(int i = 0; i < hostInv.getSizeInventory(); i++) {
			ItemStack stack = hostInv.getStackInSlot(i);
			String name = stack.getItem().getUnlocalizedName();
			if(items.keySet().contains(name)) {
				this.invCache.put(name, i);
				int val = 0;
				if(consInVilInv.get(name) != null)	
					val = consInVilInv.get(name);
				consInVilInv.put(name, val + stack.getCount());
				toBeConsumed.put(hostInv.getStackInSlot(i).getUnlocalizedName(), i);			
				}
		}
		for(String s : items.keySet()) {
			if(consInVilInv.get(s) != null) {
				if(consInVilInv.get(s) < items.get(s))
					return null;
			} else {
				return null;
			}
		}
		return toBeConsumed;
	}
	
	private Map<String, Integer> getTheseItemsCached(Map<String, Integer> items) {
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
	}
	
	private void consumeItems(Map<String, Integer> consumables, Map<String, Integer> howMuchOfEach) {
		IInventory hostInv = this.entityHost.getVillagerInventory();
		
		for(String itemToCons : consumables.keySet()) {
			ItemStack stack = hostInv.getStackInSlot(consumables.get(itemToCons));
			String name = stack.getUnlocalizedName();
			int amt = howMuchOfEach.get(name);
			
			if(amt > stack.getCount()) {
				amt -= stack.getCount();
				hostInv.setInventorySlotContents(consumables.get(itemToCons), ItemStack.EMPTY);
				howMuchOfEach.put(name, amt);
			} else {
				hostInv.decrStackSize(consumables.get(itemToCons), amt);
			}
		}
	}
	
	/**
	 * Finds the necessary items in the villagers inventory if they are sufficient.
	 * If insufficient items for the instance's saved GunEntry reference, no items are
	 * consumed
	 * @return whether lookup was successful
	 */
	public boolean findAndConsumeRangeAttackItems() {
		if(!this.isDirty()) {
			Map<String, Integer> cached = getTheseItemsCached(this.entry.getConsumables());
			if(cached != null) {
				this.consumeItems(cached, this.entry.getConsumables());
				return true;
			} else {
				Map<String, Integer> inInventory = areTheseItemsInHostInventory(this.entry.getConsumables());
				if(inInventory != null) {
					this.consumeItems(inInventory, this.entry.getConsumables());
					return true;
				}
			}
		}
		this.setDirty(true);
		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.attackTarget.isEntityAlive() && (this.shouldExecute() || !this.entityHost.getNavigator().noPath());
	}

	@Override
	public void resetTask() {
		this.attackTarget = null;
		this.ticksTargetSeen = 0;
		this.rangedAttackTime = -1;
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
			this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.speed);
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

		TGuns.boltaction.fireWeaponFromNPC(this.entityHost, this.entry.damage, acc);
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
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)this.entityHost.world.getDifficulty().getDifficultyId());
        this.entityHost.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.entityHost.getRNG().nextFloat() * 0.4F + 0.8F));
        this.entityHost.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.entityHost.world, this.entityHost);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this.entityHost, p_190726_1_);
        return entitytippedarrow;
    }
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
	
	private boolean isDirty() {return !this.isGunEntryApplicable; }
	
	private void setDirty(boolean isBad) {this.isGunEntryApplicable = !isBad; }
	
	public boolean isListening() {return this.isListening; }

	public void setListening() {this.isListening = true; }

	@Override
	public void onInventoryChanged(IInventory invBasic) {
		this.setDirty(true);
		this.invCache.clear();
	}
}
