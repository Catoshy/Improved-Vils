package com.joshycode.improvedvils.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.VilAttributes;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import openmods.Log;

public class VillagerAIAttackMelee extends EntityAIBase {

	Random rand;
	Path path;
	private Item prevHeldItem;
	private EntityVillager attacker;
	
	@Nullable
	double speedToTarget;
	private int delayCounter;
	private boolean canPenalize = false;
	private boolean longMemory;
	private double targetX;
	private double targetY;
	private double targetZ;
	private int failedPathFindingPenalty;
	private int attackTick;
	private boolean runAway;
	private boolean brookingRangedWeapon;

	public VillagerAIAttackMelee(EntityVillager villager, double speedIn, boolean useLongMemory)
	{
		this.attacker = villager;
        this.longMemory = useLongMemory;
        this.setMutexBits(3);
		this.attacker = villager;
		this.rand = new Random();
		this.speedToTarget = speedIn;
	}

	@Override
	public boolean shouldExecute()
	{
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        if(isDoingSomethingMoreImportant())
        {
    		return false;
        }
        	
        if (canPenalize )
        {
            if (--this.delayCounter <= 0)
            {
                this.path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
                this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
                return this.path != null;
            }
            else
            {
                return true;
            }
        }
        this.path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);

        if (this.path != null)
        {
            return true;
        }
        else
        {
            return this.getAttackReachSqr(entitylivingbase) >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        }
    }

	private boolean isDoingSomethingMoreImportant()
	{
		if((VilMethods.getCommBlockPos((EntityVillager) this.attacker) != null) || VilMethods.isOutsideHomeDist((EntityVillager) this.attacker) || VilMethods.isReturning((EntityVillager) this.attacker) || VilMethods.isRefillingFood((EntityVillager) this.attacker))
			return true;
		if(VilMethods.getMovingIndoors((EntityVillager) this.attacker))
			return true;
		if(((EntityVillager) this.attacker).isMating())
    		return true;
		if(VilMethods.getFollowing((EntityVillager) this.attacker) && isDistanceTooGreat())
			return true;
		ItemStack stack = this.attacker.getHeldItemMainhand();
		String s= stack.getUnlocalizedName();
		for(WeaponBrooksData g : ConfigHandler.configuredGuns.keySet())
		{
			if(s.equals(g.itemUnlocalizedName))
			{
				if(!CommonProxy.RANGE_BLACKLIST.contains(this.attacker.getAttackTarget().getClass()))
				{
					if(!(ConfigHandler.weaponFromItemName(s).meleeInRange && ((this.attacker.getDistanceSq(this.attacker.getAttackTarget())) < 8 ) || VilMethods.outOfAmmo(this.attacker)))
					{
						return true;
					}
				}
				this.brookingRangedWeapon = true;
			}
		}
		this.prevHeldItem = stack.getItem();
		return false;
	}

    @Override
	public void startExecuting()
    {
        this.attacker.getNavigator().setPath(this.path, this.speedToTarget);
        this.delayCounter = 0;
    }

	@Override
	public void updateTask()
    {
		Path path = this.attacker.getNavigator().getPath();
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
		double modifier = 0.0d;
	    AttributeModifier mod = this.attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(VillagerAIDrinkPotion.MODIFIER_UUID);

	    if(mod != null)
	    {
	    	modifier = mod.getAmount();
	    }
	    boolean flag = false;
	    if(this.attackTick > .6 * this.getCooldown() && this.attackTick != 0)
	    {
	    	this.runAway = true;
			modifier += .3d;
		    if(VilMethods.getGuardBlockPos((EntityVillager) this.attacker) != null)
		    {
				path = this.attacker.getNavigator().getPathToPos(VilMethods.getGuardBlockPos((EntityVillager) this.attacker));
		    }
		    else
		    {
				Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(attacker, 8, 6, entitylivingbase.getPositionVector());
				if(vec != null)
					path = this.attacker.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
		    }
	    }
	    else
	    {
	    	if(this.runAway)
	    	{
	    		this.attacker.getNavigator().clearPath();
	    		this.runAway = false;
	    	}

	    	if(this.attacker.getDistanceSq(entitylivingbase) < getAttackReachSqr(entitylivingbase) * .75)
	    	{
	    		if(this.attacker.getDistanceSq(entitylivingbase) < ConfigHandler.attackReach)
	    		{
	    			flag = true;
	    			Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(attacker, 16, 8, entitylivingbase.getPositionVector());

	    			if(vec != null)
	    				path = this.attacker.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
	    		}
	    		else
	    		{
	    			path = null;
	    		}
	    	}
	    	else
	    	{
	    		path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
	    	}
	    }
	    if(path != null && !flag)
	    {
	    	if(VilMethods.getGuardBlockPos((EntityVillager) this.attacker) != null &&
    			   path.getFinalPathPoint().distanceToSquared(VilMethods.guardBlockAsPP((EntityVillager) this.attacker)) > CommonProxy.MAX_GUARD_DIST - 31)
	    	{
	    		this.truncatePath(path, VilMethods.getGuardBlockPos((EntityVillager) this.attacker));
	    	}
	    }

        this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
	    --this.delayCounter;

	    if ((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F))
	    {
	        this.targetX = entitylivingbase.posX;
	        this.targetY = entitylivingbase.getEntityBoundingBox().minY;
	        this.targetZ = entitylivingbase.posZ;
	        this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

	        if (this.canPenalize)
	        {
	            this.delayCounter += failedPathFindingPenalty;
	            if (path != null)
	            {
	                net.minecraft.pathfinding.PathPoint finalPathPoint = path.getFinalPathPoint();
	                if (finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
	                    failedPathFindingPenalty = 0;
	                else
	                    failedPathFindingPenalty += 10;
	            }
	            else
	            {
	                failedPathFindingPenalty += 10;
	            }
	        }

	        if (d0 > 1024.0D)
	        {
	            this.delayCounter += 10;
	        }
	        else if (d0 > 256.0D)
	        {
	            this.delayCounter += 5;
	        }

	        if (path == null)
	        {
	            this.delayCounter += 15;
	        }
	    }
	    this.attacker.getNavigator().setPath(path, this.speedToTarget + modifier);
	    this.attackTick = Math.max(this.attackTick - 1, 0);
	    this.checkAndPerformAttack(entitylivingbase, d0);
    }

	@Override
	public boolean shouldContinueExecuting()
	{
		if(!this.attacker.getHeldItemMainhand().getItem().equals(prevHeldItem))
		{
			return false;
		}
		BlockPos pos = VilMethods.getGuardBlockPos((EntityVillager) this.attacker);
		if(pos != null && this.attacker.getDistanceSq(pos) > CommonProxy.MAX_GUARD_DIST - 31)
		{
			return false;
		}
		if(VilMethods.getCommBlockPos((EntityVillager) this.attacker) != null || VilMethods.isOutsideHomeDist((EntityVillager) this.attacker)
				|| VilMethods.getMovingIndoors((EntityVillager) this.attacker))
		{
    		return false;
    	}
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null || !entitylivingbase.isEntityAlive())
        {
            return false;
        }
    	if(this.brookingRangedWeapon && (!(this.attacker.getDistanceSq(this.attacker.getAttackTarget()) < 8 || VilMethods.outOfAmmo(this.attacker))))
    	{
			return false;
    	}
        if (canPenalize)
        {
            if (--this.delayCounter <= 0)
            {
                Path path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
                this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
                return path != null;
            }
            
            else
            {
                return true;
            }
        }
        Path path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);

        if (path != null)
        {
            return true;
        }
        else
        {
            return this.getAttackReachSqr(entitylivingbase) >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        }
	}

	private boolean attackEntityAsVillager(EntityLivingBase entityIn)
	{
		float f = (float)this.attacker.getEntityAttribute(VilAttributes.VIL_DAMAGE).getAttributeValue();
        int i = 0;

        ItemStack itemstack = this.attacker.getHeldItemMainhand();

        List<AttributeModifier> l = new ArrayList<AttributeModifier>(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
        
        if(!l.isEmpty())
        {
	        float f1 = (float) l.get(0).getAmount();
	
	    	if(f1 >= 1.5F)
	    		f1 *= ConfigHandler.villagerDeBuffMelee;
	
	    	f += f1;
        }
        
        f += EnchantmentHelper.getModifierForCreature(this.attacker.getHeldItemMainhand(), entityIn.getCreatureAttribute());
        i += EnchantmentHelper.getKnockbackModifier(this.attacker);

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this.attacker), f);

        if(!itemstack.isEmpty())
        {
        	EntityPlayer playerEnt = null;
        	UUID player = getPlayerId();

    		if(player != null)
    		{
    			playerEnt = this.attacker.world.getPlayerEntityByUUID(player);
    		}
        	if(playerEnt != null)
        	{
        		itemstack.hitEntity(entityIn, playerEnt);
        	}
        	else
        	{
        		itemstack.getItem().hitEntity(itemstack, entityIn, this.attacker);
        	}
        }
        if (flag)
        {
            if (i > 0 && entityIn instanceof EntityLivingBase)
            {
                entityIn.knockBack(this.attacker, i * 0.5F, MathHelper.sin(this.attacker.rotationYaw * 0.017453292F), (-MathHelper.cos(this.attacker.rotationYaw * 0.017453292F)));
                this.attacker.motionX *= 0.6D;
                this.attacker.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this.attacker);

            if (j > 0)
            {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, this.attacker) && itemstack1.getItem().isShield(itemstack1, entityplayer))
                {
                    float f2 = 0.25F + EnchantmentHelper.getEfficiencyModifier(this.attacker) * 0.05F;
                    if (this.rand.nextFloat() < f2)
                    {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        this.attacker.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }
            EnchantmentHelper.applyThornEnchantments(entityIn, this.attacker);
        }

        return flag;
	}

	protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_)
    {
        double d0 = this.getAttackReachSqr(p_190102_1_);

        if (p_190102_2_ <= d0 && this.attackTick <= 0 && !isDrinking())
        {
            this.attackTick = this.getCooldown();
            this.attacker.setActiveHand(EnumHand.MAIN_HAND);
            attackEntityAsVillager(p_190102_1_);
        }
    }

	private int getCooldown()
	{
		int eAttr = (int) (this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
		return (int) (eAttr * ConfigHandler.meleeAttackCooldown);
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

	private boolean isDistanceTooGreat()
	{
		try
		{
			UUID playerId = VilMethods.getPlayerId((EntityVillager) this.attacker);
			EntityPlayer player = this.attacker.getEntityWorld().getPlayerEntityByUUID(playerId);
			double followRange = this.attacker.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
			if(player.getDistanceSq(this.attacker) > (followRange - 2) * (followRange - 2)) {
				return true;
			}
		} catch(NullPointerException e) {}
		return false;
	}

	private boolean isDrinking()
	{
		try
		{
			return this.attacker.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isDrinking();
		} catch(NullPointerException e) {}
		return false;
	}

	@Nullable
	private UUID getPlayerId()
	{
		try
		{
			return this.attacker.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
		} catch (NullPointerException e) {}
		return null;
	}

	@Override
	public void resetTask()
	{
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        this.brookingRangedWeapon = false;
        
        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative()))
        {
            this.attacker.setAttackTarget((EntityLivingBase)null);
        }

        this.attacker.getNavigator().clearPath();
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return this.attacker.width * ConfigHandler.attackReach * this.attacker.width * ConfigHandler.attackReach + attackTarget.width;
    }

    protected int itemUseDuration()
    {
    	return this.attacker.getHeldItemOffhand().getMaxItemUseDuration() - this.attacker.getItemInUseCount();
    }
}
