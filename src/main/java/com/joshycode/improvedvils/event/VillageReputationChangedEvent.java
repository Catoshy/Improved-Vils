package com.joshycode.improvedvils.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class VillageReputationChangedEvent extends Event {
	
	final private EntityPlayer player;
	final private int reputationChange;
	
	public VillageReputationChangedEvent(EntityPlayer player, int reputationChange) {
		super();
		this.player = player;
		this.reputationChange = reputationChange;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public int getReputationChange() {
		return reputationChange;
	}
}
