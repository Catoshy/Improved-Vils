package com.joshycode.improvedmobs.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.item.ItemStack;

public class VillagerAIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {

	public VillagerAIAvoidEntity(EntityCreature entityIn, Class<T> classToAvoidIn, float avoidDistanceIn,
			double farSpeedIn, double nearSpeedIn) {
		super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
	}
	
	public boolean shouldExecute() {
		if(this.entity.getHeldItemMainhand() != ItemStack.EMPTY)
			return false;
		return super.shouldExecute();
	}
}
