package com.joshycode.improvedvils.entity.ai;

import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public abstract class VillagerAITarget extends EntityAITarget {
	
    protected final int targetDistance;

	public VillagerAITarget(EntityVillager villager, boolean checkSight, boolean onlyNearby, int targetDistance)
	{
		super(villager, checkSight, onlyNearby);
		this.targetDistance = targetDistance;
	}

	@Override
	public boolean shouldExecute()
	{
		if(!areAttackCommands() || VilMethods.isOutsideHomeDist((EntityVillager) this.taskOwner) || VilMethods.isReturning((EntityVillager) this.taskOwner) || VilMethods.getMovingIndoors((EntityVillager) this.taskOwner))
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
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1)
    		{
    			return false;
    		}
    	}
    	return true;
	}

	private boolean areAttackCommands() 
	{
		return VilMethods.getCommBlockPos((EntityVillager) this.taskOwner) == null || VilMethods.getTroopFaring((EntityVillager) this.taskOwner) == TroopCommands.FORWARD_ATTACK;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(!areAttackCommands() || VilMethods.isOutsideHomeDist((EntityVillager) this.taskOwner))
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
    			return false;
    		}
    	}
		return super.shouldContinueExecuting();
	}
	
	@Override
	protected double getTargetDistance()
	{
		return this.targetDistance;
	}

	private boolean isDistanceTooGreat()
	{
		UUID playerId = VilMethods.getPlayerId((EntityVillager) this.taskOwner);
		EntityPlayer player = this.taskOwner.getEntityWorld().getPlayerEntityByUUID(playerId);
		double followRange = this.taskOwner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
		if(player != null && player.getDistanceSq(this.taskOwner) > (followRange - 2) * (followRange - 2))
		{
			return true;
		}
		return false;
	}
}
