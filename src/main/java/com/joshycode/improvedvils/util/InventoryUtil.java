package com.joshycode.improvedvils.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class InventoryUtil {

	/**
	 * @param invIn
	 * @param items
	 * @return number of itemstacks of that type found in inventory
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
	
	public static int findEmptySlot(IInventory invIn)
	{
		int i = -1;
		for(int ii = 0; ii < invIn.getSizeInventory(); ii++)
		{
			ItemStack stack = invIn.getStackInSlot(ii);
			if(stack.isEmpty())
			{
				i = ii;
				break;
			}
		}
		return i;
	}

	@Nullable
	public static ItemStack getLeastStackByItem(IInventory invIn, Item item)
	{
		int size = invIn.getInventoryStackLimit() + 1;
		ItemStack stack = null;
		for(int i = 0; i < invIn.getSizeInventory(); i++)
		{
			ItemStack stack1 = invIn.getStackInSlot(i);
			if(stack1.getItem().equals(item) && stack1.getCount() < size)
			{
				stack = stack1;
				size = stack.getCount();
			}
		}
		return stack;
	}
	
	@Nullable
	public static ItemStack getGreatestStackByItem(IInventory invIn, Item item)
	{
		int size = 0;
		ItemStack rStack = null;
		for(int i = 0; i < invIn.getSizeInventory(); i++)
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(stack.getItem().equals(item) && stack.getCount() > size)
			{
				rStack = stack;
				size = rStack.getCount();
			}
		}
		return rStack;
	}

	@Nullable
	public static ItemStack getOnly1StackByItem(IInventory invIn, Item item)
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
		Set<Integer> emptyStacks = new HashSet<Integer>();
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

	public static Set<ItemStack> consumeItems(IInventory invIn, Map<Item, Integer> consumablesInInventory, Map<Item, Integer> howMuchOfEach)
	{
		Set<ItemStack> leftOverItems = new HashSet<ItemStack>();
		for(Item itemToCons : consumablesInInventory.keySet())
		{
			ItemStack stack = invIn.getStackInSlot(consumablesInInventory.get(itemToCons));
			int amt = howMuchOfEach.get(itemToCons);
			
			int i = 0;
			while(amt > 0 && i < 5)
			{
				if(amt > stack.getCount())
				{
					amt -= stack.getCount();
					stack = getLeastStackByItem(invIn, itemToCons);
				}
				else
				{
					invIn.decrStackSize(consumablesInInventory.get(itemToCons), amt);
					amt = 0;
				}
				if(stack == null)
					continue;
				i++;
			}
		}
		for(Item itemToGive : howMuchOfEach.keySet())
		{
			int amt = howMuchOfEach.get(itemToGive);
			if(amt >= 0) continue;
			
			ItemStack stack = getLeastStackByItem(invIn, itemToGive);
			Log.info("getStackByiTEM FOR CONSUME PLUS IS %s", stack);
			amt = Math.min(itemToGive.getItemStackLimit(stack), Math.abs(amt));
			if(stack == null || stack.getCount() + amt > invIn.getInventoryStackLimit() || stack.getCount() + amt > stack.getMaxStackSize())
			{
				int emptySlot = findEmptySlot(invIn);
				ItemStack leftOver = new ItemStack(itemToGive, amt);
				if(emptySlot == -1)
					leftOverItems.add(leftOver);
				else
					invIn.setInventorySlotContents(emptySlot, new ItemStack(itemToGive, amt));
			}
			else
			{
				stack.grow(amt);
			}
		}
		
		return leftOverItems;
	}
	
	

	@Nullable
	public static Map<Item, Integer> getItemStacksInInventory(IInventory invIn, Map<Item, Integer> items)
	{
		Set<Item> toUse = new HashSet<>();
		for(Map.Entry<Item, Integer> e : items.entrySet())
		{
			if(e.getValue() >= 0)
			{
				toUse.add(e.getKey());
			}
		}		
		Map<Item, Integer> consInVilInv = new HashMap<Item, Integer>();
		Map<Item, Integer> toBeConsumed = new HashMap<Item, Integer>();
		for(int i = 0; i < invIn.getSizeInventory(); i++)
		{
			ItemStack stack = invIn.getStackInSlot(i);
			if(toUse.contains(stack.getItem()))
			{
				int val = 0;

				if(consInVilInv.get(stack.getItem()) != null)
					val = consInVilInv.get(stack.getItem());

				consInVilInv.put(stack.getItem(), val + stack.getCount());
				toBeConsumed.put(stack.getItem(), i);
			}
		}
		for(Item i : toUse)
		{
			if(consInVilInv.get(i) != null)
			{
				if(consInVilInv.get(i) < items.get(i))
					return null;

			}
			else
			{
				return null;
			}
		}
		return toBeConsumed;
	}

	public static Set<ItemStack> getStacksByItemClass(IInventory invIn, Class<?> class1)
	{
		Set<ItemStack> stacks = new HashSet<ItemStack>();
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
		Set<ItemStack> food = getStacksByItemClass(inventory, ItemFood.class);
		float runningTotal = 0;
		for(ItemStack foodItem  : food)
		{
			runningTotal += ((ItemFood)foodItem.getItem()).getSaturationModifier(foodItem) * foodItem.getCount();
		}
		return runningTotal;
	}
}
