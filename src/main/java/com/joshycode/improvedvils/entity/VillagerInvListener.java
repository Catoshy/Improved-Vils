package com.joshycode.improvedvils.entity;

import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
import com.joshycode.improvedvils.entity.ai.VillagerAIGuard;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilStateQuery;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class VillagerInvListener implements IInventoryChangedListener {

	private UUID playerId;
	World world;
	EntityVillager entity;
	int int1, int2, checkTick;
	
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
		boolean flag2 = false; int i = 0, i2 = 0;
		flag2 = InventoryUtil.doesInventoryHaveItem
				(entity.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0;
		flag2 &= !getHungry();
		if(flag2) 
		{
			if(hasGuardBlockPos()) 
			{
				i = 2;
				i2 = 1;
			} 
			else if(isFollowing()) 
			{
				i2 = 2;
				i= 1;
			} 
			else 
			{
				i = 1;
				i2 = 1;
			}
		} 
		else 
		{
			VilCapabilityMethods.setGuardBlock(entity, null);
			VilCapabilityMethods.setFollowing(entity, false);
		}
		if(i != int1 && i2 != int2) 
		{
			int1 = i; int2 = i2;
			NetWrapper.NETWORK.sendTo(new VilStateQuery(i, i2), (EntityPlayerMP) world.getPlayerEntityByUUID(playerId));
		}
	}
	
	private boolean isFollowing() 
	{
		try 
		{
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isFollowing();
		} catch (NullPointerException e) {}
		return false;
	}

	private boolean getHungry()
	{
		try 
		{
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException e) {}
		return true;
	}

	private boolean hasGuardBlockPos()
	{
		try 
		{
			return this.entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getGuardBlockPos() != null;
		} catch (NullPointerException e) {}
		return true;
	}
}
