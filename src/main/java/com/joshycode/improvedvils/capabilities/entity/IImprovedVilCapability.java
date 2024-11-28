package com.joshycode.improvedvils.capabilities.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IImprovedVilCapability extends INBTSerializable<NBTTagCompound> {

	public IImprovedVilCapability setPlayerId(UUID id);
	@Nullable
	public UUID getPlayerId();
	public IImprovedVilCapability setGuardBlockPos(BlockPos posIn);
	@Nullable
	public BlockPos getGuardBlockPos();
	public IImprovedVilCapability setCommBlock(BlockPos posIn);
	@Nullable
	public BlockPos getCommBlockPos();
	public IImprovedVilCapability setTroopFaring(TroopCommands c);
	public TroopCommands getTroopFaring();
	@Nullable
	public BlockPos getFoodStorePos();
	public IImprovedVilCapability setFoodStore(BlockPos pos);
	public BlockPos getKitStorePos();
	public IImprovedVilCapability setKitStore(BlockPos pos);
	public BlockPos getLastDoor();
	public IImprovedVilCapability setLastDoor(BlockPos pos);
	public List<UUID> getKnownPlayers();
	public boolean getHungry();
	public IImprovedVilCapability setHungry(boolean isHungry);
	public boolean isReturning();
	public IImprovedVilCapability setReturning(boolean isReturning);
	public boolean isMovingIndoors();
	public IImprovedVilCapability setMovingIndoors(boolean b);
	public boolean isFollowing();
	public IImprovedVilCapability setFollowing(boolean follow);
	public boolean isDrinking();
	public IImprovedVilCapability setDrinking(boolean b);
	public boolean getRefillingFood();
	public IImprovedVilCapability setRefilling(boolean b);
	public boolean getListener();
	public IImprovedVilCapability setInvListener(boolean b);
	public String getTeam();
	public IImprovedVilCapability setTeam(String string);
	public float getPlayerReputation(UUID uniqueID);
	public IImprovedVilCapability setPlayerReputation(UUID uniqueID, float f, int i);
	@Nullable
	public UUID getHomeVillageID();
	public int getHomeVillagePlayerReputationReference(UUID uniqueId);
	public IImprovedVilCapability setHomeVillageID(UUID uuid);
	public int getArmourValue();
	public float getAttackValue();
	public boolean getShieldValue();
	public float getFoodSaturation();
	public IImprovedVilCapability setArmourValue(int armour);
	public IImprovedVilCapability setAttackValue(float attackVal);
	public IImprovedVilCapability setShield(boolean hasShield);
	public IImprovedVilCapability setSaturation(float foodSaturation);
	public IImprovedVilCapability setPlayerReputationIfEstablished(UUID player, float f);
	public boolean isMutinous();
	public IImprovedVilCapability setMutinous(boolean setMutiny);
	public boolean getOutOfAmmo();
	public IImprovedVilCapability setIsOutAmmo(boolean noAmmo);
	public boolean getActiveDuty();
	public IImprovedVilCapability setActiveDuty(boolean activeDuty);
}
