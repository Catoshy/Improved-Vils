package com.joshycode.improvedvils.gui;

import org.jline.utils.Log;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.entity.EntityVillagerContainer;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.VillagerInvListener;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class VilGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int intA, int intB)
	{
		Object gui = null;
		if(ID == CommonProxy.VIL_GUI_ID && world.getEntityByID(x) instanceof EntityVillager)
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
		else if(ID == CommonProxy.BATON_GUI_ID)
		{
			Log.info("received baton request client");
			gui = new GuiBatonStelling(x);
		}
		return gui;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		Object gui = null;
		if(ID == CommonProxy.VIL_GUI_ID && world.getEntityByID(x) instanceof EntityVillager)
		{
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			InventoryHands equipInv = new InventoryHands(e, "Hands", false);
			VillagerInvListener listener = new VillagerInvListener(player.getUniqueID(), e, world);
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(true);
			e.getVillagerInventory().addInventoryChangeListener(listener);
			equipInv.addInventoryChangeListener(listener);

			gui = new EntityVillagerContainer(player.inventory, e.getVillagerInventory(), equipInv);
		}
		return gui;
	}
}
