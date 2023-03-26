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
	private BlockPos commObj;
	private boolean movingIndoors;

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
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bm", movingIndoors);
		
		if(this.guardObj == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", this.guardObj.toLong());
		
		if(this.commObj == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bc", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bc", this.commObj.toLong());
			
        if(this.player != null)	
			nbt.setString(VILPLAYER_NBT_KEY, this.player.toString());
        return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.isHungry = nbt.getBoolean(VILPLAYER_NBT_KEY + "b");
		this.isReturning = nbt.getBoolean(VILPLAYER_NBT_KEY + "br");
		this.movingIndoors = nbt.getBoolean(VILPLAYER_NBT_KEY + "bm");
		
		long l = nbt.getLong(VILPLAYER_NBT_KEY + "bp");
		if(l == Long.MAX_VALUE)
			this.guardObj = null;
		else
			this.guardObj = BlockPos.fromLong(l);
		
		long lc = nbt.getLong(VILPLAYER_NBT_KEY + "bc");
		if(lc == Long.MAX_VALUE)
			this.commObj = null;
		else
			this.commObj = BlockPos.fromLong(lc);
		
		String s = nbt.getString(VILPLAYER_NBT_KEY);
		if(!s.isEmpty()) {
			this.player = UUID.fromString(s);
			System.out.println("UUID-toString; " + s + "	recovered from nbt");
		}
	}

	@Override
	public void setHungry(boolean isHungry) {this.isHungry = isHungry; System.out.println("isHUngery .. " + isHungry); }

	@Override
	public boolean getHungry() {return this.isHungry; }

	@Override
	public void setGuardBlockPos(BlockPos pos) {
		this.guardObj = pos;
	}

	@Nullable
	@Override
	public BlockPos getGuardBlockPos() {
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

	@Override
	public void setCommBlock(BlockPos posIn) {
		this.commObj = posIn;
	}

	@Nullable
	@Override
	public BlockPos getCommBlockPos() {
		return this.commObj;
	}

	@Override
	public boolean isMovingIndoors() {
		return this.movingIndoors;
	}

	@Override
	public void setMovingIndoors(boolean b) {
		this.movingIndoors = b;
	}


}