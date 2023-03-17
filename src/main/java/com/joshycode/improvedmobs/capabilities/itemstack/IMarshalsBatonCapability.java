package com.joshycode.improvedmobs.capabilities.itemstack;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.joshycode.improvedmobs.util.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMarshalsBatonCapability extends INBTSerializable<NBTTagCompound> {

	public boolean addVillager(UUID entityid, int company, int platoon);
	public void removeVillager(UUID entityid);
	public Collection<UUID> getVillagersPlatoon(int platoon);
	public Collection<UUID> getVillagersCompany(int company);
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID);
}
