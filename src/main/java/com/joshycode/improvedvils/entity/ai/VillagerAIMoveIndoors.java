package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIMoveIndoors extends EntityAIMoveIndoors {

	EntityVillager entityHost;

	public VillagerAIMoveIndoors(EntityCreature entityIn)
	{
		super(entityIn);
		this.entityHost = (EntityVillager) entityIn;
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getDuty(entityHost) && !VilMethods.getHungry(entityHost))
			return false;

		if(super.shouldExecute())
		{
			VilMethods.setMovingIndoors(this.entityHost, true);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(!super.shouldContinueExecuting())
		{
			VilMethods.setMovingIndoors(this.entityHost, false);
			return false;
		}
		return true;
	}

	@Override
	public void resetTask()
	{
		VilMethods.setMovingIndoors(this.entityHost, false);
	}

}
