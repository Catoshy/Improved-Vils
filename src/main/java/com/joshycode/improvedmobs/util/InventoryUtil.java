package com.joshycode.improvedmobs.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.entity.ai.VillagerAIShootRanged;

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
			if(stack.getItem().equals(item)) {
				r++;
			}
		}
		return r;
	}
	
	public static ItemStack getStackByItem(IInventory invIn, Item item) {
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			String stackItem = stack.getUnlocalizedName();
			if(stackItem.equals(item.getUnlocalizedName())) {
				System.out.println("getStackByItem -- " + stack.toString());
				return stack;
			}
		}
		System.out.println("getStackByItem -- " + "null");
		return null;
	}
	
	public static ItemStack get1StackByItem(IInventory invIn, Item item) {
		ItemStack rStack = null;;
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			if(stack.getItem().equals(item)) {
				if(rStack == null) {
					System.out.println("get1StackByItem -- " + stack.toString());
					rStack = stack;
				} else {
					return null;
				}
			}
		}
		return rStack;
	}

	public static <T extends Item> ItemStack findAndDecrItem(IInventory invIn, Class<T> clazz) {
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			if(clazz.isInstance(stack.getItem())) {
				return invIn.decrStackSize(i, 1);
			}
		}		
		return null;
	}
	
	public static void consumeItems(IInventory invIn, Map<String, Integer> consumables, Map<String, Integer> howMuchOfEach) {		
		for(String itemToCons : consumables.keySet()) {
			ItemStack stack = invIn.getStackInSlot(consumables.get(itemToCons));
			String name = stack.getUnlocalizedName();
			int amt = howMuchOfEach.get(name);
			
			if(amt > stack.getCount()) {
				amt -= stack.getCount();
				invIn.setInventorySlotContents(consumables.get(itemToCons), ItemStack.EMPTY);
				howMuchOfEach.put(name, amt);
			} else {
				invIn.decrStackSize(consumables.get(itemToCons), amt);
			}
		}
	}
	
	@Nullable
	public static Map<String, Integer> getItemStacksInInventory(IInventory invIn, Map<String, Integer> items) {
		Map<String, Integer> consInVilInv = new HashMap();
		Map<String, Integer> toBeConsumed = new HashMap();	
		for(int i = 0; i < invIn.getSizeInventory(); i++) {
			ItemStack stack = invIn.getStackInSlot(i);
			String name = stack.getItem().getUnlocalizedName();
			if(items.keySet().contains(name)) {
				int val = 0;
				if(consInVilInv.get(name) != null)	
					val = consInVilInv.get(name);
				consInVilInv.put(name, val + stack.getCount());
				toBeConsumed.put(invIn.getStackInSlot(i).getUnlocalizedName(), i);			
				}
		}
		for(String s : items.keySet()) {
			if(consInVilInv.get(s) != null) {
				if(consInVilInv.get(s) < items.get(s))
					return null;
			} else {
				return null;
			}
		}
		return toBeConsumed;
	}
}
