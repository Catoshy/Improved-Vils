package com.joshycode.improvedvils.entity;

import java.util.UUID;

import com.joshycode.improvedvils.ServerProxy;
import com.joshycode.improvedvils.network.NetWrapper;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.world.World;

public class VillagerInvListener implements IInventoryChangedListener {

	private UUID playerId;
	World world;
	EntityVillager entity;
	int checkTick;
	
	public VillagerInvListener(UUID playerId, EntityVillager entity, World world2) 
	{
		this.playerId = playerId;
		this.entity = entity;
		this.world = world2;
	}
	
	@Override
	public void onInventoryChanged(IInventory invBasic) 
	{
		if(this.checkTick == this.entity.ticksExisted) return;
		
		this.checkTick = this.entity.ticksExisted;
		ServerProxy.updateGuiForClient(entity, this.world.getPlayerEntityByUUID(this.playerId), false);
		ServerProxy.checkArmourWeaponsAndFood(entity, this.world.getPlayerEntityByUUID(this.playerId));
	}
}
