package com.joshycode.improvedmobs.event;

import net.minecraft.entity.EntityAgeable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChildGrowEvent extends Event {

	public ChildGrowEvent(EntityAgeable entity) {
		this.entity = entity;
	}

	private final EntityAgeable entity;
	
	public EntityAgeable getEntity() {
		return entity;
	}
}
