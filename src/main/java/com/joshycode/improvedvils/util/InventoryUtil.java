package com.joshycode.improvedvils.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class InventoryUtil {

	/**
	 * @param invIn
	 * @param items
	 * @return number of items of that type found in inventory
	 */
	public static int doesInventoryHaveItem(IInventory invIn, Item item) 
	{
		int r = 0;
		for(int i = 0; i < invIn.getSizeInventory(); i++) 
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(stack.getItem().equals(item)) 
			{
				r++;
			}
		}
		return r;
	}
	
	public static ItemStack getStackByItem(IInventory invIn, Item item) 
	{
		for(int i = 0; i < invIn.getSizeInventory(); i++)
		{
			ItemStack stack = invIn.getStackInSlot(i);
			String stackItem = stack.getUnlocalizedName();
			if(stackItem.equals(item.getUnlocalizedName())) 
			{
				return stack;
			}
		}
		return null;
	}
	
	public static ItemStack get1StackByItem(IInventory invIn, Item item) 
	{
		ItemStack rStack = null;
		for(int i = 0; i < invIn.getSizeInventory(); i++) 
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(stack.getItem().equals(item)) 
			{
				if(rStack == null)
				{
					rStack = stack;
				}
				else 
				{
					return null;
				}
			}
		}
		return rStack;
	}

	public static <T extends Item> ItemStack findAndDecrItem(IInventory invIn, Class<T> clazz) 
	{
		for(int i = 0; i < invIn.getSizeInventory(); i++) 
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(clazz.isInstance(stack.getItem())) 
			{
				return invIn.decrStackSize(i, 1);
			}
		}		
		return null;
	}
	
	public static <T extends Item> ItemStack findAndIncrItem(IInventory invIn, Class<T> clazz) 
	{
		Set<Integer> emptyStacks = new HashSet();
		for(int i = 0; i < invIn.getSizeInventory(); i++) 
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(clazz.isInstance(stack.getItem())) 
			{
				stack.grow(1);
				return stack;
			} 
			else if(stack.isEmpty())
			{
				emptyStacks.add(i);
			}
		}	
		if(emptyStacks.size() > 0)
		{
			Item item = null;
			for(Item t : ForgeRegistries.ITEMS) 
			{
				if(clazz.isInstance(t)) 
				{
					item = t;
				}
			}
			if(item != null) 
			{
				ItemStack stack = new ItemStack(item);
				invIn.setInventorySlotContents(emptyStacks.iterator().next(), stack);
				return stack;
			}
		}
		return null;
	}
	
	public static void consumeItems(IInventory invIn, Map<String, Integer> consumables, Map<String, Integer> howMuchOfEach)
	{		
		for(String itemToCons : consumables.keySet()) 
		{
			ItemStack stack = invIn.getStackInSlot(consumables.get(itemToCons));
			String name = stack.getUnlocalizedName();
			int amt = howMuchOfEach.get(name);
			
			if(amt > stack.getCount()) 
			{
				amt -= stack.getCount();
				invIn.setInventorySlotContents(consumables.get(itemToCons), ItemStack.EMPTY);
				howMuchOfEach.put(name, amt);
			} 
			else 
			{
				invIn.decrStackSize(consumables.get(itemToCons), amt);
			}
		}
	}
	
	@Nullable
	public static Map<String, Integer> getItemStacksInInventory(IInventory invIn, Map<String, Integer> items)
	{
		Map<String, Integer> consInVilInv = new HashMap();
		Map<String, Integer> toBeConsumed = new HashMap();	
		for(int i = 0; i < invIn.getSizeInventory(); i++) 
		{
			ItemStack stack = invIn.getStackInSlot(i);
			String name = stack.getItem().getUnlocalizedName();
			if(items.keySet().contains(name))
			{
				int val = 0;
				
				if(consInVilInv.get(name) != null)	
					val = consInVilInv.get(name);
				
				consInVilInv.put(name, val + stack.getCount());
				toBeConsumed.put(invIn.getStackInSlot(i).getUnlocalizedName(), i);			
			}
		}
		for(String s : items.keySet())
		{
			if(consInVilInv.get(s) != null) 
			{
				if(consInVilInv.get(s) < items.get(s))
					return null;
				
			} 
			else
			{
				return null;
			}
		}
		return toBeConsumed;
	}

	public static Set<ItemStack> getStacksByItem(IInventory invIn, Class class1) 
	{
		Set<ItemStack> stacks = new HashSet();
		for(int i = 0; i < invIn.getSizeInventory(); i++)
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(class1.isInstance(stack.getItem())) 
			{
				stacks.add(stack);
			}
		}
		return stacks;
	}

	public static float getFoodSaturation(IInventory inventory) 
	{
		Set<ItemStack> food = getStacksByItem(inventory, ItemFood.class);
		float runningTotal = 0;
		for(ItemStack foodItem  : food)
		{
			runningTotal += ((ItemFood)foodItem.getItem()).getSaturationModifier(foodItem) * foodItem.getCount();
		}
		return runningTotal;
	}
}
