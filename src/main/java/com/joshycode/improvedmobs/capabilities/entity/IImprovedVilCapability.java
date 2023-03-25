package com.joshycode.improvedmobs.capabilities.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IImprovedVilCapability extends INBTSerializable<NBTTagCompound> {
	
	public void setPlayerId(UUID id);
	@Nullable
	public UUID getPlayerId();
	public void setHungry(boolean isHungry);
	public boolean getHungry();
	public void setGuardBlockPos(BlockPos posIn);
	@Nullable
	public BlockPos getBlockPos();
	public void clearGuardPos();
	public boolean isReturning();
	public void setReturning(boolean isReturning);
}
