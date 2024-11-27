package com.joshycode.improvedvils.entity.ai;

import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;

public class VillagerAISwimming extends EntityAISwimming {

	protected static final UUID MODIFIER_UUID = UUID.fromString("3ccaa8b7-9872-4ae3-9401-d72c4a0ce244");
	//TODO
	@SuppressWarnings("unused")
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
			if(blockUnderneath.isSideSolid(this.entityLiving.getEntityWorld(), this.entityLiving.getPosition().down(), entityDir)
					&& !this.isTryingToLand())
				return false;
		}
		return super.shouldExecute();
	}

	private boolean isTryingToLand() 
	{
		Path path = this.entityLiving.getNavigator().getPath();
		EnumFacing facing = this.entityLiving.getAdjustedHorizontalFacing();
		IBlockState blockFacing = this.entityLiving.getEntityWorld().getBlockState(this.entityLiving.getPosition().offset(facing));
		
		if(path == null || path.getCurrentPathLength() >= path.getCurrentPathIndex()) 
			return (blockFacing.isSideSolid(this.entityLiving.getEntityWorld(), this.entityLiving.getPosition().offset(facing), EnumFacing.UP)
					&& this.entityLiving.getAttackTarget() == null);
		
		int i = path.getCurrentPathIndex();
		PathPoint next = path.getPathPointFromIndex(i + 1);
		PathPoint now = path.getPathPointFromIndex(i);
		
		return now.y < next.y;
	}
}
