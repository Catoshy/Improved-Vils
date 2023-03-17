package com.joshycode.improvedmobs.capabilities.entity;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IVilPlayerIdCapability extends INBTSerializable<NBTTagCompound> {
	
	public void setPlayerId(UUID id);
	public UUID getPlayerId();
}
