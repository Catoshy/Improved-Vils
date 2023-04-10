package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
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
		if(InventoryUtil.doesInventoryHaveItem(this.e.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0  && !VilCapabilityMethods.getHungry(e))
			return false;
		
		if(super.shouldExecute()) 
		{
			VilCapabilityMethods.setMovingIndoors(this.e, true);
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
			VilCapabilityMethods.setMovingIndoors(this.e, false);
			return false;
		}
		return true;
	}
	
	@Override
	public void resetTask() 
	{
		VilCapabilityMethods.setMovingIndoors(this.e, false);
	}

}
