package com.joshycode.improvedvils.capabilities.village;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;

public class VillageCapability implements IVillageCapability{

	public static final String VILLAGE_NBT_KEY = "improved-vils:village";
	
	private UUID id;
	private HashMap<String, Integer> teamRepuatations;
	private HashMap<UUID, Double> playerRepuationMeans;
	private String currentTeam;
	
	public VillageCapability() 
	{ 
		this.teamRepuatations = new HashMap(); 
		this.playerRepuationMeans = new HashMap();
	}
	
	@Override
	public NBTTagCompound serializeNBT() 
	{
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(VILLAGE_NBT_KEY + "ID", this.id.toString());
		ByteArrayOutputStream teamReputations = new ByteArrayOutputStream();
		ByteArrayOutputStream playerReputationMeans = new ByteArrayOutputStream();
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(teamReputations);
			oos.writeObject(this.teamRepuatations);
			oos.close();
			nbt.setByteArray(VILLAGE_NBT_KEY + "TR", teamReputations.toByteArray());
			
			oos = new ObjectOutputStream(playerReputationMeans);
			oos.writeObject(this.playerRepuationMeans);
			oos.close();
			nbt.setByteArray(VILLAGE_NBT_KEY + "PR", playerReputationMeans.toByteArray());
		} catch (IOException e) { e.printStackTrace(); }
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) 
	{
		String stringId = nbt.getString(VILLAGE_NBT_KEY + "ID");
		this.id = UUID.fromString(stringId);
		ObjectInputStream ois;
		try 
		{
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(VILLAGE_NBT_KEY + "TR")));
			this.teamRepuatations = (HashMap<String, Integer>) ois.readObject();
			ois.close();
			
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(VILLAGE_NBT_KEY + "PR")));
			this.playerRepuationMeans = (HashMap<UUID, Double>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
	}

	@Override
	public UUID getUUID() 
	{
		return this.id;
	}

	/**
	 * Should only be called once - during attach event!
	 */
	@Override
	public void setUUID(UUID id) 
	{
		this.id = id;
	}

	@Override
	public int getTeamReputation(Team team) 
	{
		if(this.teamRepuatations.containsKey(team.getName()))
		{
			return this.teamRepuatations.get(team.getName());
		}
		return 0;
	}

	@Override
	public int getCurrentTeamReputation() 
	{
		if(this.currentTeam != null && this.teamRepuatations.containsKey(this.currentTeam))
		{
			return this.teamRepuatations.get(currentTeam);
		}
		return 0;
	}

	@Override
	public String getTeam() 
	{
		return this.currentTeam;
	}

	@Override
	public void setTeam(Team team) 
	{
		this.currentTeam = team.getName();
	}

	@Override
	public void changeTeamReputation(Team team, int reputationChange)
	{
		if(this.teamRepuatations.containsKey(team.getName()))
		{
			int rep = this.teamRepuatations.get(team.getName());
			this.teamRepuatations.put(team.getName(), rep + reputationChange);
		}
		else
		{
			this.teamRepuatations.put(team.getName(), reputationChange);
		}
	}

	@Override
	public void setTeamReputation(Team team, int reputation) 
	{
		this.teamRepuatations.put(team.getName(), reputation);
	}

	@Override
	public double getPlayerMeanReputation(UUID playerId) {
		return this.playerRepuationMeans.get(playerId);
	}

	@Override
	public void setMeanPlayerReputation(UUID playerId, double reputation) {
		this.playerRepuationMeans.put(playerId, reputation);
	}
}
