package com.joshycode.improvedvils.entity.ai;

import java.util.List;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import openmods.Log;

public abstract class VillagerAITarget<T extends EntityLivingBase> extends EntityAITarget {

	public VillagerAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby)
	{
		super(creature, checkSight, onlyNearby);
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getCommBlockPos((EntityVillager) this.taskOwner) != null) || VilMethods.isOutsideHomeDist((EntityVillager) this.taskOwner) || VilMethods.isReturning((EntityVillager) this.taskOwner) || VilMethods.getMovingIndoors((EntityVillager) this.taskOwner))
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
		UUID playerId = VilMethods.getPlayerId((EntityVillager) this.taskOwner);
		EntityPlayer player = this.taskOwner.getEntityWorld().getPlayerEntityByUUID(playerId);
		double followRange = this.taskOwner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
		if(player.getDistanceSq(this.taskOwner) > (followRange - 2) * (followRange - 2))
		{
			return true;
		}
		return false;
	}
}
