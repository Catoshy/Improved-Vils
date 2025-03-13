package com.joshycode.improvedvils.util;


import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathUtil {

	public static Vec3d findBlockInDirection(BlockPos start, BlockPos dest)
	{
		int entX = start.getX();
		int entY = start.getY();
		int entZ = start.getZ();

		int dX = dest.getX() - entX;
		int dY = dest.getY() - entY;
		int dZ = dest.getZ() - entZ;

		if(Math.abs(dX) > Math.abs(dZ))
		{
			double slope = (double)dZ / Math.abs((double)dX);
			double slopeY = (double)dY / Math.abs((double)dX);
			double runZ = entZ;
			double runY = entY;
			int runX = entX;
			int step = 1;
			if(dX < 0)
				step = -1;

			for(int i = 0; i < 32; i++)
			{
				runZ += slope;
				runY += slopeY;
				runX += step;

				BlockPos pos = new BlockPos(runX, runY, runZ);
				if(pos.getDistance(entX, entY, entZ) >= 16)
				{
					return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}
		else
		{
			double slope = (double)dX / Math.abs((double)dZ);
			double slopeY = (double)dY / Math.abs((double)dZ);
			double runX = entX;
			double runY = entY;
			int runZ = entZ;
			int step = 1;
			if(dZ < 0)
				step = -1;

			for(int i = 0; i < 32; i++)
			{
				runX += slope;
				runY += slopeY;
				runZ += step;
				
				BlockPos pos = new BlockPos(runX, runY, runZ);
				
				if(pos.getDistance(entX, entY, entZ) >= 16)
				{
					return new Vec3d(pos);
				}
			}
		}
		return null;
	}
	
	public static Vec3d findNavigableBlockInDirection(BlockPos start, BlockPos dest, EntityLiving entity, float offsetAngleDeg, boolean flipFacing)
	{
		if(start.getDistance(dest.getX(), dest.getY(), dest.getZ()) <= CommonProxy.GUARD_MAX_PATH)
				return new Vec3d(dest);
		
		int entX = start.getX();
		int entY = start.getY();
		int entZ = start.getZ();

		int dX = dest.getX() - entX;
		int dY = dest.getY() - entY;
		int dZ = dest.getZ() - entZ;

		if(Math.abs(dX) > Math.abs(dZ))
		{
			if(offsetAngleDeg != 0)
			{
				offsetAngleDeg *= (-1 + Math.round(entity.getRNG().nextFloat()) * 2) * (Math.PI / 180); //Random sign -/+ and convert to radians
				double tangentAng = Math.atan(dZ / dX) + offsetAngleDeg;
				dZ = Math.round((float) (Math.tan(tangentAng) * dX));
			}
			if(flipFacing)
				dX *= -1;
			double slope = (double)dZ / Math.abs((double)dX);
			double slopeY = (double)dY / Math.abs((double)dX);
			double runZ = entZ;
			double runY = entY;
			int runX = entX;
			int step = 1;
			if(dX < 0)
				step = -1;

			for(int i = 0; i < 32; i++)
			{
				runZ += slope;
				runY += slopeY;
				runX += step;

				BlockPos pos = new BlockPos(runX, runY, runZ);
				if(pos.getDistance(entX, entY, entZ) >= 8)
				{
					while(!entity.getNavigator().canEntityStandOnPos(pos) && pos.getY() != dest.getY())
					{
						if(dY > 0)
							runY += 1;
						else if(dY < 0)
							runY -= 1;
						pos = new BlockPos(runX, runY, runZ);
					}
					if(ConfigHandler.debug && entity.isInWater())
						Log.info("Water Villager, try blockpos %s", pos );
					return new Vec3d(pos);
				}
			}
		}
		else
		{
			if(offsetAngleDeg != 0)
			{
				offsetAngleDeg *= (-1 + Math.round(entity.getRNG().nextFloat()) * 2) * (Math.PI / 180); //Random sign -/+ and convert to radians
				double tangentAng = Math.atan(dX / dZ) + offsetAngleDeg;
				dX = Math.round((float) (Math.tan(tangentAng) * dZ));
			}
			if(flipFacing)
				dZ *= -1;
			double slope = (double)dX / Math.abs((double) dZ);
			double slopeY = (double)dY / Math.abs((double) dZ);
			double runX = entX;
			double runY = entY;
			int runZ = entZ;
			int step = 1;
			if(dZ < 0)
				step = -1;

			for(int i = 0; i < 32; i++)
			{
				runX += slope;
				runY += slopeY;
				runZ += step;
				
				BlockPos pos = new BlockPos(runX, runY, runZ);
				if(pos.getDistance(entX, entY, entZ) >= 8)
				{
					while(!entity.getNavigator().canEntityStandOnPos(pos) && pos.getY() != dest.getY())
					{
						if(dY > 0)
							runY += 1;
						else if(dY < 0)
							runY -= 1;
						pos = new BlockPos(runX, runY, runZ);
					}
					if(ConfigHandler.debug && entity.isInWater())
						Log.info("Water Villager, try blockpos %s", pos );
					return new Vec3d(pos);
				}
			}
		}
		return null;
	}
}
