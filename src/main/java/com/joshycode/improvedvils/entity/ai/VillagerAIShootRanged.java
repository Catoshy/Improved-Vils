package com.joshycode.improvedvils.entity.ai;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.entity.EntityBullet;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.RangeAttackType;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.handler.VillagerPredicate;
import com.joshycode.improvedvils.network.GunFiredPacket;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.ProjectileHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class VillagerAIShootRanged extends EntityAIBase {
	//TODO this class
	
	/** The entity the AI instance has been applied to */
	final EntityVillager entityHost;
	private EntityLivingBase attackTarget;
	private VillagerPredicate<Entity> friendlyFirePredicate;
	@Nullable
	private WeaponBrooksData weaponData;
	private RangeAttackEntry entry;
	private Vec3d randomPos;
	//private boolean isGunEntryApplicable;
	//private boolean isListening;
	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
	 * maxRangedAttackTime.
	 */
	private int rangedAttackTime;
	private int ticksTargetSeen;
	private int attackTimeVariance;
	private float attackRange;
	private float attackRange_2;
	private Random rand;

	//GUN HANDLING:
	private int burstCount; //shots left in current burst.
	private int friendlyFireAvoidTicks;

	public VillagerAIShootRanged(EntityVillager shooter, int attackTimeVariance, float attackRange, float speed, VillagerPredicate<Entity> predicate)
	{
		this.entityHost = shooter;
		this.friendlyFirePredicate = predicate;
		this.rangedAttackTime = -1;
		this.attackTimeVariance = attackTimeVariance;
		this.attackRange = attackRange;
		this.attackRange_2 = attackRange * attackRange;
		this.burstCount = 0;
		this.rand = new Random();
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getCommBlockPos(this.entityHost) != null) || VilMethods.isOutsideHomeDist(this.entityHost) || VilMethods.isReturning(this.entityHost) || VilMethods.isRefillingFood(this.entityHost))
			return false;
		if(VilMethods.getMovingIndoors(this.entityHost))
			return false;
		if(this.entityHost.isMating())
    		return false;
		if(VilMethods.getFollowing(this.entityHost) && isDistanceTooGreat())
			return false;
		weaponData = villagerGunEntryForItems();
		if(weaponData == null)
			return false;
		if(this.entityHost.getAttackTarget() == null)
			return false;
		EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();
		
		if(entitylivingbase == null || (CommonProxy.RANGE_BLACKLIST.contains(entitylivingbase.getClass()) && weaponData.meleeInRange)
				|| (this.entityHost.getDistanceSq(entitylivingbase) < 8 && weaponData.meleeInRange))//TODO list of exempted entities present but not guaranteed to work!
		{
			return false;
		}
		
		attackTarget = entitylivingbase;
		return true;
	}
	
	@Nullable
	public WeaponBrooksData villagerGunEntryForItems()
	{
		WeaponBrooksData d = ConfigHandler.weaponFromItemName(this.entityHost.getHeldItemMainhand().getUnlocalizedName());
		Collection<RangeAttackEntry> l = ConfigHandler.configuredGuns.get(d);
		if(l != null)
		{
			for(RangeAttackEntry e : l)
			{
				if(InventoryUtil.getItemStacksInInventory(this.entityHost.getVillagerInventory(), e.getConsumables()) != null)
				{
					entry = e;
					VilMethods.setOutOfAmmo(this.entityHost, false);
					return d;
				}
			}
		}
		VilMethods.setOutOfAmmo(this.entityHost, true);
		return null;
	}

	/**
	 * Finds the necessary items in the villagers inventory if they are sufficient.
	 * If insufficient items for the instance's saved GunEntry reference, no items are
	 * consumed
	 * @return whether lookup was successful
	 */
	public boolean findAndConsumeRangeAttackItems()
	{
		Map<Item, Integer> inInventory = InventoryUtil.getItemStacksInInventory(this.entityHost.getVillagerInventory(), entry.getConsumables());
		if(inInventory != null)
		{
			InventoryUtil.consumeItems(this.entityHost.getVillagerInventory(), inInventory, entry.getConsumables());
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return attackTarget.isEntityAlive() && (shouldExecute() || !this.entityHost.getNavigator().noPath()) && !VilMethods.outOfAmmo(this.entityHost);
	}

	@Override
	public void updateTask()
	{
		double d0 = this.entityHost.getDistanceSq(attackTarget.posX, attackTarget.posY, attackTarget.posZ);
		boolean targetInSight = this.entityHost.getEntitySenses().canSee(attackTarget);

		if(this.friendlyFireAvoidTicks > 0 && this.randomPos != null)
		{
			this.entityHost.getNavigator().tryMoveToXYZ(randomPos.x, randomPos.y, randomPos.z, .67D);
			if(friendlyFireAvoidTicks-- == 0)
			{
				this.entityHost.getNavigator().clearPath();
				this.entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, .67D);
				this.rangedAttackTime = 10;
			}
			return;
		}
		else
		{
			friendlyFireAvoidTicks = 0;
		}
		
		if(targetInSight)
		{
			++ticksTargetSeen;
		}
		else
		{
			ticksTargetSeen = 0;
		}
		
		if(d0 <= attackRange_2 && ticksTargetSeen >= 10) //When in range of attack, don't get needlessly closer
		{
			this.entityHost.getNavigator().clearPath();
		}
		else
		{
			getToTarget();
		}
		this.entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 55.0F);
		float f;
		if(--rangedAttackTime == 0)
		{
			if(d0 > attackRange_2 || !targetInSight || this.checkForConflict() || this.notLookingAtTarget() || !this.attackTarget.isEntityAlive())//TODO
			{
				rangedAttackTime++;
				return;
			}
			
			f = MathHelper.sqrt(d0) / attackRange;
			float f1 = MathHelper.clamp(f, .1F, 1F);
			
			if(findAndConsumeRangeAttackItems())
			{
				this.entityHost.resetActiveHand();
				this.entityHost.setActiveHand(EnumHand.MAIN_HAND);
				if(entry.type != RangeAttackType.BOW)
				{
					attackEntityWithRangedAttackGun(attackTarget, f1);

					if(weaponData.shotsForBurst > 1 && burstCount-- > 0)
					{
						rangedAttackTime = weaponData.burstCoolDown;
					}
					else
					{
						burstCount = weaponData.shotsForBurst;
						rangedAttackTime = MathHelper.floor(f * (weaponData.coolDown - attackTimeVariance) + attackTimeVariance);
					}
				}
				else
				{
			         attackEntityWithRangedAttack(attackTarget, f * 2);
					 rangedAttackTime = MathHelper.floor(f * (weaponData.coolDown - attackTimeVariance) + attackTimeVariance);
				}
			}
		}
		else if(rangedAttackTime < 0)
		{
			f = MathHelper.sqrt(d0) / attackRange;
			rangedAttackTime = MathHelper.floor(f * (weaponData.coolDown - attackTimeVariance) + attackTimeVariance);
		}
	}

	private void attackEntityWithRangedAttackGun(EntityLivingBase target, float distanceFactor)
	{
		EnumDifficulty difficulty = this.entityHost.world.getDifficulty();
		float acc = 1.0f;
		switch(difficulty)
		{
			case EASY:
				acc = 1.0f;
				break;
			case NORMAL:
				acc = 1.15f;
				break;
			case HARD:
				acc = 1.3f;
				break;
			default:
				break;
		}
		if(entry.type == RangeAttackEntry.RangeAttackType.SHOT)
		{
			shootShot(distanceFactor, target, acc);
		}
		else
		{
			shootGun(distanceFactor, target, acc);
		}
	}

    private void shootGun(float distanceFactor, EntityLivingBase target, float f)
    {
		EntityBullet bullet = new EntityBullet(this.entityHost.getEntityWorld(), this.entityHost, entry, f, "Villager's Shot");
		this.entityHost.getEntityWorld().spawnEntity(bullet);
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(this.entityHost.getEntityId()), new TargetPoint(this.entityHost.dimension, this.entityHost.posX, this.entityHost.posY, this.entityHost.posZ, 124));
		this.entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F, .05F);
	}

	private void shootShot(float distanceFactor, EntityLivingBase target, float f)
	{
		for(int i = 0; i < weaponData.projectiles; i++)
		{
			EntityBullet bullet = new EntityBullet(this.entityHost.getEntityWorld(), this.entityHost, entry, f + 5.0F, "Villager's Shot");
			this.entityHost.getEntityWorld().spawnEntity(bullet);
		}
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(this.entityHost.getEntityId()), new TargetPoint(this.entityHost.dimension, this.entityHost.posX, this.entityHost.posY, this.entityHost.posZ, 124));
		this.entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 3.0F, 2.0F);
	}

	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityArrow entityarrow = getArrow(distanceFactor);
        if (this.entityHost.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
        {
            entityarrow = ((net.minecraft.item.ItemBow) this.entityHost.getHeldItemMainhand().getItem()).customizeArrow(entityarrow); //TODO arrow customization based on inv itemstack???
        }
        double d0 = target.posX - this.entityHost.posX;
        double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - entityarrow.posY;
        double d2 = target.posZ - this.entityHost.posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow = vanillaEnchantmentBow(this.entityHost.getHeldItemMainhand(), entityarrow, getPlayer());
        entityarrow.shoot(d0, d1 + d3 * (0.20000000298023224D * .5), d2, 2.8F, (float)(14 - (4 - this.entityHost.getEntityWorld().getDifficulty().getDifficultyId()) * 4)); //TODO
        this.entityHost.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.entityHost.getRNG().nextFloat() * 0.4F + 0.8F));
        this.entityHost.world.spawnEntity(entityarrow);
    }

    private EntityArrow vanillaEnchantmentBow(ItemStack stack, EntityArrow arrow, @Nullable EntityPlayer playerIn)
    {
    	boolean crit = true;
    	if(EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0)
    	{
	    	 arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
	    	 crit = false;
	     }
    	else
    	{
	 	     arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
	    }
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

        if (j > 0)
        {
        	arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.5D);
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
	    arrow.setIsCritical(crit);
	    return arrow;
	}

	private boolean isDistanceTooGreat()
	{
		UUID playerId = VilMethods.getPlayerId(this.entityHost);
		EntityPlayer player = this.entityHost.getEntityWorld().getPlayerEntityByUUID(playerId);
		double followRange = this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
		if(player.getDistanceSq(this.entityHost) > (followRange - 2) * (followRange - 2))
		{
			return true;
		}
		return false;
	}

    protected EntityArrow getArrow(float damage)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.entityHost.world, this.entityHost);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this.entityHost, damage);
        return entitytippedarrow;
    }

    @Nullable
    private EntityPlayer getPlayer()
    {
    	IImprovedVilCapability cap = this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(cap != null)
		{
			UUID playerId = cap.getPlayerId();
			return this.entityHost.getWorld().getPlayerEntityByUUID(playerId);
		}
		return null;
    }
    
	private boolean notLookingAtTarget() 
	{
		double diffX = this.attackTarget.posX - this.entityHost.posX;
		double diffZ = this.attackTarget.posZ - this.entityHost.posZ;
        float attackerYaw = (float)(MathHelper.atan2(diffZ, diffX) * (180D / Math.PI)) - 90.0F;
        float wrapDifference = MathHelper.wrapDegrees(this.entityHost.getRotationYawHead() - attackerYaw);
        if(MathHelper.abs(wrapDifference) > 15F)
        {
        	return true;
        }
		return false;
	}

	private boolean checkForConflict() 
	{
		RayTraceResult lineOfSight = ProjectileHelper.checkForFirendlyFire(this.entityHost, this.entityHost.getEntityWorld());
		if(ConfigHandler.debug && lineOfSight != null && lineOfSight.entityHit != null)
			Log.info("Looking for firendly fire?! here's what we got... %s", lineOfSight.entityHit);
		if(lineOfSight != null && lineOfSight.typeOfHit == RayTraceResult.Type.ENTITY && this.friendlyFirePredicate.apply(lineOfSight.entityHit))
		{
			if(ConfigHandler.debug)
				Log.info("Firendly Fire Detected! %s", lineOfSight.entityHit);
			this.avoidFirendlyFire();
			return true;
		}
		
		List<EntityVillager> list = this.entityHost.getEntityWorld().getEntitiesWithinAABB(EntityVillager.class, this.attackTarget.getEntityBoundingBox().grow(2D), this.friendlyFirePredicate);
		if(!list.isEmpty())
		{
			if(ConfigHandler.debug)
				Log.info("Friendlies too close to fire! %s", list.get(0));
			this.rangedAttackTime++;
			return true;
		}
		return false;
	}

    
    private void getToTarget()
    {
    	BlockPos pos = VilMethods.getGuardBlockPos(this.entityHost);
		if(pos != null)
		{
			Path p = this.entityHost.getNavigator().getPathToEntityLiving(attackTarget);
			if(p != null) // TODO removed a condition regarding final pathpoint distance to potentially bypass truncate method (made code harder to read)
			{
				truncatePath(p, pos, CommonProxy.MAX_GUARD_DIST - 31);
			}
			this.entityHost.getNavigator().setPath(p, .67D);
			return;
		} 
		else if(VilMethods.getFollowing(this.entityHost))
		{
			UUID playerId = VilMethods.getPlayerId(this.entityHost);
			EntityPlayer player = this.entityHost.getEntityWorld().getPlayerEntityByUUID(playerId);
			if(player != null)
			{
				pos = player.getPosition();
				double followRange = this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
				if(this.entityHost.getDistance(player) > followRange)
				{
					Path p = this.entityHost.getNavigator().getPathToEntityLiving(attackTarget);
					if(p != null)
					{
						truncatePath(p, pos, (float) (followRange * followRange / 2 /* use half regular follow range */));
					}
					this.entityHost.getNavigator().setPath(p, .67D);
				}
				return;
			}
		}
		this.entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, .67D);
    }
    
    private void avoidFirendlyFire()
    {
		friendlyFireAvoidTicks = 25;
		this.entityHost.getNavigator().clearPath();
		boolean randomDirection = this.entityHost.getEntityWorld().rand.nextBoolean();
		float z = MathHelper.sin((float) Math.toRadians(this.entityHost.getRotationYawHead()));
		float x = MathHelper.cos((float) Math.toRadians(this.entityHost.getRotationYawHead()));
		if(randomDirection)
		{
			z = -z;
			x = -x;
		}
		float randomModifier = this.rand.nextInt(7);
		randomPos = new Vec3d(this.entityHost.posX + (x * randomModifier), this.entityHost.posY, this.entityHost.posZ + (z * randomModifier));
		
		if(ConfigHandler.debug)
			Log.info("random position towards; %s", randomPos);
		
		if(randomPos != null && this.rand.nextInt(2) == 0)
		{
			this.entityHost.getNavigator().tryMoveToXYZ(randomPos.x, randomPos.y, randomPos.z, .67F);
		}
    }

	private void truncatePath(Path p, BlockPos pos, float maxDistSq)
	{
		for(int i = 0; i < p.getCurrentPathLength(); i++)
		{
			if(p.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) >
					maxDistSq)
			{
				p.setCurrentPathLength(i);
			}
			if(i < p.getCurrentPathLength() -1 && p.getPathPointFromIndex(i).y - p.getPathPointFromIndex(i + 1).y >= 2) //go not where you cannot return
			{
				p.setCurrentPathLength(i);
				return;
			}
		}
	}
	
	@Override
	public void resetTask()
	{
		attackTarget = null;
		ticksTargetSeen = 0;
		if(this.entry.type != RangeAttackType.SINGLESHOT && this.entry.type != RangeAttackType.SHOT)
			rangedAttackTime = -1;
		this.entityHost.getNavigator().clearPath();
	}
}