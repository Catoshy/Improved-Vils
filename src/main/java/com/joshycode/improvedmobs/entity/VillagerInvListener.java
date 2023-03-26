package com.joshycode.improvedmobs.entity;

import java.util.UUID;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilGuardQuery;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

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
		boolean flag2 = false; int i = 0;
		flag2 = InventoryUtil.doesInventoryHaveItem(entity.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0;
		flag2 &= !getHungry();
		if(flag2) {
			if(hasGuardBlockPos()) {
				i = 2;
			}
			i = 1;
		}
		NetWrapper.NETWORK.sendTo(new VilGuardQuery(i), (EntityPlayerMP) world.getPlayerEntityByUUID(playerId));
	}
	
	private boolean getHungry() {
		try {
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
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
