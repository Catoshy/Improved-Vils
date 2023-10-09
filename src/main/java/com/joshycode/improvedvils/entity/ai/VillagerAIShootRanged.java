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
import com.joshycode.improvedvils.handler.EventHandlerVil;
import com.joshycode.improvedvils.network.GunFiredPacket;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
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
	private EventHandlerVil.VillagerPredicate<Entity> friendlyFirePredicate;
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

	public VillagerAIShootRanged(EntityVillager shooter, int attackTimeVariance, float attackRange, float speed, EventHandlerVil.VillagerPredicate<Entity> predicate)
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
		if((VilMethods.getCommBlockPos(entityHost) != null) || VilMethods.isOutsideHomeDist(entityHost) || VilMethods.isReturning(entityHost) || VilMethods.isRefillingFood(entityHost))
			return false;
		if(VilMethods.getMovingIndoors(entityHost))
			return false;
		if(entityHost.isMating())
    		return false;
		if(VilMethods.getFollowing(entityHost) && isDistanceTooGreat())
			return false;
		weaponData = villagerGunEntryForItems();
		if(weaponData == null)
			return false;
		if(entityHost.getAttackTarget() == null)
			return false;
		EntityLivingBase entitylivingbase = entityHost.getAttackTarget();

		if(entitylivingbase == null || (CommonProxy.RANGE_BLACKLIST.contains(entitylivingbase.getClass()) && weaponData.meleeInRange)
				|| (entityHost.getDistanceSq(entitylivingbase) < 8 && weaponData.meleeInRange))//TODO list of exempted entities present but not guaranteed to work!
		{
			return false;
		}
		
		attackTarget = entitylivingbase;
		return true;
	}
	
	@Nullable
	public WeaponBrooksData villagerGunEntryForItems()
	{
		WeaponBrooksData d = ConfigHandler.weaponFromItemName(entityHost.getHeldItemMainhand().getUnlocalizedName());
		Collection<RangeAttackEntry> l = ConfigHandler.configuredGuns.get(d);
		if(l != null)
		{
			for(RangeAttackEntry e : l)
			{
				if(InventoryUtil.getItemStacksInInventory(entityHost.getVillagerInventory(), e.getConsumables()) != null)
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
		Map<Item, Integer> inInventory = InventoryUtil.getItemStacksInInventory(entityHost.getVillagerInventory(), entry.getConsumables());
		if(inInventory != null)
		{
			InventoryUtil.consumeItems(entityHost.getVillagerInventory(), inInventory, entry.getConsumables());
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return attackTarget.isEntityAlive() && (shouldExecute() || !entityHost.getNavigator().noPath()) && !VilMethods.outOfAmmo(entityHost);
	}

	@Override
	public void updateTask()
	{
		double d0 = entityHost.getDistanceSq(attackTarget.posX, attackTarget.posY, attackTarget.posZ);
		boolean targetInSight = entityHost.getEntitySenses().canSee(attackTarget);

		if(this.friendlyFireAvoidTicks > 0 && this.randomPos != null)
		{
			entityHost.getNavigator().tryMoveToXYZ(randomPos.x, randomPos.y, randomPos.z, .67D);
			if(friendlyFireAvoidTicks-- == 0)
			{
				this.entityHost.getNavigator().clearPath();
				entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, .67D);
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
			entityHost.getNavigator().clearPath();
		}
		else
		{
			getToTarget();
		}
		entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 55.0F);
		float f;
		if(--rangedAttackTime == 0)
		{
			if(d0 > attackRange_2 || !targetInSight || this.checkForConflict() || this.notLookingAtTarget() || !this.attackTarget.isEntityAlive())//TODO
			{
				rangedAttackTime++;
				return;
			}
			
			f = MathHelper.sqrt(d0) / attackRange;

			float f1 = f;

			if(f < 0.1F)
			{
				f1 = 0.1F;
			}

			if(f1 > 1.0F)
			{
				f1 = 1.0F;
			}

			if(findAndConsumeRangeAttackItems())
			{
				entityHost.resetActiveHand();
				entityHost.setActiveHand(EnumHand.MAIN_HAND);
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
			         attackEntityWithRangedAttack(attackTarget, f);
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
		EnumDifficulty difficulty = entityHost.world.getDifficulty();
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
		EntityBullet bullet = new EntityBullet(entityHost.getEntityWorld(), entityHost, entry, f, "Villager's Shot");
		entityHost.getEntityWorld().spawnEntity(bullet);
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(entityHost.getEntityId()), new TargetPoint(entityHost.dimension, entityHost.posX, entityHost.posY, entityHost.posZ, 124));
		entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F, .05F);
	}

	private void shootShot(float distanceFactor, EntityLivingBase target, float f)
	{
		for(int i = 0; i < weaponData.projectiles; i++)
		{
			EntityBullet bullet = new EntityBullet(entityHost.getEntityWorld(), entityHost, entry, f + 5.0F, "Villager's Shot");
			entityHost.getEntityWorld().spawnEntity(bullet);
		}
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(entityHost.getEntityId()), new TargetPoint(entityHost.dimension, entityHost.posX, entityHost.posY, entityHost.posZ, 124));
		entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 3.0F, 2.0F);
	}

	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityArrow entityarrow = getArrow(distanceFactor);
        if (entityHost.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
        {
            entityarrow = ((net.minecraft.item.ItemBow) entityHost.getHeldItemMainhand().getItem()).customizeArrow(entityarrow); //TODO arrow customization based on inv itemstack???
        }
        double d0 = target.posX - entityHost.posX;
        double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - entityarrow.posY;
        double d2 = target.posZ - entityHost.posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow = vanillaEnchantmentBow(entityHost.getHeldItemMainhand(), entityarrow, getPlayer());
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, entityHost.world.getDifficulty().getDifficultyId());
        entityHost.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (entityHost.getRNG().nextFloat() * 0.4F + 0.8F));
        entityHost.world.spawnEntity(entityarrow);
    }

    private EntityArrow vanillaEnchantmentBow(ItemStack stack, EntityArrow arrow, @Nullable EntityPlayer playerIn)
    {
    	if(EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0)
    	{
	    	 arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
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
	    return arrow;
	}

	private boolean isDistanceTooGreat()
	{
		try
		{
			UUID playerId = VilMethods.getPlayerId(entityHost);
			EntityPlayer player = entityHost.getEntityWorld().getPlayerEntityByUUID(playerId);
			double followRange = entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
			if(player.getDistanceSq(entityHost) > (followRange - 2) * (followRange - 2))
			{
				return true;
			}
		} catch(NullPointerException e) {}
		return false;
	}

    protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(entityHost.world, entityHost);
        entitytippedarrow.setEnchantmentEffectsFromEntity(entityHost, p_190726_1_);
        return entitytippedarrow;
    }

    @Nullable
    private EntityPlayer getPlayer()
    {
    	IImprovedVilCapability cap = entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(cap != null)
		{
			UUID playerId = cap.getPlayerId();
			return entityHost.getWorld().getPlayerEntityByUUID(playerId);
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
		RayTraceResult lineOfSight = com.joshycode.improvedvils.util.ProjectileHelper.checkForFirendlyFire(this.entityHost, this.entityHost.getEntityWorld());
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
    	BlockPos pos = VilMethods.getGuardBlockPos(entityHost);
		if(pos != null)
		{
			if(entityHost.getDistanceSq(pos) < CommonProxy.GUARD_IGNORE_LIMIT)
			{
				Path p = entityHost.getNavigator().getPathToEntityLiving(attackTarget);
				if(p != null) // TODO removed a condition regarding final pathpoint distance to potentially bypass truncate method (made code harder to read)
				{
					truncatePath(p, pos);
				}
				entityHost.getNavigator().setPath(p, .67D);
			}
		}
		else
		{
			entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, .67D);
		}
    }
    
    private void avoidFirendlyFire()
    {
		friendlyFireAvoidTicks = 25;
		entityHost.getNavigator().clearPath();
		boolean randomDirection = entityHost.getEntityWorld().rand.nextBoolean();
		float z = MathHelper.sin((float) Math.toRadians(entityHost.getRotationYawHead()));
		float x = MathHelper.cos((float) Math.toRadians(entityHost.getRotationYawHead()));
		if(randomDirection)
		{
			z = -z;
			x = -x;
		}
		float randomModifier = this.rand.nextInt(7);
		randomPos = new Vec3d(entityHost.posX + (x * randomModifier), entityHost.posY, entityHost.posZ + (z * randomModifier));
		
		if(ConfigHandler.debug)
			Log.info("random position towards; %s", randomPos);
		
		if(randomPos != null && this.rand.nextInt(2) == 0)
		{
			entityHost.getNavigator().tryMoveToXYZ(randomPos.x, randomPos.y, randomPos.z, .67F);
		}
    }

	private void truncatePath(Path p, BlockPos pos)
	{
		for(int i = 0; i < p.getCurrentPathLength(); i++)
		{
			if(p.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) >
					CommonProxy.MAX_GUARD_DIST - 31 /* 2 x 2*/)
			{
				p.setCurrentPathLength(i);
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
		entityHost.getNavigator().clearPath();
	}
}