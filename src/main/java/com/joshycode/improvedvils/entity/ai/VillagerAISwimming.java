package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillagerAISwimming extends EntityAISwimming {

	private EntityLiving entityLiving;

	public VillagerAISwimming(EntityLiving entityIn) 
	{
		super(entityIn);
		this.entityLiving = entityIn;
	}
	
	@Override
	public boolean shouldExecute()
	{
		if(!super.shouldExecute()) return false;
		
		if(!this.entityLiving.isInsideOfMaterial(Material.WATER))
		{
			IBlockState blockUnderneath =  this.entityLiving.getEntityWorld().getBlockState(this.entityLiving.getPosition().down()).getActualState(this.entityLiving.getEntityWorld(), this.entityLiving.getPosition().down());
			EnumFacing entityDir = EnumFacing.getDirectionFromEntityLiving(this.entityLiving.getPosition().down(), entityLiving);
			if(blockUnderneath.isSideSolid(this.entityLiving.getEntityWorld(), this.entityLiving.getPosition().down(), entityDir)
					&& !this.isTryingToLand())
				return false;
		}
		return true;
	}

	private boolean isTryingToLand() 
	{
		Path path = this.entityLiving.getNavigator().getPath();
		IBlockState blockFacing;
		BlockPos pos;
		EnumFacing entityFacing = this.entityLiving.getAdjustedHorizontalFacing();
		World world = this.entityLiving.getEntityWorld();
		
		pos = this.entityLiving.getPosition().offset(entityFacing);
		blockFacing = world.getBlockState(pos).getActualState(world, pos);
		boolean isNearSolid = blockFacing.isSideSolid(world, pos, EnumFacing.getDirectionFromEntityLiving(pos, this.entityLiving));
		
		if(isNearSolid)
			return this.entityLiving.getAttackTarget() == null;
		
		if(path != null && path.getCurrentPathLength() > path.getCurrentPathIndex() + 1) 
		{
			int i = path.getCurrentPathIndex();
			PathPoint next = path.getPathPointFromIndex(i + 1);
			PathPoint now = path.getPathPointFromIndex(i);
			
			if(ConfigHandler.debug)
				Log.info("Water Villager: has path, is in path (not near end), and next y = %s. now y = " +now.y, next.y);
			
			return now.y < next.y;
		}
		return false;
	}
}
