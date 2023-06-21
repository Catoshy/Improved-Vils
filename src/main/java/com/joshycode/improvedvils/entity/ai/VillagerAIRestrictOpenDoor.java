package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIRestrictOpenDoor extends EntityAIRestrictOpenDoor {

	private EntityVillager villager;

	public VillagerAIRestrictOpenDoor(EntityCreature creatureIn)
	{
		super(creatureIn);
		this.villager = (EntityVillager) creatureIn;
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getGuardBlockPos(villager) != null)
			return false;

		return super.shouldExecute();
	}
}
