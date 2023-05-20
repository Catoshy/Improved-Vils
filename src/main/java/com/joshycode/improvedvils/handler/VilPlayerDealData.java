package com.joshycode.improvedvils.handler;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.ServerProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.Pair;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class VilPlayerDealData implements Runnable{
	
	private static final double SIX_BLOCKS_SQUARED = 36D;
	private static final int EVIL = -15;
	private static final int HATED_THRESHOLD = -10;
	private static final int GOOD_THRESHOLD = 5;
	private static final int BAD_THRESHOLD = -5;
	private static final int UNBEARABLE_THREASHOLD = -7;

	
	private WeakReference<EntityVillager> villagerRef;
	private WeakReference<EntityPlayer> playerRef;
	private WeakReference<World> worldRef;
	private World world;
	private EntityPlayer player;
	private EntityVillager villager;
	
	private Village village;
	private Team vilTeam;
	private final UUID vilsPlayerId;
	private float villagerPlayerRep;
	private int wholeVillagePlayerRep;
	private int currentPlayerVillageRep;
	private int villageCurrentTeamRep;
	private boolean noCurrentPlayer;
	private boolean isCurrentPlayer;
	private boolean isVillagerInHomeVillage;
	
	protected VilPlayerDealData(EntityVillager villager, EntityPlayer player, World world) {
		super();
		this.villagerRef = new WeakReference<>(villager);
		this.playerRef = new WeakReference<>(player);
		this.worldRef = new WeakReference<>(world);
		this.vilsPlayerId = VilMethods.getPlayerId(villager);
	}
	
	@Override
	public void run() 
	{
		World world = this.worldRef.get();
		EntityPlayer player = this.playerRef.get();
		if(world == null)
		{
			Log.warn("lost world opening Gui for player %s, returning", player);
			return;
		}
		if(player == null)
		{
			Log.warn("lost player opening Gui for player %s, returning", player);
			return;
		}
		EntityVillager villager = this.villagerRef.get();
		if(villager == null)
		{
			Log.warn("lost villager opening Gui for player %s, returning", player);
			return;
		}
		this.world = world;
		this.player = player;
		this.villager = villager;
		this.init();
	}
	
	public void init()
	{
		this.village = world.getVillageCollection().getNearestVillage(new BlockPos(villager), 0);
		
		this.vilTeam = getVillagerTeam();
		this.villagerPlayerRep = getPlayerReputation();
		this.wholeVillagePlayerRep = getWholeVillagePlayerRep();
		this.villageCurrentTeamRep = getVillageTeamRep(village);
		this.isVillagerInHomeVillage = isVillagerInHomeVillage();
		
		if(vilsPlayerId == null)
		{
			noCurrentPlayer = true;
		}
		else
		{
			isCurrentPlayer = vilsPlayerId.equals(player.getUniqueID());
		}
		
		currentPlayerVillageRep = getCurrentPlayerWholeVillageRep();
		
		this.openVillagerGUI();
	}

	public void openVillagerGUI() 
	{

		double dist = player.getDistanceSq(this.villager);
		
		if(isTooFar(dist) || !isPlayerOfGoodStanding() || this.villager.isChild()) return;
		
		ServerProxy.updateArmourWeaponsAndFood(this.villager, this.player);
		VilMethods.setPlayerId(this.player, this.villager);
		
		if(vilTeam == null || this.player.getTeam().isSameTeam(vilTeam))
			setPlayerVillagerFealtyIfWorthy();
		
		int platoonAndEnlistedStanding = -2;
		int company = 0;
		
		ItemStack stack = InventoryUtil.get1StackByItem(this.player.inventory, CommonProxy.ItemHolder.BATON);
		
		if(stack != null && ServerProxy.getPlayerFealty(this.player, this.villager))
		{
			IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
			if(cap != null) 
			{
				Pair<Integer, Integer> p = cap.getVillagerPlace(this.villager.getUniqueID());
				platoonAndEnlistedStanding = -1; /* Has the Baton but is not Enlisted*/
				
				if(p != null) 
				{
					platoonAndEnlistedStanding = p.a;
					company = p.b;
				}
			}
		} 
		/* platoonAndEnlistedStanding = -2; Does not have baton, cannot be Enlisted*/
		this.player.openGui(ImprovedVils.instance, 100, this.world, this.villager.getEntityId(), platoonAndEnlistedStanding, company);
	}
	
	@Nullable
	private Team getVillagerTeam() 
	{
		String s = VilMethods.getTeam(this.villager);
		if(s != null)
		{
			return this.villager.getEntityWorld().getScoreboard().getTeam(s);
		}
		return null;
	}
	
	private boolean isPlayerOfGoodStanding()
	{
		if(this.vilTeam == null) 
		{
			if(!this.isCurrentPlayer)
			{
				if(this.noCurrentPlayer && this.wholeVillagePlayerRep > BAD_THRESHOLD)
				{
					return true;
				}
				float currentPlayerRep = currentPlayerReputation();
				if((this.villagerPlayerRep >  currentPlayerRep && currentPlayerRep < BAD_THRESHOLD) ||
						(this.wholeVillagePlayerRep > 0  && this.currentPlayerVillageRep < UNBEARABLE_THREASHOLD))
				{
					return true;
				}
			}
			else if(this.villagerPlayerRep > UNBEARABLE_THREASHOLD && this.wholeVillagePlayerRep > HATED_THRESHOLD)
			{
				return true;
			}
		}
		else if(this.player.getTeam().isSameTeam(this.vilTeam))
		{
			if(this.villagerPlayerRep > HATED_THRESHOLD && this.wholeVillagePlayerRep > HATED_THRESHOLD)
				return true;
		}
		else
		{
			//TODO slight modification to this, should make separate wholeVilRep for either case (familiar player or evil  team) 
			if((this.villagerPlayerRep > 0 || this.villageCurrentTeamRep < UNBEARABLE_THREASHOLD) && this.wholeVillagePlayerRep > HATED_THRESHOLD)
				return true;
		}
		return false;
	}
	
	private void setPlayerVillagerFealtyIfWorthy() 
	{		
		//if(vilCap.getPlayerReputation(player.getUniqueID()) != 0) return;
		
		if(this.isVillagerInHomeVillage)
		{
			if(this.wholeVillagePlayerRep < GOOD_THRESHOLD && this.wholeVillagePlayerRep > 0) return;
			
			ServerProxy.setPlayerReputationAcrossVillage(this.world, village, this.player);
			checkAndTryToClaimVillageForTeam();
			VilMethods.setPlayerReputation(this.villager, this.player.getUniqueID(), wholeVillagePlayerRep);
		}
		else
		{
			VilMethods.setPlayerReputation(this.villager, this.player.getUniqueID(), .25F);
		}
	}
	
	private void checkAndTryToClaimVillageForTeam() 
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(wholeVillagePlayerRep < GOOD_THRESHOLD || this.player.getWorldScoreboard().getTeam(villageCap.getTeam()) != null) return;
		
		int teamReputation = villageCap.getTeamReputation(this.player.getTeam());
		boolean evilPlayer = false;
		
		if(teamReputation == 0)
		{
			float sum = 0;
			int denom = 0;
			Iterator<String> iterator = this.player.getTeam().getMembershipCollection().iterator();
			while(iterator.hasNext())
			{
				String playerUsername = iterator.next();
				@SuppressWarnings("deprecation")
				int reputation = village.getPlayerReputation(playerUsername); /* Must do something with GameProfiles and MC Server TODO*/
				if(reputation < EVIL)
				{
					evilPlayer = true;
					break;
				}
				sum += reputation;
				denom++;
			}
			teamReputation = (int) (sum / denom);
		}
		if(teamReputation < HATED_THRESHOLD || evilPlayer) return;
		villageCap.setTeam(this.player.getTeam());
		List<EntityVillager> population = ServerProxy.getVillagePopulation(village, this.world);
		for(EntityVillager villager : population)
		{
			if(!villager.isChild())
			{
				if(this.isVillagerInHomeVillage)
				{
					VilMethods.setTeam(villager, this.player.getTeam());
				}
			}
		}
	}
	
	private boolean isVillagerInHomeVillage() 
	{
		UUID villageID = this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getUUID();
		UUID vilsHomeVillageID = this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHomeVillageID();
		return villageID.equals(vilsHomeVillageID);
	}
	
	private float getPlayerReputation() 
	{
		return this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerReputation(this.player.getUniqueID());
	}
	
	/**
	 * Please be aware the method already checks for if the villager is actually in his own home village.
	 * @param village
	 * @param entityIn
	 * @param player
	 * @return
	 */
	private int getWholeVillagePlayerRep() 
	{	
		if(this.village != null && this.isVillagerInHomeVillage)
		{
			return village.getPlayerReputation(this.player.getUniqueID());
		}
		return 0;
	}
	
	private float currentPlayerReputation() 
	{
		UUID id = VilMethods.getPlayerId(this.villager);
		if(id != null)
		{
			return this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerReputation(id);
		}
		return 0;
	}
	
	private int getCurrentPlayerWholeVillageRep() 
	{
		if(!this.isCurrentPlayer)
		{
			if(this.vilsPlayerId != null)
				return this.village.getPlayerReputation(this.vilsPlayerId);
		}
		else
		{
			return this.wholeVillagePlayerRep;
		}
		return 0;
	}
	
	private static int getVillageTeamRep(Village village) 
	{
		return village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getCurrentTeamReputation();
	}
	
	private static boolean isTooFar(double dist) 
	{
		return dist >= SIX_BLOCKS_SQUARED;
	}
}
