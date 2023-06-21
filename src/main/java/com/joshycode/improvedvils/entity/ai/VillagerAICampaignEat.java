package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class VillagerAICampaignEat extends EntityAIBase implements IInventoryChangedListener {

	private final EntityVillager entityHost;
	private int hungerCooldown;
	private int checkTick;

	public VillagerAICampaignEat(EntityVillager entityIn)
	{
		this.entityHost = entityIn;
		this.hungerCooldown = 600;
		this.entityHost.getVillagerInventory().addInventoryChangeListener(this);
		this.setMutexBits(0);
	}

	@Override
	public boolean shouldExecute()
	{
		if(InventoryUtil.doesInventoryHaveItem(this.entityHost.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0)
			return true;

		return false;
	}

	@Override
	public void updateTask()
	{
		if(this.hungerCooldown > 0)
		{
			this.hungerCooldown--;
			if(this.hungerCooldown <= 0)
			{
				checkFood();
			}
		}
	}

	@Override
	public void onInventoryChanged(IInventory invBasic)
	{
		if(isHungry() && this.checkTick != this.entityHost.ticksExisted)
			checkFood();
	}

	private void checkFood()
	{
		this.checkTick = this.entityHost.ticksExisted;
		ItemStack stack = InventoryUtil.findAndDecrItem(entityHost.getVillagerInventory(), ItemFood.class);
		if(stack != null)
		{
			setHungry(false);
			float saturation = ((ItemFood)stack.getItem()).getSaturationModifier(stack);
			this.hungerCooldown = (int) (saturation * (40000 * ConfigHandler.dailyBread)); /* .1F sat should mean 10 seconds of "fullness". 20 ticks/sec * 10 seconds * 1/.1F sat(10) */
		}
		else
		{
			setHungry(true);
		}
	}

	private boolean isHungry()
	{
		try
		{
			return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException e) {}
		return false;
	}

	private void setHungry(boolean b)
	{
		try
		{
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setHungry(b);
		} catch (NullPointerException e) {}
	}
}
