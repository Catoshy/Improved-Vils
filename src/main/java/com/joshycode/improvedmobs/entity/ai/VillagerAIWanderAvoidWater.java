package com.joshycode.improvedmobs.entity.ai;

import com.joshycode.improvedmobs.handler.CapabilityHandler;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;

public class VillagerAIWanderAvoidWater extends EntityAIWanderAvoidWater {

	public VillagerAIWanderAvoidWater(EntityCreature p_i47302_1_, double p_i47302_2_) {
		super(p_i47302_1_, p_i47302_2_);
	}

	@Override
	public boolean shouldExecute() {
		if(isReturning())
			return false;
		return super.shouldExecute();
	}

	private boolean isReturning() {
		try {
			this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		} catch (NullPointerException e) {}
		return false;
	}
}
