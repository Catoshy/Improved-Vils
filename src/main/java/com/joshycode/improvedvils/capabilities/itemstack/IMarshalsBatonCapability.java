package com.joshycode.improvedvils.capabilities.itemstack;

import java.util.Set;
import java.util.UUID;

import com.joshycode.improvedvils.util.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMarshalsBatonCapability extends INBTSerializable<NBTTagCompound> {

	public boolean addVillager(UUID entityid, int company, int platoon);
	public boolean removeVillager(UUID entityid);
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID);
	public Set<UUID>getVillagersSelected();
	public int selectedUnit();
	public void setCompany(int company);
	public void setPlatoon(int company, int platoon);
}
