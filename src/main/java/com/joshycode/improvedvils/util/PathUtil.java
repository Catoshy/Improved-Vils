package com.joshycode.improvedvils.util;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathUtil {

	@Nullable
	public
	static Vec3d findBlockInDirection(BlockPos start, BlockPos dest)
	{
		int entX = start.getX();
		int entY = start.getY();
		int entZ = start.getZ();

		int dX = dest.getX() - entX;
		int dZ = dest.getZ() - entZ;

		if(Math.abs(dX) > Math.abs(dZ))
		{
			double slope = dZ / Math.abs(dX);
			double runZ = entZ;
			int runX = entX;
			int step = 1;
			if(dX < 0)
				step = -1;

			for(int i = 0; i < 32; i++)
			{
				runZ += slope;
				runX += step;
				BlockPos pos = new BlockPos(runX, entY, runZ);
				if(pos.getDistance(entX, entY, entZ) >= 16)
				{
					return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}
		else
		{
			double slope = dX / Math.abs(dZ);
			double runX = entX;
			int runZ = entZ;
			int step = 1;
			if(dZ < 0)
				step = -1;

			for(int i = 0; i < 200; i++)
			{
				runX += slope;
				runZ += step;
				BlockPos pos = new BlockPos(runX, entY, runZ);
				if(pos.getDistance(entX, entY, entZ) >= 16)
				{
					return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}
		return null;
	}

}
