package com.joshycode.improvedvils.util;

import java.util.UUID;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;

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
		
		if(this.entity.getDistanceSq(this.world.getPlayerEntityByUUID(playerId)) > 12 || !this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getListener()) 
		{
			this.entity.getVillagerInventory().removeInventoryChangeListener(this);
			return;
		}
		if(ConfigHandler.debug)
			Log.info("inventory changed for villager", entity);

		this.checkTick = this.entity.ticksExisted;
		VillagerPlayerDealMethods.updateGuiForClient(entity, this.world.getPlayerEntityByUUID(this.playerId));
		if(this.world.getPlayerEntityByUUID(this.playerId) != null)
			VillagerPlayerDealMethods.checkArmourWeaponsAndFood(entity, this.playerId);
	}
}
