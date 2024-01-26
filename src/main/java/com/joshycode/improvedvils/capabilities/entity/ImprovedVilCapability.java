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
	private BlockPos kitStorePos;
	private boolean movingIndoors;
	private boolean following;
	private boolean invListener;
	private boolean isDrinking;
	private boolean isRefilling;
	private int armourValue;
	private float attackValue;
	private boolean hasShield;
	private float foodSaturationValue;
	private boolean isMutinous;
	private boolean noAmmo;
	private boolean duty;


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
		nbt.setBoolean(VILPLAYER_NBT_KEY + "ba", noAmmo);
		nbt.setBoolean(VILPLAYER_NBT_KEY + "bdu", duty);
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
		
		if(this.kitStorePos == null)
			nbt.setLong(VILPLAYER_NBT_KEY + "bk", Long.MAX_VALUE);
		else
			nbt.setLong(VILPLAYER_NBT_KEY + "bk", this.kitStorePos.toLong());

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
		this.noAmmo = nbt.getBoolean(VILPLAYER_NBT_KEY + "ba");
		this.duty = nbt.getBoolean(VILPLAYER_NBT_KEY + "bdu");
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
		
		long lk = nbt.getLong(VILPLAYER_NBT_KEY + "bk");
		if(lk == Long.MAX_VALUE)
			this.kitStorePos = null;
		else
			this.kitStorePos = BlockPos.fromLong(lk);

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
	public IImprovedVilCapability setPlayerId(UUID id) { this.player = id; return this;}

	@Override
	public UUID getPlayerId() { return this.player; }

	@Override
	public IImprovedVilCapability setInvListener(boolean isListening) { this.invListener = isListening; return this;}

	@Override
	public boolean getListener() { return this.invListener; }

	@Override
	public IImprovedVilCapability setHungry(boolean isHungry) { this.isHungry = isHungry; return this;}

	@Override
	public boolean getHungry() { return this.isHungry; }

	@Override
	public IImprovedVilCapability setGuardBlockPos(BlockPos pos) { this.guardObj = pos; return this;}

	@Override
	public BlockPos getGuardBlockPos() { return this.guardObj; }

	@Override
	public boolean isReturning() { return this.isReturning; }

	@Override
	public IImprovedVilCapability setReturning(boolean isReturning) { this.isReturning = isReturning; return this;}

	@Override
	public IImprovedVilCapability setCommBlock(BlockPos posIn) { this.commObj = posIn; return this;}

	@Override
	public BlockPos getCommBlockPos() { return this.commObj; }

	@Override
	public boolean isMovingIndoors() { return this.movingIndoors; }

	@Override
	public IImprovedVilCapability setMovingIndoors(boolean b) { this.movingIndoors = b; return this;}

	@Override
	public boolean isFollowing() { return this.following; }

	@Override
	public IImprovedVilCapability setFollowing(boolean follow) { this.following = follow; return this;}

	@Override
	public boolean isDrinking() { return this.isDrinking; }

	@Override
	public IImprovedVilCapability setDrinking(boolean b) { this.isDrinking = b; return this;}

	@Override
	public BlockPos getFoodStorePos() { return this.foodStorePos; }

	@Override
	public IImprovedVilCapability setFoodStore(BlockPos pos) { this.foodStorePos = pos; return this;}
	
	@Override
	public BlockPos getKitStorePos() { return this.kitStorePos; }

	@Override
	public IImprovedVilCapability setKitStore(BlockPos pos) 
	{ 
		Log.info("setting Kits store pos in cap, %s", pos);
		this.kitStorePos = pos; 
		return this;
	}

	@Override
	public boolean getRefillingFood() { return this.isRefilling; }

	@Override
	public IImprovedVilCapability setRefilling(boolean b) { this.isRefilling = b; return this;}

	@Override
	public String getTeam() { return this.teamName; }

	@Override
	public IImprovedVilCapability setTeam(@Nullable String team) { this.teamName = team; return this;}

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
	public IImprovedVilCapability setPlayerReputation(UUID uniqueID, float f, int i)
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
		return this;
	}
	
	@Override
	public IImprovedVilCapability setPlayerReputationIfEstablished(UUID uniqueID, float f) 
	{
		if(this.playerReputations.get(uniqueID) != null)
		{
			this.playerReputations.get(uniqueID).setLeft(MathHelper.clamp(f, -30, 40.5f));
		}
		return this;
	}

	@Override
	public UUID getHomeVillageID() { return this.homeVillageId; }

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
	public IImprovedVilCapability setHomeVillageID(UUID uuid) { this.homeVillageId = uuid; return this;}

	@Override
	public int getArmourValue() { return this.armourValue; }

	@Override
	public float getAttackValue() { return this.attackValue; }

	@Override
	public boolean getShieldValue() { return this.hasShield; }

	@Override
	public float getFoodSaturation() { return this.foodSaturationValue; }

	@Override
	public IImprovedVilCapability setArmourValue(int armour) { this.armourValue = armour; return this;}

	@Override
	public IImprovedVilCapability setAttackValue(float attackVal) { this.attackValue = attackVal; return this;}

	@Override
	public IImprovedVilCapability setShield(boolean hasShield) { this.hasShield = hasShield; return this;}

	@Override
	public IImprovedVilCapability setSaturation(float foodSaturation) { this.foodSaturationValue = foodSaturation; return this;}

	@Override
	public Collection<UUID> getKnownPlayers() { return this.playerReputations.keySet(); }

	@Override
	public boolean isMutinous() { return this.isMutinous; }

	@Override
	public IImprovedVilCapability setMutinous(boolean setMutiny) { this.isMutinous = setMutiny; return this;}

	@Override
	public boolean getOutOfAmmo() {	return this.noAmmo; }

	@Override
	public IImprovedVilCapability setIsOutAmmo(boolean noAmmo) { this.noAmmo = noAmmo; return this;}

	@Override
	public boolean getActiveDuty() { return this.duty; }

	@Override
	public IImprovedVilCapability setActiveDuty(boolean activeDuty) { this.duty = activeDuty; return this; }

}