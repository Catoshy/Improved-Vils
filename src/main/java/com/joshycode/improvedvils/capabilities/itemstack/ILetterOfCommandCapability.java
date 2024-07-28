package com.joshycode.improvedvils.capabilities.itemstack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ILetterOfCommandCapability extends INBTSerializable<NBTTagCompound> 
{
	public void attachInfo(Map<Integer, Long> food,  Map<Integer, Long> kit, Map<Integer, Set<UUID>> platoons);
	public Map<Integer, Long> getfoodStoreMap();
	public Map<Integer, Long> getkitStoreMap();
	public Map<Integer, Set<UUID>> getPlatoonMap();
}
