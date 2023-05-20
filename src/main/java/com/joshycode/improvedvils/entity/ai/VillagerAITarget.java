package com.joshycode.improvedvils.entity.ai;

import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public abstract class VillagerAITarget<T extends EntityLivingBase> extends EntityAITarget {

	public VillagerAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby) 
	{
		super(creature, checkSight, onlyNearby);
	}
	
	public boolean shouldExecute() 
	{
		if(VilMethods.getCommBlockPos((EntityVillager) this.taskOwner) != null)
			return false;
		if(VilMethods.isOutsideHomeDist((EntityVillager) this.taskOwner))
			return false;
		if(VilMethods.isReturning((EntityVillager) this.taskOwner))
			return false;
		if(VilMethods.getMovingIndoors((EntityVillager) this.taskOwner))
			return false;
		if(((EntityVillager) this.taskOwner).isMating())
    		return false;
		if(VilMethods.getFollowing((EntityVillager) this.taskOwner) 
				&& isDistanceTooGreat())
			return false;
		BlockPos pos = VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner);
    	if(pos != null) 
    	{
    		double hostDist = this.taskOwner.getDistanceSq(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
    		if(VilMethods.isReturning((EntityVillager) this.taskOwner)) 
    		{
    			return false;
    		}
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) 
    		{
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		}
    	}
    	return true;
	}

	@Override
	public boolean shouldContinueExecuting() 
	{
		if(VilMethods.getCommBlockPos((EntityVillager) this.taskOwner) != null || VilMethods.isOutsideHomeDist((EntityVillager) this.taskOwner))
			return false;
		if(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner) != null) 
		{
    		if(VilMethods.isReturning((EntityVillager) this.taskOwner)) 
    		{
    			return false;
    		}
    		double hostDist = this.taskOwner.getDistanceSq(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) 
    		{
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		}
    	}
		return super.shouldContinueExecuting();
	}
	
	private boolean isDistanceTooGreat() 
	{
		try 
		{	
			UUID playerId = VilMethods.getPlayerId((EntityVillager) this.taskOwner);
			EntityPlayer player = this.taskOwner.getEntityWorld().getPlayerEntityByUUID(playerId);
			double followRange = this.taskOwner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
			if(player.getDistanceSq(this.taskOwner) > (followRange - 2) * (followRange - 2)) 
			{
				return true;
			}
		} catch(NullPointerException e) {}
		return false;
	}
}
