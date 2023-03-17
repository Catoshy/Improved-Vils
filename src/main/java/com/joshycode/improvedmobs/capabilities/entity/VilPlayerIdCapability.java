package com.joshycode.improvedmobs.capabilities.entity;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

public final class VilPlayerIdCapability implements IVilPlayerIdCapability{
	public static final String VILPLAYER_NBT_KEY = "improved-vils:player";
	
	public VilPlayerIdCapability() {
	}

	private UUID player;	

	@Override
	public void setPlayerId(UUID id) {
		this.player = id;
	}

	@Override
	public UUID getPlayerId() {
		return this.player;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
        if(this.player != null)	
			nbt.setString(VILPLAYER_NBT_KEY, this.player.toString());
        return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		String s = nbt.getString(VILPLAYER_NBT_KEY);
		if(!s.isEmpty()) {
			this.player = UUID.fromString(s);
			System.out.println("UUID-toString; " + s + "	recovered from nbt");
		}
	}


}