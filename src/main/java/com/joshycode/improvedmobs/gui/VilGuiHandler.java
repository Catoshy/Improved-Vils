package com.joshycode.improvedmobs.gui;

import com.joshycode.improvedmobs.entity.EntityVillagerContainer;
import com.joshycode.improvedmobs.entity.InventoryHands;
import com.joshycode.improvedmobs.entity.VillagerInvListener;
import com.joshycode.improvedmobs.entity.ai.VillagerAIShootRanged;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class VilGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int intA, int intB) {
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			if(intA == -2) {
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), false, false);
			} else if (intA == -1) {
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), true, false);
			} else {
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), e.getEntityId(), intA, intB);
			}
		}
		return gui;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{//TODO change id from 100 to something that makes more sense like a static final var
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			InventoryHands equipInv = new InventoryHands(e, "Hands", false);
			e.getVillagerInventory().addInventoryChangeListener(new VillagerInvListener(player.getUniqueID(), e, world));
			/*e.tasks.taskEntries.forEach(t -> {
				if(t.action instanceof VillagerAIShootRanged) {
					if(!((VillagerAIShootRanged)t.action).isListening()) {
						e.getVillagerInventory().addInventoryChangeListener((IInventoryChangedListener) t.action);
						((VillagerAIShootRanged) t.action).setListening();
					}
					/** the hand/armor inventory is destroyed every time it closes so listeners must be added
					 * every time it's called
					equipInv.addInventoryChangeListener((IInventoryChangedListener) t.action);
				}
			}); */
			
			gui = new EntityVillagerContainer(player.inventory, e.getVillagerInventory(), equipInv);
		}
		return gui;
	}

}
