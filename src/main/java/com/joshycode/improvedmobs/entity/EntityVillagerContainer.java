package com.joshycode.improvedmobs.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class EntityVillagerContainer extends Container {
		
	public EntityVillagerContainer(InventoryPlayer playerInv, IInventory villagerInventory, IInventory villagerHand) {
		this.addSlotToContainer(new Slot(villagerHand, 5, 8, 8));	//head
		this.addSlotToContainer(new Slot(villagerHand, 4, 8, 26));	//chest
		this.addSlotToContainer(new Slot(villagerHand, 3, 26, 8));	//legs
		this.addSlotToContainer(new Slot(villagerHand, 2, 26, 26));	//feet
		this.addSlotToContainer(new Slot(villagerHand, 0, 44, 8));	//MH
		this.addSlotToContainer(new Slot(villagerHand, 1, 44, 26));	//OH
		for(int i = 0; i < 4; i++)
				this.addSlotToContainer(new Slot(villagerInventory, i, 62 + i * 18, 44));
		for(int i = 0; i < 4; i++)
				this.addSlotToContainer(new Slot(villagerInventory, i, 62 + i * 18, 62));
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(playerInv, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(playerInv, i, 8+i*18, 142));
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer playerIn){
		super.onContainerClosed(playerIn);
		
	}

}
