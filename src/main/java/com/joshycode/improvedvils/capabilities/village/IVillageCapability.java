package com.joshycode.improvedvils.capabilities.village;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.common.util.INBTSerializable;

public interface IVillageCapability extends INBTSerializable<NBTTagCompound> {

	public UUID getUUID();
	public void setUUID(UUID id);
	public double getPlayerMeanReputation(UUID playerId);
	public void setMeanPlayerReputation(UUID playerId, double reputation);
	public int getTeamReputation(Team team);
	public int getCurrentTeamReputation();
	public void setLastTeamDealing(int time); //TODO
	public int lastTeamDealing();
	@Nullable
	public String getTeam();
	public void setTeam(@Nullable Team teamName);
	void setTeamReputation(Team team, int reputation);
	void changeTeamReputation(Team team, int reputation);
}
