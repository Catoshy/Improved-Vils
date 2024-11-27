package com.joshycode.improvedvils.entity.ai;

import java.util.UUID;

import javax.annotation.Nullable;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.PathUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VillagerAIAttackMelee extends EntityAIBase {

	private Item prevHeldItem;
	private Path path;
	private PathNavigate navigator;
	EntityVillager attacker;
	private double speedToTarget;
	private double modifier;
	private boolean canPenalize = false;
	private boolean longMemory;
	private double targetX;
	private double targetY;
	private double targetZ;
	private int delayCounter;
	private int failedPathFindingPenalty;
	int attackTick;
	private boolean runAway;
	private boolean brookingRangedWeapon;

	public VillagerAIAttackMelee(EntityVillager villager, double speedIn, boolean useLongMemory)
	{
		this.attacker = villager;
        this.longMemory = useLongMemory;
		this.attacker = villager;
		this.speedToTarget = speedIn;
		this.navigator = villager.getNavigator();
		this.modifier = 0;
        this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		EntityLivingBase entityTarget = this.attacker.getAttackTarget();
		
        if (entityTarget == null || !entityTarget.isEntityAlive() || isDoingSomethingMoreImportant())
            return false;

        if (canPenalize )
        {
            if (--this.delayCounter <= 0)
            {
                this.path = this.getPathToTarget(entityTarget);
                this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
                return this.path != null;
            }
            else
            {
                return true;
            }
        }
        this.path = this.getPathToTarget(entityTarget);
        if (this.path != null)//TODO
        {
        	this.navigator.setPath(this.path, this.hostSpeed());
            return true;
        }
        else
        {
            return VillagerAIAttack.getAttackReachSqr(this.attacker, entityTarget) >= this.attacker.getDistanceSq(entityTarget.posX, entityTarget.getEntityBoundingBox().minY, entityTarget.posZ);
        }
    }

	private boolean isDoingSomethingMoreImportant()
	{
		if(!areAttackCommands() || VilMethods.isOutsideHomeDist(this.attacker) || VilMethods.isReturning(this.attacker) || VilMethods.isRefillingFood(this.attacker))
			return true;
		if(VilMethods.getMovingIndoors(this.attacker))
			return true;
		if(( this.attacker).isMating())
    		return true;
		if(VilMethods.getFollowing(this.attacker) && isDistanceTooGreat())
			return true;
		ItemStack stack = this.attacker.getHeldItemMainhand();
		String s = stack.getUnlocalizedName();
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
	
	private boolean areAttackCommands() 
	{
		return VilMethods.getCommBlockPos(this.attacker) == null || VilMethods.getTroopFaring(this.attacker) == TroopCommands.FORWARD_ATTACK;
	}

	@Override
	public void updateTask()
    {
		EntityLivingBase entityTarget = this.attacker.getAttackTarget();
		boolean flag = false;
	    if(this.attackTick > .6 * VillagerAIAttack.getCooldown(this.attacker) && this.attackTick != 0)
	    {
	    	this.runAway = true;
	    	this.modifier = .3d;
		    if(VilMethods.getGuardBlockPos(this.attacker) != null)
		    {
				this.path = this.navigator.getPathToPos(VilMethods.getGuardBlockPos(this.attacker));
		    }
		    else
		    {
				Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(attacker, 8, 6, entityTarget.getPositionVector());
				if(vec != null)
					this.path = this.navigator.getPathToXYZ(vec.x, vec.y, vec.z);
		    }
	    }
	    else
	    {
	    	if(this.runAway)
	    	{
	    		this.attacker.getNavigator().clearPath();
	    		this.modifier = 0d;
	    		this.runAway = false;
	    	}

	    	if(this.attacker.getDistanceSq(entityTarget) < VillagerAIAttack.getAttackReachSqr(this.attacker, entityTarget) * .75)
	    	{
	    		if(this.attacker.getDistanceSq(entityTarget) < ConfigHandler.attackReach)
	    		{
	    			flag = true;
	    			Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(attacker, 16, 8, entityTarget.getPositionVector());

	    			if(vec != null)
	    				this.path = this.navigator.getPathToXYZ(vec.x, vec.y, vec.z);
	    		}
	    		else
	    		{
	    			this.path = null;
	    		}
	    	}
	    	else
	    	{
	    		this.path = this.getPathToTarget(entityTarget);
	    	}
	    }
	    
	    if(VilMethods.getGuardBlockPos(this.attacker) != null && !flag)
	    {
	    	this.truncatePath(VilMethods.getGuardBlockPos(this.attacker));
	    }
	    
	    if(this.path != null)
        	this.navigator.setPath(this.path, this.hostSpeed());
	    
        this.attacker.getLookHelper().setLookPositionWithEntity(entityTarget, 30.0F, 30.0F);
        double d0 = this.attacker.getDistanceSq(entityTarget.posX, entityTarget.getEntityBoundingBox().minY, entityTarget.posZ);
	    --this.delayCounter;

	    if ((this.longMemory || this.attacker.getEntitySenses().canSee(entityTarget)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entityTarget.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= .5D || this.attacker.getRNG().nextFloat() < 0.05F))
	    {
	        this.targetX = entityTarget.posX;
	        this.targetY = entityTarget.getEntityBoundingBox().minY;
	        this.targetZ = entityTarget.posZ;
	        this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

	        /*
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
	        }*/

	        if (d0 > 1024.0D)
	        {
	            this.delayCounter += 10;
	        }
	        else if (d0 > 256.0D)
	        {
	            this.delayCounter += 5;
	        }

	        if (this.attacker.getNavigator().getPath() == null)
	        {
	            this.delayCounter += 15;
	        }
	    }
	    this.attackTick = Math.max(this.attackTick - 1, 0);
	    if(VillagerAIAttack.checkAndPerformAttack(this.attacker, entityTarget, this.attackTick, d0, false))
	    	this.attackTick = VillagerAIAttack.getCooldown(this.attacker);
	    super.updateTask();
    }

	@Override
	public boolean shouldContinueExecuting()
	{
		if(!this.attacker.getHeldItemMainhand().getItem().equals(prevHeldItem))
		{
			Log.info("not prev held item", null);
			return false;
		}
		BlockPos pos = VilMethods.getGuardBlockPos(this.attacker);
		if(pos != null && this.attacker.getDistanceSq(pos) > CommonProxy.MAX_GUARD_DIST - 31)
		{
			Log.info("Guard block problems", null);
			return false;
		}
		if(!areAttackCommands()  || VilMethods.isOutsideHomeDist( this.attacker)
				|| VilMethods.getMovingIndoors(this.attacker))
		{
			Log.info("something something commands", null);
    		return false;
    	}
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null || !entitylivingbase.isEntityAlive())
        {
			Log.info("Target null", null);
            return false;
        }
    	if(this.brookingRangedWeapon && (!(this.attacker.getDistanceSq(this.attacker.getAttackTarget()) < 8 || VilMethods.outOfAmmo(this.attacker))))
    	{
			Log.info("weapons.", null);
			return false;
    	}
        /*if (canPenalize)
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
            return VillagerAIAttack.getAttackReachSqr(this.attacker, entitylivingbase) >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        }*/ 
    	return true;
	}

	private void truncatePath(BlockPos pos)
	{
		if(this.path == null) return;
		for(int i = 0; i < this.path.getCurrentPathLength(); i++)
		{
			if(this.path.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) >
					CommonProxy.MAX_GUARD_DIST - 31 /* 2 x 2*/)
			{
				this.path.setCurrentPathLength(i - 1);
			}
			if(i < this.path.getCurrentPathLength() -1 && this.path.getPathPointFromIndex(i).y - this.path.getPathPointFromIndex(i + 1).y >= 2) //go not where you cannot return
			{
				this.path.setCurrentPathLength(i - 1);
			}
		}
	}

	private boolean isDistanceTooGreat()
	{
		UUID playerId = VilMethods.getPlayerId(this.attacker);
		EntityPlayer player = this.attacker.getEntityWorld().getPlayerEntityByUUID(playerId);
		double followRange = this.attacker.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
		if(player != null && player.getDistanceSq(this.attacker) > (followRange - 2) * (followRange - 2)) 
		{
			return true;
		}
		return false;
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

	@Nullable
	private Path getPathToTarget(EntityLivingBase entityTarget) 
	{
		Vec3d pos = PathUtil.findNavigableBlockInDirection(this.attacker.getPosition(), entityTarget.getPosition(), this.attacker, 0.0F, false);
		return this.navigator.getPathToXYZ(pos.x, pos.y, pos.z);
	}

    protected double hostSpeed() { return (this.speedToTarget > 0 ? this.speedToTarget : this.attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) + this.modifier; }
}
