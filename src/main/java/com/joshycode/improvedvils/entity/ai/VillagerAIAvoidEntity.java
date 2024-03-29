package com.joshycode.improvedvils.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAvoidEntity;

public class VillagerAIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {

	public VillagerAIAvoidEntity(EntityCreature entityIn, Class<T> classToAvoidIn, float avoidDistanceIn,
			double farSpeedIn, double nearSpeedIn)
	{
		super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
	}

	@Override
	public boolean shouldExecute()
	{
		if(!this.entity.getHeldItemMainhand().isEmpty())
			return false;

		return super.shouldExecute();
	}
}
