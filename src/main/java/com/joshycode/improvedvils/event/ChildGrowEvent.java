package com.joshycode.improvedvils.event;

import net.minecraft.entity.EntityAgeable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChildGrowEvent extends Event {

	private final EntityAgeable entity;

	public ChildGrowEvent(EntityAgeable entity)
	{
		this.entity = entity;
	}

	public EntityAgeable getEntity()
	{
		return entity;
	}
}