package com.joshycode.improvedvils.entity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InventoryHands implements IInventory {

	private EntityLiving entity;
	private String inventoryTitle;
	private final int slotsCount;
	private boolean hasOwnName;
    private List<IInventoryChangedListener> changeListeners;

	public InventoryHands(EntityLiving e, String title, boolean customName)
	{
		this.entity = e;
		this.inventoryTitle = title;
        this.slotsCount = 6;
        this.hasOwnName = customName;
	}
	
	public EntityLiving getEntity() 
	{
		return entity;
	}
	
    @SideOnly(Side.CLIENT)
    public InventoryHands(EntityLiving e, String string)
    {
        this(e, string, true);
    }
	
    /**
     * Add a listener that will be notified when any item in this inventory is modified.
     */
    public void addInventoryChangeListener(IInventoryChangedListener listener)
    {
        if (this.changeListeners == null)
        {
            this.changeListeners = Lists.<IInventoryChangedListener>newArrayList();
        }

        this.changeListeners.add(listener);
    }
    
    /**
     * removes the specified IInvBasic from receiving further change notices
     */
    public void removeInventoryChangeListener(IInventoryChangedListener listener)
    {
        this.changeListeners.remove(listener);
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty()
    {
        if (this.changeListeners != null)
        {
            for (int i = 0; i < this.changeListeners.size(); ++i)
            {
                ((IInventoryChangedListener)this.changeListeners.get(i)).onInventoryChanged(this);
            }
        }
    }
    
	@Override
	public String getName() 
	{
		return this.inventoryTitle;
	}

	@Override
	public boolean hasCustomName() 
	{
		return this.hasOwnName;
	}
	
    public void setCustomName(String inventoryTitleIn)
    {
        this.hasOwnName = true;
        this.inventoryTitle = inventoryTitleIn;
    }

	@Override
	public ITextComponent getDisplayName() 
	{
		return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
	}

	@Override
	public int getSizeInventory() 
	{
		return this.slotsCount;
	}

	@Override
	public boolean isEmpty() 
	{
		/** 
		 * NOT for all tests so that for each slot a result of 0 means slot is empty
		 * flag is 0
		 * if any slot is not empty flag will OR to 1
		 * return NOT of flag because the asker is expecting 1 to mean empty
		 * */
		boolean flag = false;
		flag |= !this.entity.getHeldItemMainhand().isEmpty();
		flag |= !this.entity.getHeldItemOffhand().isEmpty();
		flag |= !this.entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty();
		flag |= !this.entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty();
		flag |= !this.entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty();
		flag |= !this.entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty();
		return !flag;
	}

	@Override
	public ItemStack getStackInSlot(int index) 
	{
		switch(index)
		{
			case 0:
				return this.entity.getHeldItemMainhand();
			case 1:
				return this.entity.getHeldItemOffhand();
			case 2:
				return this.entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
			case 3:
				return this.entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
			case 4:
				return this.entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			case 5:
				return this.entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) 
	{
		return index >= 0 && index < 6 && !getStackInSlot(index).isEmpty() && count > 0 ? getStackInSlot(index).splitStack(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) 
	{
		ItemStack i = getStackInSlot(index);
		if(i.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		switch(index)
		{
			case 0:
				this.entity.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				break;
			case 1:
				this.entity.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
				break;
			case 2:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);
				break;
			case 3:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);
				break;
			case 4:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
				break;
			case 5:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
				break;
		}
		return i;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) 
	{
		switch(index)
		{
			case 0:
				this.entity.setHeldItem(EnumHand.MAIN_HAND, stack);
				break;
			case 1:
				this.entity.setHeldItem(EnumHand.OFF_HAND, stack);
				break;
			case 2:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.FEET, stack);
				break;
			case 3:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, stack);
				break;
			case 4:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);
				break;
			case 5:
				this.entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack);
				break;
		}
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) 
	{
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) 
	{
		return true;
	}

	@Override
	public int getField(int id) 
	{
		return 0;
	}

	@Override
	public void setField(int id, int value) 
	{
	}

	@Override
	public int getFieldCount() 
	{
		return 0;
	}

	@Override
	public void clear() 
	{
		for(int i = 0; i < 6; i++)
			setInventorySlotContents(i, ItemStack.EMPTY);
	}

	public void setEquipmentSlot(EntityEquipmentSlot slot, ItemStack itemstack1) 
	{
		this.entity.setItemStackToSlot(slot, itemstack1);
	}

	public ItemStack getStackInSlot(EntityEquipmentSlot armourSlot) {
		return this.entity.getItemStackFromSlot(armourSlot);
	}
}
