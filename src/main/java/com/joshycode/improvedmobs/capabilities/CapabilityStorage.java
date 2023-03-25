package com.joshycode.improvedmobs.capabilities;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityStorage<T extends INBTSerializable<?>> implements IStorage<T> {

	@Override
	@Nullable
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
		if(instance instanceof INBTSerializable<?>) {
			return ((INBTSerializable<NBTTagCompound>)instance).serializeNBT(); //renamed and cast from vilcap to more generic type
		}
		return null;
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
		if(instance instanceof INBTSerializable<?>) {
			if(nbt instanceof NBTTagCompound) {
				((INBTSerializable<NBTTagCompound>)instance).deserializeNBT((NBTTagCompound) nbt);
			} else {
				throw new IllegalArgumentException("Cannot deserialize Villager-player relashionship capability from non-NBTTagCompound");
			}
		}
	}

}
