package com.joshycode.improvedvils.capabilities.itemstack;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.util.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMarshalsBatonCapability extends INBTSerializable<NBTTagCompound> {

	public boolean addVillager(UUID entityid, int company, int platoon);
	public boolean removeVillager(UUID entityid);
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID);
	public Set<UUID>getVillagersSelected();
	@Nullable
	public BlockPos getPlatoonFoodStore(int company, int platoon2);
	public BlockPos getPlatoonKitStore(int company, int platoon);
	public int selectedUnit();
	public void setPlatoon(int company, int platoon);
	public void setPlatoonFoodStore(BlockPos pos);
	public void setPlatoonKitStore(BlockPos pos);
}
