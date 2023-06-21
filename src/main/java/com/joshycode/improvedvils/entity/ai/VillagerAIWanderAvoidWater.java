package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.util.math.BlockPos;

public class VillagerAIWanderAvoidWater extends EntityAIWanderAvoidWater {

	public VillagerAIWanderAvoidWater(EntityCreature p_i47302_1_, double p_i47302_2_)
	{
		super(p_i47302_1_, p_i47302_2_);
	}

	@Override
	public boolean shouldExecute()
	{
		if(isReturning() || (getCommBlock() != null))
			return false;
		return super.shouldExecute();
	}

	private BlockPos getCommBlock()
	{
		try
		{
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getCommBlockPos();
		} catch (NullPointerException e) {}
		return null;
	}

	private boolean isReturning()
	{
		try
		{
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isReturning();
		} catch (NullPointerException e) {}
		return false;
	}
}
