package com.joshycode.improvedmobs.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtil {

	/**
	 * @param invIn
	 * @param items
	 * @return number of items of that type found in inventory
	 */
	public static int doesInventoryHaveItem(IInventory invIn, Item item) {
		int r = 0;
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			String stackItem = stack.getUnlocalizedName();
			if(stackItem.equals(item.getUnlocalizedName())) {
				r++;
			}
		}
		System.out.println("Value r = " + r);
		return r;
	}
	
	public static ItemStack getStackByItem(IInventory invIn, Item item) {
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			String stackItem = stack.getUnlocalizedName();
			if(stackItem.equals(item.getUnlocalizedName())) {
				return stack;
			}
		}
		return null;
	}
}
