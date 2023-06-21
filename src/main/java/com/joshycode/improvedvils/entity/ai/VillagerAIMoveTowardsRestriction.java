package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIMoveTowardsRestriction extends EntityAIMoveTowardsRestriction {

	EntityVillager entity;

	public VillagerAIMoveTowardsRestriction(EntityVillager creatureIn, double speedIn)
	{
		super(creatureIn, speedIn);
		this.entity = creatureIn;
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getCommBlockPos(this.entity) != null || VilMethods.getGuardBlockPos(this.entity) != null || VilMethods.getFollowing(this.entity))
		{
			return false;
		}
		return super.shouldExecute();
	}

}
