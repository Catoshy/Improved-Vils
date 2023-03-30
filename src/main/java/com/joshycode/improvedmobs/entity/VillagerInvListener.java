package com.joshycode.improvedmobs.entity;

import java.util.UUID;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilStateQuery;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.world.World;

public class VillagerInvListener implements IInventoryChangedListener {

	private UUID playerId;
	World world;
	EntityVillager entity;
	
	public VillagerInvListener(UUID playerId, EntityVillager entity, World world2) {
		this.playerId = playerId;
		this.entity = entity;
		this.world = world2;
	}
	
	@Override
	public void onInventoryChanged(IInventory invBasic) {
		boolean flag2 = false; int i = 0, i2 = 0;
		flag2 = InventoryUtil.doesInventoryHaveItem(entity.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0;
		flag2 &= !getHungry();
		if(flag2) {
			if(hasGuardBlockPos()) {
				i = 2;
			} else if(isFollowing()) {
				i2 = 2;
			}
			i = 1;
			i2 = 1;
		}
		NetWrapper.NETWORK.sendTo(new VilStateQuery(i, i2), (EntityPlayerMP) world.getPlayerEntityByUUID(playerId));
	}
	
	private boolean isFollowing() {
		try {
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isFollowing();
		} catch (NullPointerException e) {}
		return false;
	}

	private boolean getHungry() {
		try {
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isHungry();
		} catch (NullPointerException e) {}
		return true;
	}

	private boolean hasGuardBlockPos() {
		try {
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getGuardBlockPos() != null;
		} catch (NullPointerException e) {}
		return true;
	}
}
