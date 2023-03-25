package com.joshycode.improvedmobs.entity.ai;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerAIMoveIndoors extends EntityAIMoveIndoors {

	EntityVillager e;
	
	public VillagerAIMoveIndoors(EntityCreature entityIn) {
		super(entityIn);
		this.e = (EntityVillager) entityIn;
	}
	
	@Override
	public boolean shouldExecute() {
		IImprovedVilCapability cap = e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(cap != null) {
			if(InventoryUtil.doesInventoryHaveItem(this.e.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0  && !cap.getHungry())
				return false;
		}
		return super.shouldExecute();
	}

}
