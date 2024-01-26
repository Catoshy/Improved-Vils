package com.joshycode.improvedvils.entity.ai;

import org.jline.utils.Log;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class VillagerAICampaignEat extends EntityAIBase implements IInventoryChangedListener {

	private static final String hungerCooldownInfo = ImprovedVils.MODID + ":hunger";
	private final EntityVillager entityHost;
	private final float dailyBread;
	private int hungerCooldown;
	private int checkTick;

	public VillagerAICampaignEat(EntityVillager entityIn, float dailyBread)
	{
		this.entityHost = entityIn;
		this.dailyBread = dailyBread;
		this.hungerCooldown = entityIn.getEntityData().getInteger(hungerCooldownInfo);
		if(this.hungerCooldown <= 0)
			this.hungerCooldown = 1;
		this.entityHost.getVillagerInventory().addInventoryChangeListener(this);
		this.setMutexBits(0);
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getDuty(this.entityHost))
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
			if(this.hungerCooldown % 20 == 0)
			{
				this.entityHost.getEntityData().setInteger(hungerCooldownInfo, this.hungerCooldown);
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
			this.hungerCooldown = (int) (saturation * (24000 / this.dailyBread / .6)); /* .1F sat should mean 10 seconds of "fullness". 20 ticks/sec * 10 seconds * 1/.1F sat(10) */
		}
		else
		{
			setHungry(true);
		}
	}

	private boolean isHungry()
	{
		return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
	}

	private void setHungry(boolean b)
	{
		this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setHungry(b);
	}
}
