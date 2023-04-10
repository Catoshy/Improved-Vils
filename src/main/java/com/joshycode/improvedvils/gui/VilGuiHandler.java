package com.joshycode.improvedvils.gui;

import com.joshycode.improvedvils.entity.EntityVillagerContainer;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.entity.VillagerInvListener;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class VilGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int intA, int intB) 
	{
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			if(intA == -2) 
			{
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), false, false);
			} 
			else if (intA == -1) 
			{
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), true, false);
			} 
			else
			{
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), intA, intB);
			}
		}
		return gui;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{//TODO change id from 100 to something that makes more sense like a static final var
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			InventoryHands equipInv = new InventoryHands(e, "Hands", false);
			VillagerInvListener listener = new VillagerInvListener(player.getUniqueID(), e, world);
			setInvListener(e, listener);
			e.getVillagerInventory().addInventoryChangeListener(listener);
			
			gui = new EntityVillagerContainer(player.inventory, e.getVillagerInventory(), equipInv);
		}
		return gui;
	}
	
	private void setInvListener(EntityVillager e, VillagerInvListener inv) 
	{
		try 
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(inv);
		} catch (NullPointerException ex) {}
	}

}
