package com.joshycode.improvedvils.entity.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
import com.joshycode.improvedvils.util.Pair;
import com.joshycode.improvedvils.util.ProjectileHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class VillagerAIShootRanged extends EntityAIBase {
	
	/** The entity the AI instance has been applied to */
	final EntityVillager entityHost;
	private EntityLivingBase attackTarget;
	private VillagerPredicate<Entity> friendlyFirePredicate;
	@Nullable
	private WeaponBrooksData weaponData;
	private RangeAttackEntry entry;
	private Vec3i randomPos;
	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
	 * maxRangedAttackTime.
	 */
	private int rangedAttackTime;
	private int attackTimeVariance;
	private final float attackRangeBow;
	private float attackRange;
	private float attackRange_2;

	//GUN HANDLING:
	private int burstCount; //shots left in current burst.
	private int friendlyFireAvoidTicks;
	
	//Friendly-fire debug TODO
	private String debugString = "";

	public VillagerAIShootRanged(EntityVillager shooter, int attackTimeVariance, float attackRangeBow, float speed, VillagerPredicate<Entity> predicate)
	{
		this.entityHost = shooter;
		this.friendlyFirePredicate = predicate;
		this.rangedAttackTime = -1;
		this.attackTimeVariance = attackTimeVariance;
		this.attackRangeBow = attackRangeBow;
		this.burstCount = 0;
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getCommBlockPos(this.entityHost) != null
				|| VilMethods.isOutsideHomeDist(this.entityHost) 
				|| VilMethods.isReturning(this.entityHost) 
				|| VilMethods.isRefillingFood(this.entityHost)
				|| VilMethods.getMovingIndoors(this.entityHost)
				|| this.entityHost.isMating()
				|| (VilMethods.getFollowing(this.entityHost) && isDistanceTooGreat()))
		{
			return false;
		}
		
		this.weaponData = villagerGunEntryForItems();
		if(this.weaponData == null || this.entityHost.getAttackTarget() == null)
			return false;
	
		EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();
		if(checkIfMelee(entitylivingbase))
			return false;
		
		this.setAttackRange();
		this.attackTarget = entitylivingbase;
		return true;
	}

	private boolean checkIfMelee(EntityLivingBase entitylivingbase) 
	{
		boolean hasBlacklist = CommonProxy.RANGE_BLACKLIST.contains(entitylivingbase.getClass()) 
												&& this.weaponData.meleeInRange;
		boolean isCloseEnough = this.entityHost.getDistanceSq(entitylivingbase) < 16 
												&& this.weaponData.meleeInRange
												&& this.rangedAttackTime > 20;
		return hasBlacklist || isCloseEnough;
	}
	
	private void setAttackRange() 
	{
		if(this.entry.type == RangeAttackType.BOW)
			this.attackRange = this.attackRangeBow;
		else
			this.attackRange = this.weaponData.attackRange;
		this.attackRange_2 = this.attackRange * this.attackRange;
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
			Set<ItemStack> itemsToSpill = InventoryUtil.consumeItems(this.entityHost.getVillagerInventory(), inInventory, entry.getConsumables());
			itemsToSpill.forEach(stack -> {
				this.entityHost.entityDropItem(stack, this.entityHost.getEyeHeight());
				});
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return attackTarget.isEntityAlive() && shouldExecute() && !VilMethods.outOfAmmo(this.entityHost);
	}

	@Override
	public void updateTask()
	{
		double d0 = this.entityHost.getDistanceSq(attackTarget.posX, attackTarget.posY, attackTarget.posZ);
		double movementSpeed = this.entityHost.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
		boolean targetInSight = this.entityHost.getEntitySenses().canSee(attackTarget);
		
		if(this.friendlyFireAvoidTicks > 0 && this.randomPos != null)
		{
			this.entityHost.getNavigator().tryMoveToXYZ(randomPos.getX(), randomPos.getY(), randomPos.getZ(), movementSpeed);
			if(friendlyFireAvoidTicks-- == 0 || this.entityHost.getPosition().distanceSq(randomPos) < 1.0D)
			{
				this.friendlyFireAvoidTicks = 0;
				this.entityHost.getNavigator().clearPath();
				this.entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, movementSpeed);
				this.rangedAttackTime = 10;
			}
			return;
		}
		else
		{
			this.friendlyFireAvoidTicks = 0;
			this.randomPos = null;
		}
		
		if(d0 <= this.attackRange_2 && targetInSight) //When in range of attack, don't get needlessly closer
		{
			this.entityHost.getNavigator().clearPath();
		}
		else
		{
			getToTarget(movementSpeed);
		}
		float f;
		this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 55.0F);

		if(--this.rangedAttackTime <= 0)
		{
			if(d0 > this.attackRange_2 || !targetInSight || this.checkForConflict() || this.notLookingAtTarget() || !this.attackTarget.isEntityAlive())
			{
				this.rangedAttackTime = 2;
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

					if(this.weaponData.shotsForBurst > 1 && this.burstCount-- > 0)
					{
						this.rangedAttackTime = this.weaponData.burstCoolDown;
					}
					else
					{
						this.burstCount = this.weaponData.shotsForBurst;
						this.rangedAttackTime = this.weaponData.farnessFactor ? (int) (f1 * this.weaponData.coolDown) : this.weaponData.coolDown;
					}
				}
				else
				{
			         attackEntityWithRangedAttack(this.attackTarget, f * 2);
			         this.rangedAttackTime = MathHelper.floor(f * (this.weaponData.coolDown - this.attackTimeVariance) + this.attackTimeVariance);
				}
			}
			else
			{
				f = MathHelper.sqrt(d0) / this.attackRange;
				this.rangedAttackTime = MathHelper.floor(f * (this.weaponData.coolDown - this.attackTimeVariance) + this.attackTimeVariance);
			}
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
        	//TODO DEBUG
			this.shootShot(distanceFactor, target, acc, this.debugString);
		}
		else
		{
        	//TODO DEBUG
			this.shootGun(distanceFactor, target, acc, this.debugString);
		}
	}

	//TODO DEBUG
    private void shootGun(float distanceFactor, EntityLivingBase target, float f, String debugString2)
    {
		EntityBullet bullet = new EntityBullet(this.entityHost.getEntityWorld(), this.entityHost, entry, f, "Villager's Shot", debugString2);
		this.entityHost.getEntityWorld().spawnEntity(bullet);
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(this.entityHost.getEntityId(), this.getParticleData()), new TargetPoint(this.entityHost.dimension, this.entityHost.posX, this.entityHost.posY, this.entityHost.posZ, 124));
		this.entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F, .05F);
    }

	//TODO DEBUG
	private void shootShot(float distanceFactor, EntityLivingBase target, float f, String debugString2)
	{
		for(int i = 0; i < weaponData.projectiles; i++)
		{
			EntityBullet bullet = new EntityBullet(this.entityHost.getEntityWorld(), this.entityHost, entry, f + 5.0F, "Villager's Shot", debugString2);
			this.entityHost.getEntityWorld().spawnEntity(bullet);
		}
		NetWrapper.NETWORK.sendToAllAround(new GunFiredPacket(this.entityHost.getEntityId(), this.getParticleData()), new TargetPoint(this.entityHost.dimension, this.entityHost.posX, this.entityHost.posY, this.entityHost.posZ, 124));
		this.entityHost.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 3.0F, 2.0F);
	}

	private double[] getParticleData() 
	{
		double[] data = new double[6];
		data[0] = this.entityHost.posX;
		data[1] = this.entityHost.posY;
		data[2] = this.entityHost.posZ;
		data[3] = this.entityHost.rotationYawHead;
		data[4] = this.entityHost.rotationPitch;
		return data;
	}

	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityArrow entityarrow = getArrow(distanceFactor);
        if (this.entityHost.getHeldItemMainhand().getItem() instanceof ItemBow)
        {
            entityarrow = ((ItemBow) this.entityHost.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);
        }
        double d0 = target.posX - this.entityHost.posX;
        double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - entityarrow.posY;
        double d2 = target.posZ - this.entityHost.posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow = vanillaEnchantmentBow(this.entityHost.getHeldItemMainhand(), entityarrow, getPlayer());
        entityarrow.shoot(d0, d1 + d3 * (0.20000000298023224D * .5), d2, 2.8F, (float)(14 - (4 - this.entityHost.getEntityWorld().getDifficulty().getDifficultyId()) * 4));
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
		if(player != null && player.getDistanceSq(this.entityHost) > (followRange - 2) * (followRange - 2))
		{
			return true;
		}
		return false;
	}

    protected EntityArrow getArrow(float damage)
    {
        EntityArrow entityarrow;
        ItemArrow arrowItem = null;
        for(Item item : this.entry.getConsumables().keySet())
        {
        	if(item instanceof ItemArrow)
        	{
        		arrowItem = (ItemArrow) item;
        	}
        }
        if(arrowItem == null)
        {
        	arrowItem = (ItemArrow) Items.ARROW;
        }
        ItemStack falseStack = new ItemStack(arrowItem);
        entityarrow = arrowItem.createArrow(this.entityHost.getEntityWorld(), falseStack, this.entityHost);
        entityarrow.setEnchantmentEffectsFromEntity(this.entityHost, damage);
        return entityarrow;
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

    //TODO
	private boolean notLookingAtTarget() 
	{
		if(this.attackTarget != this.entityHost.getAttackTarget())
		{
			this.attackTarget = this.entityHost.getAttackTarget();
			return true;
		}
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
		//RayTraceResult lineOfSight = ProjectileHelper.checkForFirendlyFire(this.entityHost, this.entityHost.getEntityWorld(), this.entry.ballisticData.inaccuracy);
		//Friendly-fire debug
    	//TODO DEBUG
		Pair<RayTraceResult, String> pair = ProjectileHelper.checkForFirendlyFire(this.entityHost, this.entityHost.getEntityWorld(), this.entry.ballisticData.inaccuracy, ConfigHandler.friendlyFireSearchRange);
		RayTraceResult lineOfSight = pair.a;
		this.debugString = pair.b;
		if(lineOfSight != null && lineOfSight.typeOfHit == RayTraceResult.Type.ENTITY && this.friendlyFirePredicate.apply(lineOfSight.entityHit))
		{
			this.avoidFirendlyFire();
			return true;
		}
		
		List<EntityVillager> list = this.entityHost.getEntityWorld().getEntitiesWithinAABB(EntityVillager.class, this.attackTarget.getEntityBoundingBox().grow(1.5D), this.friendlyFirePredicate);
		if(!list.isEmpty())
		{
			/*if(ConfigHandler.debug)
				Log.info("Friendlies too close to fire! %s", list.get(0));*/
			this.rangedAttackTime++;
			return true;
		}
		return false;
	}

    
    private void getToTarget(double movementSpeed)
    {
    	BlockPos pos = VilMethods.getGuardBlockPos(this.entityHost);
		if(pos != null)
		{
			Path p = this.entityHost.getNavigator().getPathToEntityLiving(attackTarget);
			if(p != null)
			{
				truncatePath(p, pos, CommonProxy.MAX_GUARD_DIST - 31);
			}
			this.entityHost.getNavigator().setPath(p, movementSpeed);
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
					this.entityHost.getNavigator().setPath(p, movementSpeed);
				}
				return;
			}
		}
		this.entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, movementSpeed);
    }
    
    private void avoidFirendlyFire()
    {
		friendlyFireAvoidTicks = this.entityHost.getRNG().nextInt(20) + 20;
		this.entityHost.getNavigator().clearPath();
		boolean randomDirection = this.entityHost.getEntityWorld().rand.nextBoolean();
		float z = MathHelper.sin((float) Math.toRadians(this.entityHost.getRotationYawHead()));
		float x = MathHelper.cos((float) Math.toRadians(this.entityHost.getRotationYawHead()));
		if(randomDirection)
		{
			z = -z;
			x = -x;
		}
		float randomModifier = this.entityHost.getRNG().nextInt(7);
		randomPos = new Vec3i(this.entityHost.posX + (x * randomModifier), this.entityHost.posY, this.entityHost.posZ + (z * randomModifier));
		
		if(randomPos != null && this.entityHost.getRNG().nextInt(2) == 0)
		{
			this.entityHost.getNavigator().tryMoveToXYZ(randomPos.getX(), randomPos.getY(), randomPos.getZ(), this.entityHost.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}
    }

	private void truncatePath(Path p, BlockPos pos, float maxDistSq)
	{
		for(int i = 0; i < p.getCurrentPathLength(); i++)
		{
			if(p.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) >
					maxDistSq)
			{
				p.setCurrentPathLength(i - 1);
			}
			if(i < p.getCurrentPathLength() - 1 && p.getPathPointFromIndex(i).y - p.getPathPointFromIndex(i + 1).y >= 2) //go not where you cannot return
			{
				p.setCurrentPathLength(i - 1);
				return;
			}
		}
	}
	
	@Override
	public void resetTask()
	{
		attackTarget = null;
		if(this.entry.type != RangeAttackType.SINGLESHOT && this.entry.type != RangeAttackType.SHOT)
			rangedAttackTime = -1;
		this.entityHost.getNavigator().clearPath();
	}
}