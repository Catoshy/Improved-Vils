package com.joshycode.improvedvils.entity.ai;

import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.EnumFacing;

public class VillagerAISwimming extends EntityAISwimming {

	protected static final UUID MODIFIER_UUID = UUID.fromString("3ccaa8b7-9872-4ae3-9401-d72c4a0ce244");
	private static final AttributeModifier MODIFIER = (new AttributeModifier(MODIFIER_UUID, "Wading jump boost", +0.5D, 0)).setSaved(false);
	private EntityLiving entityLiving;

	public VillagerAISwimming(EntityLiving entityIn) 
	{
		super(entityIn);
		this.entityLiving = entityIn;
	}
	
	@Override
	public boolean shouldExecute()
	{
		if(!this.entityLiving.isInsideOfMaterial(Material.WATER))
		{
			IBlockState blockUnderneath =  this.entityLiving.getEntityWorld().getBlockState(this.entityLiving.getPosition().down());
			EnumFacing entityDir = EnumFacing.getDirectionFromEntityLiving(this.entityLiving.getPosition().down(), entityLiving);
			if(blockUnderneath.isSideSolid(this.entityLiving.getEntityWorld(), this.entityLiving.getPosition().down(), entityDir))
				return false;
		}
		return super.shouldExecute();
	}
}
