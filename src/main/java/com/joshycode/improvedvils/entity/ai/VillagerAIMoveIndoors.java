package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIMoveIndoors extends EntityAIMoveIndoors {

	EntityVillager e;
	
	public VillagerAIMoveIndoors(EntityCreature entityIn) 
	{
		super(entityIn);
		this.e = (EntityVillager) entityIn;
	}
	
	@Override
	public boolean shouldExecute() 
	{
		if(InventoryUtil.doesInventoryHaveItem(this.e.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0  && !VilMethods.getHungry(e))
			return false;
		
		if(super.shouldExecute()) 
		{
			VilMethods.setMovingIndoors(this.e, true);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public boolean shouldContinueExecuting() 
	{
		if(!super.shouldContinueExecuting())
		{
			VilMethods.setMovingIndoors(this.e, false);
			return false;
		}
		return true;
	}
	
	@Override
	public void resetTask() 
	{
		VilMethods.setMovingIndoors(this.e, false);
	}

}
