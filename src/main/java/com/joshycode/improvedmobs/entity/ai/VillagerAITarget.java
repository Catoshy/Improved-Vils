package com.joshycode.improvedmobs.entity.ai;

import java.util.Collections;
import java.util.List;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.PositionUtil;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;

public abstract class VillagerAITarget<T extends EntityLivingBase> extends EntityAITarget {

	public VillagerAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
		super(creature, checkSight, onlyNearby);
	}
	
	public boolean shouldExecute() {
		if(CapabilityHandler.getCommBlockPos((EntityVillager) this.taskOwner) != null || PositionUtil.isOutsideHomeDist((EntityVillager) this.taskOwner)
				|| CapabilityHandler.getMovingIndoors((EntityVillager) this.taskOwner)) {
    		return false;
    	}
		BlockPos pos = CapabilityHandler.getGuardBlockPos((EntityVillager) this.taskOwner);
    	if(pos != null) {
    		double hostDist = this.taskOwner.getDistanceSq(CapabilityHandler.getGuardBlockPos((EntityVillager) this.taskOwner));
    		if(CapabilityHandler.isReturning((EntityVillager) this.taskOwner)) {
    			return false;
    		}
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) {
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		}
    	}
    	return true;
	}

	@Override
	public boolean shouldContinueExecuting() {
		if(CapabilityHandler.getCommBlockPos((EntityVillager) this.taskOwner) != null || PositionUtil.isOutsideHomeDist((EntityVillager) this.taskOwner))
			return false;
		if(CapabilityHandler.getGuardBlockPos((EntityVillager) this.taskOwner) != null) {
    		if(CapabilityHandler.isReturning((EntityVillager) this.taskOwner)) {
    			return false;
    		}
    		double hostDist = this.taskOwner.getDistanceSq(CapabilityHandler.getGuardBlockPos((EntityVillager) this.taskOwner));
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) {
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		}
    	}
		return super.shouldContinueExecuting();
	}
}
