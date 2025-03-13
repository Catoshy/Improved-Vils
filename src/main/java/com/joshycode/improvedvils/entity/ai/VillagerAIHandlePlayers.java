package com.joshycode.improvedvils.entity.ai;

import java.util.List;
import java.util.UUID;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.Pair;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;

public class VillagerAIHandlePlayers extends EntityAIBase {

	private EntityVillager villager;
	private Village village;
	private int fealtyHolding;
	private int timerVal;
	
	public VillagerAIHandlePlayers(EntityVillager villager) 
	{
		super();
		this.villager = villager;
		this.fealtyHolding = 5;
		timerVal = 100;
	}

	@Override
	public boolean shouldExecute()
	{
		if(this.villager.getEntityWorld().isRemote) return false;
		
		Village village = villager.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(villager), 0);
		UUID homeId = this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHomeVillageID();
		if(village != null && homeId == null)
		{
			this.village = village;
			return true;
		}
		if(this.villager.ticksExisted < 20 || village == null ||
				(village.equals(this.village) && this.villager.getRNG().nextInt(this.getTimer()) != 0))
		{
			return false;
		}

		this.village = village;
		return true;
	}

	private int getTimer() 
	{
		if(this.timerVal == 500) return this.timerVal;
		if(this.village != null)
		{
			boolean flag = false;
			for(UUID playerId : this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getKnownPlayers())
			{
				if(this.village.getPlayerReputation(playerId) == 0)
					flag = true;
			}
			this.timerVal = flag ? 100 : 500;
		}
		return this.timerVal;
	}

	@Override
	public void startExecuting()
	{
		IImprovedVilCapability vilCap = this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		IVillageCapability villageCap = this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(vilCap.getHomeVillageID() == null)
		{
			setHomeIfSame(vilCap, villageCap);		
		}
		else if(!VillagerPlayerDealMethods.isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID()))
		{
			float playerRep = vilCap.getPlayerReputation(vilCap.getPlayerId());
			if(this.fealtyHolding > 0 && vilCap.getPlayerId() != null && playerRep > 0)
			{
				this.fealtyHolding--;
				return;
			}
			setHomeIfSame(vilCap, villageCap);
		}
		else
		{
			this.fealtyHolding = 5;
		}
		this.updateFromVillageReputation(vilCap, villageCap);
	}

	private void setHomeIfSame(IImprovedVilCapability vilCap, IVillageCapability villageCap) 
	{
		if(vilCap.getTeam().isEmpty() || vilCap.getTeam().equals(villageCap.getTeam()))
		{
			if(ConfigHandler.debug)
				Log.info("setting home village ID %s", villageCap.getUUID());
			vilCap.setHomeVillageID(villageCap.getUUID());
			vilCap.setTeam(villageCap.getTeam());
		}
	}

	public void updateFromVillageReputation(IImprovedVilCapability vilCap, IVillageCapability villageCap)
	{
		if(!VillagerPlayerDealMethods.isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID())) return;
	
		if(!villageCap.getTeam().equals(vilCap.getTeam()))
		{
			VilMethods.setTeam(this.villager, villageCap.getTeam());
		}
		for(UUID playerId : vilCap.getKnownPlayers())
		{
			if(ConfigHandler.debug)
				Log.info("updating village reputation for player UUID: %s", playerId);
			int playerReputation = this.village.getPlayerReputation(playerId);
			if(playerReputation > 5 || playerReputation < 0 || vilCap.getPlayerReputation(playerId) > 15F)
			{
				Pair<List<EntityVillager>, long[]> pair = VillagerPlayerDealMethods.getVillagePopulation(this.village, this.villager.getEntityWorld());
				List<EntityVillager> population = pair.a;
				long[] removeChunks = pair.b;
				
				int updateReputation = VillagerPlayerDealMethods.getVillageReputationFromMean(this.village, playerId, population) ;
				VillagerPlayerDealMethods.updateVillageReputation(this.villager.getEntityWorld(), this.village, playerId, updateReputation, population);
				VillagerPlayerDealMethods.putAwayChunks(this.villager.getEntityWorld(), removeChunks);
			}
		}
		VillagerPlayerDealMethods.updateVillageTeamReputation(this.village, this.villager.getEntityWorld().getScoreboard().getTeam(vilCap.getTeam()));
	}
}
