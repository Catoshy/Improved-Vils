package com.joshycode.improvedmobs.capabilities.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public final class ImprovedVilCapability implements IImprovedVilCapability{
	public static final String VILPLAYER_NBT_KEY = "improved-vils:player";
	
	public ImprovedVilCapability() {}

	private UUID player;	
	private boolean isHungry;
	private BlockPos guardObj;
	private boolean isReturning;

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
		nbt.setBoolean(VILPLAYER_NBT_KEY + "b", isHungry); /*b for boolean!*/
		nbt.setBoolean(VILPLAYER_NBT_KEY + "br", isReturning);
		
		if(this.guardObj == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", this.guardObj.toLong());
			
        if(this.player != null)	
			nbt.setString(VILPLAYER_NBT_KEY, this.player.toString());
        return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.isHungry = nbt.getBoolean(VILPLAYER_NBT_KEY + "b");
		this.isReturning = nbt.getBoolean(VILPLAYER_NBT_KEY + "br");
		
		long l = nbt.getLong(VILPLAYER_NBT_KEY + "bp");
		if(l == Long.MAX_VALUE)
			this.guardObj = null;
		else
			this.guardObj = BlockPos.fromLong(l);
		
		String s = nbt.getString(VILPLAYER_NBT_KEY);
		if(!s.isEmpty()) {
			this.player = UUID.fromString(s);
			System.out.println("UUID-toString; " + s + "	recovered from nbt");
		}
	}

	@Override
	public void setHungry(boolean isHungry) {this.isHungry = isHungry; }

	@Override
	public boolean getHungry() {return this.isHungry; }

	@Override
	public void setGuardBlockPos(BlockPos pos) {
		this.guardObj = pos;
	}
	
	@Override
	public void clearGuardPos() {
		this.guardObj = null;
	}

	@Nullable
	@Override
	public BlockPos getBlockPos() {
		return this.guardObj;
	}

	@Override
	public boolean isReturning() {
		return this.isReturning;
	}

	@Override
	public void setReturning(boolean isReturning) {
		this.isReturning = isReturning;
	}


}