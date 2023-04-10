package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;

import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIMoveTowardsRestriction extends EntityAIMoveTowardsRestriction {

	EntityVillager entity;
	
	public VillagerAIMoveTowardsRestriction(EntityVillager creatureIn, double speedIn) 
	{
		super(creatureIn, speedIn);
		this.entity = creatureIn;
	}
	
	public boolean shouldExecute()
	{
		if(VilCapabilityMethods.getCommBlockPos(this.entity) != null || VilCapabilityMethods.getGuardBlockPos(this.entity) != null || VilCapabilityMethods.getFollowing(this.entity))
		{
			return false;
		}
		return super.shouldExecute();
	}

}
