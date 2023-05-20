package com.joshycode.improvedvils.capabilities.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.entity.VillagerInvListener;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IImprovedVilCapability extends INBTSerializable<NBTTagCompound> {
	
	public void setPlayerId(UUID id);
	@Nullable
	public UUID getPlayerId();
	public void setGuardBlockPos(BlockPos posIn);
	@Nullable
	public BlockPos getGuardBlockPos();
	public void setCommBlock(BlockPos posIn);
	@Nullable
	public BlockPos getCommBlockPos();
	public void setInvListener(VillagerInvListener i);
	@Nullable
	public BlockPos getFoodStorePos();
	public void setFoodStore(BlockPos pos);
	@Nullable
	public VillagerInvListener getListener();
	public boolean getHungry();
	public void setHungry(boolean isHungry);
	public boolean isReturning();
	public void setReturning(boolean isReturning);
	public boolean isMovingIndoors();
	public void setMovingIndoors(boolean b);
	public boolean isFollowing();
	public void setFollowing(boolean follow);
	public boolean isDrinking();
	public void setDrinking(boolean b);
	public boolean getRefillingFood();
	public void setRefilling(boolean b);
	@Nullable
	public String getTeam();
	public void setTeam(Team team);
	public float getPlayerReputation(UUID uniqueID);
	public void setPlayerReputation(UUID uniqueID, float f);
	@Nullable
	public UUID getHomeVillageID();
	public int getHomeVillagePlayerReputationReference(UUID uniqueId);
	void setHomeVillagePlayerReputationReference(UUID uniqueID, int i);
	public void setHomeVillageID(UUID uuid);
	public int getArmourValue();
	public float getAttackValue();
	public boolean getShieldValue();
	public float getFoodSaturation();
	public void setArmourValue(int armour);
	public void setAttackValue(float attackVal);
	public void setShield(boolean hasShield);
	public void setSaturation(float foodSaturation);
}
