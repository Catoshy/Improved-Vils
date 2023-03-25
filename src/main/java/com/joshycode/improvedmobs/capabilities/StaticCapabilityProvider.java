package com.joshycode.improvedmobs.capabilities;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class StaticCapabilityProvider<C, S extends NBTBase> implements ICapabilitySerializable<S> {

	Capability<C> capability;
	C object;
	
	protected StaticCapabilityProvider(Capability<C> capability, C object) {
		this.capability = capability;
		this.object = object;
	}
	
	@Nonnull
	public static <C> StaticCapabilityProvider<C, NBTTagCompound> from(@Nonnull final Capability<C> cap, @Nonnull final C object) {
		return new StaticCapabilityProvider<C, NBTTagCompound>(cap, object);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return getCapability(capability, facing) != null;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == this.capability) {
			return (T) this.object;
		}
		return null;
	}

	@Override
	public S serializeNBT() {
		return (S) this.capability.writeNBT(getInstance(), null);
	}

	@Override
	public void deserializeNBT(S nbt) {
		this.capability.readNBT(getInstance(), null, nbt);
	}
	
	private C getInstance() {
		try {
			return this.object;
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
