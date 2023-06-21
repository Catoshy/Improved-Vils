package com.joshycode.improvedvils.capabilities.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.VillagerInvListener;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class ImprovedVilCapability implements IImprovedVilCapability{

	public static final String VILPLAYER_NBT_KEY = "improved-vils:player";

	public ImprovedVilCapability()
	{
		this.playerReputations = new HashMap<>();
	}

	private UUID player, homeVillageId;
	private HashMap<UUID, MutablePair<Float, Integer>> playerReputations;
	private String teamName;
	private boolean isHungry;
	private BlockPos guardObj;
	private boolean isReturning;
	private BlockPos commObj;
	private BlockPos foodStorePos;
	private boolean movingIndoors;
	private boolean following;
	private VillagerInvListener invListener;
	private boolean isDrinking;
	private boolean isRefilling;
	private int armourValue;
	private float attackValue;
	private boolean hasShield;
	private float foodSaturationValue;
	private boolean isMutinous;

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound nbt = new NBTTagCompound();

		nbt.setBoolean(VILPLAYER_NBT_KEY + "b", isHungry); /*b for boolean!*/
		nbt.setBoolean(VILPLAYER_NBT_KEY + "br", isReturning);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "brf", isRefilling);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bm", movingIndoors);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bf", following);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bd", isDrinking);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bSh", hasShield);
		nbt.setInteger(VILPLAYER_NBT_KEY + "iav", armourValue);
		nbt.setFloat(VILPLAYER_NBT_KEY + "fav", attackValue);
		nbt.setFloat(VILPLAYER_NBT_KEY + "ffs", foodSaturationValue);

		if(this.guardObj == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bp", this.guardObj.toLong());

		if(this.commObj == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bc", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bc", this.commObj.toLong());

		if(this.foodStorePos == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bs", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bs", this.foodStorePos.toLong());

        if(this.player != null)
			nbt.setString(VILPLAYER_NBT_KEY, this.player.toString());

        if(this.homeVillageId != null)
        	nbt.setString(VILPLAYER_NBT_KEY + "shv", this.homeVillageId.toString());

        if(this.teamName != null)
        	nbt.setString(VILPLAYER_NBT_KEY + "stn", this.teamName);

        ByteArrayOutputStream playerReputations = new ByteArrayOutputStream();
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(playerReputations);
			oos.writeObject(this.playerReputations);
			oos.close();
			nbt.setByteArray(VILPLAYER_NBT_KEY + "PR", playerReputations.toByteArray());
		} catch (IOException e) {e.printStackTrace();}

        return nbt;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		this.isHungry = nbt.getBoolean(VILPLAYER_NBT_KEY + "b");
		this.isReturning = nbt.getBoolean(VILPLAYER_NBT_KEY + "br");
		this.isRefilling = nbt.getBoolean(VILPLAYER_NBT_KEY + "brf");
		this.movingIndoors = nbt.getBoolean(VILPLAYER_NBT_KEY + "bm");
		this.following = nbt.getBoolean(VILPLAYER_NBT_KEY + "bf");
		this.isDrinking = nbt.getBoolean(VILPLAYER_NBT_KEY + "bd");
		this.hasShield = nbt.getBoolean(VILPLAYER_NBT_KEY + "bSh");
		this.armourValue = nbt.getInteger(VILPLAYER_NBT_KEY + "iav");
		this.attackValue = nbt.getFloat(VILPLAYER_NBT_KEY + "fav");
		this.foodSaturationValue = nbt.getFloat(VILPLAYER_NBT_KEY + "ffs");

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

		long lf = nbt.getLong(VILPLAYER_NBT_KEY + "bs");
		if(lf == Long.MAX_VALUE)
			this.foodStorePos = null;
		else
			this.foodStorePos = BlockPos.fromLong(lf);

		String s = nbt.getString(VILPLAYER_NBT_KEY);
		if(!s.isEmpty())
			this.player = UUID.fromString(s);

		String hvId = nbt.getString(VILPLAYER_NBT_KEY + "shv");
		if(!hvId.isEmpty())
			this.homeVillageId = UUID.fromString(hvId);

		String tn = nbt.getString(VILPLAYER_NBT_KEY + "stn");
		if(!tn.isEmpty())
			this.teamName = tn;

		ObjectInputStream ois;
		try
		{
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(VILPLAYER_NBT_KEY + "PR")));
			this.playerReputations = (HashMap<UUID, MutablePair<Float, Integer>>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {e.printStackTrace();}

		if(this.playerReputations == null)
		{
			this.playerReputations = new HashMap<>();
		}
	}

	@Override
	public void setPlayerId(UUID id)
	{
		//TODO
		this.player = id;
	}

	@Override
	public UUID getPlayerId() { return this.player; }

	@Override
	public void setInvListener(VillagerInvListener listenerIn) { this.invListener = listenerIn;}

	@Override
	public VillagerInvListener getListener() { return this.invListener; }

	@Override
	public void setHungry(boolean isHungry) { this.isHungry = isHungry; }

	@Override
	public boolean getHungry() { return this.isHungry; }

	@Override
	public void setGuardBlockPos(BlockPos pos) { this.guardObj = pos; }

	@Override
	public BlockPos getGuardBlockPos() { return this.guardObj; }

	@Override
	public boolean isReturning() { return this.isReturning; }

	@Override
	public void setReturning(boolean isReturning) { this.isReturning = isReturning; }

	@Override
	public void setCommBlock(BlockPos posIn) { this.commObj = posIn; }

	@Override
	public BlockPos getCommBlockPos() { return this.commObj; }

	@Override
	public boolean isMovingIndoors() { return this.movingIndoors; }

	@Override
	public void setMovingIndoors(boolean b) { this.movingIndoors = b; }

	@Override
	public boolean isFollowing() { return this.following; }

	@Override
	public void setFollowing(boolean follow) { this.following = follow; }

	@Override
	public boolean isDrinking() { return this.isDrinking; }

	@Override
	public void setDrinking(boolean b) { this.isDrinking = b; }

	@Override
	public BlockPos getFoodStorePos() { return this.foodStorePos; }

	@Override
	public void setFoodStore(BlockPos pos) { this.foodStorePos = pos; }

	@Override
	public boolean getRefillingFood() { return this.isRefilling; }

	@Override
	public void setRefilling(boolean b) { this.isRefilling = b; }

	@Override
	public String getTeam() { return this.teamName; }

	@Override
	public void setTeam(@Nullable String team) { this.teamName = team; }

	@Override
	public float getPlayerReputation(UUID uniqueID)
	{
		if(this.playerReputations.get(uniqueID) != null)
		{
			return this.playerReputations.get(uniqueID).getLeft();
		}
		return 0;
	}

	@Override
	public void setPlayerReputation(UUID uniqueID, float f, int i)
	{
		if(this.playerReputations.get(uniqueID) != null)
		{
			this.playerReputations.get(uniqueID).setLeft(MathHelper.clamp(f, -30, 40.5f));
			this.playerReputations.get(uniqueID).setRight(MathHelper.clamp(i, -30, 10));
		}
		else
		{
			MutablePair<Float, Integer> pair = new MutablePair<>();
			pair.setLeft(MathHelper.clamp(f, -30, 40.5f));
			pair.setRight(MathHelper.clamp(i, -30, 10));
			this.playerReputations.put(uniqueID, pair);
		}
	}
	
	@Override
	public void setPlayerReputationIfEstablished(UUID uniqueID, float f) 
	{
		if(this.playerReputations.get(uniqueID) != null)
		{
			this.playerReputations.get(uniqueID).setLeft(MathHelper.clamp(f, -30, 40.5f));
		}
	}

	@Override
	public UUID getHomeVillageID() 
	{
		if(ConfigHandler.debug)
			Log.info("homve village id for villager %s", this.homeVillageId);
		return this.homeVillageId; 
	}

	@Override
	public int getHomeVillagePlayerReputationReference(UUID uniqueID)
	{
		if(this.playerReputations.get(uniqueID) != null && this.playerReputations.get(uniqueID).getRight() != null)
		{
			return this.playerReputations.get(uniqueID).getRight();
		}
		return 0;
	}

	@Override
	public void setHomeVillageID(UUID uuid) { this.homeVillageId = uuid; }

	@Override
	public int getArmourValue() { return this.armourValue; }

	@Override
	public float getAttackValue() { return this.attackValue; }

	@Override
	public boolean getShieldValue() { return this.hasShield; }

	@Override
	public float getFoodSaturation() { return this.foodSaturationValue; }

	@Override
	public void setArmourValue(int armour) { this.armourValue = armour; }

	@Override
	public void setAttackValue(float attackVal) { this.attackValue = attackVal; }

	@Override
	public void setShield(boolean hasShield) { this.hasShield = hasShield; }

	@Override
	public void setSaturation(float foodSaturation) { this.foodSaturationValue = foodSaturation; }

	@Override
	public Collection<UUID> getKnownPlayers() { return this.playerReputations.keySet(); }

	@Override
	public boolean isMutinous() { return this.isMutinous; }

	@Override
	public void setMutinous(boolean setMutiny) { this.isMutinous = setMutiny; }
}