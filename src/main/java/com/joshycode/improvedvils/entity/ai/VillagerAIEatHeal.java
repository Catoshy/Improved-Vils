package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class VillagerAIEatHeal extends EntityAIBase {

	private final EntityVillager entityHost;
	private int hungerCooldown;
	
	public VillagerAIEatHeal(EntityVillager entity) 
	{
		super();
		this.entityHost = entity;
		this.hungerCooldown = 0;
	}

	@Override
	public boolean shouldExecute() 
	{
		if(this.hungerCooldown <= 0) 
		{
			if(InventoryUtil.getStacksByItem(this.entityHost.getVillagerInventory(), ItemFood.class).size() > 0 && this.entityHost.getHealth() < this.entityHost.getMaxHealth())
				return true;
		} 
		else 
		{
			this.hungerCooldown--;
		}
		return false;
	}
	
	public void startExecuting() 
	{
		ItemStack stack = InventoryUtil.findAndDecrItem(entityHost.getVillagerInventory(), ItemFood.class);
		if(stack != null) 
		{
			float saturation = ((ItemFood)stack.getItem()).getSaturationModifier(stack);
			this.entityHost.heal(saturation * 2);
		} 
		this.hungerCooldown = 300;
	}
	
	 public boolean shouldContinueExecuting() 
	 {
		 return false;
	 }
}
