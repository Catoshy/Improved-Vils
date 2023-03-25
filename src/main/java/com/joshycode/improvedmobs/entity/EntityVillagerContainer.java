package com.joshycode.improvedmobs.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class EntityVillagerContainer extends Container {

	static final int ACTIVE_SLOT = 13;
	
	private static final int INV_START = ACTIVE_SLOT+1, INV_END = INV_START+26, HOTBAR_START = INV_END+1, HOTBAR_END = HOTBAR_START+8;
	
	public EntityVillagerContainer(InventoryPlayer playerInv, IInventory villagerInventory, IInventory villagerHand) {
		this.addSlotToContainer(new Slot(villagerHand, 5, 8, 8));	//head
		this.addSlotToContainer(new Slot(villagerHand, 4, 8, 26));	//chest
		this.addSlotToContainer(new Slot(villagerHand, 3, 26, 8));	//legs
		this.addSlotToContainer(new Slot(villagerHand, 2, 26, 26));	//feet
		this.addSlotToContainer(new Slot(villagerHand, 0, 44, 8));	//MH
		this.addSlotToContainer(new Slot(villagerHand, 1, 44, 26));	//OH
		for(int i = 0; i < 4; i++)
				this.addSlotToContainer(new Slot(villagerInventory, i, 8 + i * 18, 62));
		for(int i = 0; i < 4; i++)
				this.addSlotToContainer(new Slot(villagerInventory, i + 4, 8 + i * 18, 80));
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(playerInv, j+i*9+9, 8+j*18, 102+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(playerInv, i, 8+i*18, 160));
	}
	
	/**
	* Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	* special thanks to "coolAlias" on MC forums for providing this code
	*/
	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
	ItemStack itemstack = ItemStack.EMPTY;
	Slot slot = (Slot)this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if(par2 < INV_START) {
				// place in action bar
				if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false))
				{
					if(!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			// item in player's inventory, but not in action bar
			else if (par2 >= INV_START && par2 < HOTBAR_START)
			{
				// place in action bar
				if (!this.mergeItemStack(itemstack1, 6, ACTIVE_SLOT + 1, false))
				{
					return ItemStack.EMPTY;
				}
			}
			// item in action bar - place in player inventory
			else if (par2 >= HOTBAR_START && par2 < HOTBAR_END + 1)
			{
				if (!this.mergeItemStack(itemstack1, 6, ACTIVE_SLOT + 1, false))
				{
					return ItemStack.EMPTY;
				}
			}
			if (itemstack.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
	
			if (itemstack1.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}
		}
		return itemstack;
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
