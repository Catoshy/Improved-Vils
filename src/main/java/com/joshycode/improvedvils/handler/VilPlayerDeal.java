package com.joshycode.improvedvils.handler;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.Pair;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class VilPlayerDeal implements Runnable{

	private static final double SIX_BLOCKS_SQUARED = 36D;

	private WeakReference<EntityVillager> villagerRef;
	private WeakReference<EntityPlayerMP> playerRef;
	private WeakReference<World> worldRef;
	private World world;
	private EntityPlayerMP player;
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

	public VilPlayerDeal(int villagerId, EntityPlayerMP player, World world) {
		super();
		EntityVillager villager = (EntityVillager) world.getEntityByID(villagerId);
		this.villagerRef = new WeakReference<>(villager);
		this.playerRef = new WeakReference<>(player);
		this.worldRef = new WeakReference<>(world);
		this.vilsPlayerId = VilMethods.getPlayerId(villager);
	}

	@Override
	public void run()
	{
		if(ConfigHandler.debug)
			Log.info("Starting Gui handler on server thread ...");
		try
		{
			World world = this.worldRef.get();
			EntityPlayerMP player = this.playerRef.get();
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
			this.openVillagerGUI();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void init()
	{
		this.village = world.getVillageCollection().getNearestVillage(new BlockPos(villager), 0);

		if(this.village != null && ConfigHandler.debug)
		{
			Log.info("found village near villager %s", this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getUUID());
		}

		this.isVillagerInHomeVillage = isVillagerInHomeVillage();
		this.vilTeam = getVillagerTeam();
		this.villagerPlayerRep = getPlayerReputation();
		this.wholeVillagePlayerRep = getWholeVillagePlayerRep();
		this.villageCurrentTeamRep = getVillageTeamRep();

		if(vilsPlayerId == null)
		{
			noCurrentPlayer = true;
		}
		else
		{
			isCurrentPlayer = vilsPlayerId.equals(player.getUniqueID());
		}

		currentPlayerVillageRep = getCurrentPlayerWholeVillageRep();
		
		
		if(ConfigHandler.debug)
		{
			if(this.village != null)
			{
				Log.info("village Team %s", this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam());
				Log.info("wholeVillagePlayerRep %s", this.wholeVillagePlayerRep);
				Log.info("villageCurrentTeamRep %s", this.villageCurrentTeamRep);
				Log.info("currentPlayerVillageRep %s", this.currentPlayerVillageRep);
			}
			else
			{
				Log.info("Village is Null!");
			}
			Log.info("VilTeam Name: %s", VilMethods.getTeam(this.villager));
			if(this.vilTeam == null)
				Log.info("vilTeam is null");
			else
				Log.info("vilTeam %s", this.vilTeam);
			Log.info("villagerPlayerRep %s", this.villagerPlayerRep);
			Log.info("villager's Home Village %s", VilMethods.getHomeVillageId(villager));
			Log.info("isVillagerInHomeVillage %s", this.isVillagerInHomeVillage);
			Log.info("noCurrentPlayer %s", this.noCurrentPlayer);
			Log.info("isCurrentPlayer %s", this.isCurrentPlayer);
		}

	}

	public void openVillagerGUI()
	{

		double dist = this.player.getDistanceSq(this.villager);

		if(isTooFar(dist) || !isPlayerOfGoodStanding() || this.villager.isChild()) return;

		VillagerPlayerDealMethods.updateArmourWeaponsAndFood(this.villager);
		VilMethods.setPlayerId(this.player, this.villager);

		if(vilTeam == null || (this.village != null && this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam().isEmpty()))
			setPlayerVillageFealtyIfWorthy();
		//checkMutiny(); TODO

		int platoonAndEnlistedStanding = -2;
		int company = 0;

		ItemStack stack = InventoryUtil.getOnly1StackByItem(this.player.inventory, CommonProxy.ItemHolder.BATON);

		if(stack != null && VillagerPlayerDealMethods.getPlayerFealty(this.player, this.villager))
		{
			IMarshalsBatonCapability cap = this.player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
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
		this.player.openGui(ImprovedVils.instance, CommonProxy.VIL_GUI_ID, this.world, this.villager.getEntityId(), platoonAndEnlistedStanding, company);
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
				float currentPlayerRep = currentPlayerReputation();
				if((this.noCurrentPlayer || currentPlayerRep < 5) && this.wholeVillagePlayerRep > VillagerPlayerDealMethods.BAD_THRESHOLD)
				{
					if(ConfigHandler.debug)
						Log.info("no player, or current player not deserving of full loyalty. You are good enough (not bad) for villager %s", this.villager);
					return true;
				}
				if((this.villagerPlayerRep > currentPlayerRep && currentPlayerRep < VillagerPlayerDealMethods.BAD_THRESHOLD) ||
						(this.wholeVillagePlayerRep > 0  && this.currentPlayerVillageRep < VillagerPlayerDealMethods.UNBEARABLE_THRESHOLD))
				{
					if(ConfigHandler.debug)
						Log.info("not current player, there is a player for villager %s - but he is bad", this.villager);
					return true;
				}
			}
			else if(this.villagerPlayerRep > VillagerPlayerDealMethods.UNBEARABLE_THRESHOLD && this.wholeVillagePlayerRep > VillagerPlayerDealMethods.HATED_THRESHOLD)
			{
				if(ConfigHandler.debug)
					Log.info("player has the ear of villager %s, player is not too unbearable and hated", this.villager);
				return true;
			}
		}
		else if(this.player.getTeam().isSameTeam(this.vilTeam))
		{
			if(this.villagerPlayerRep > VillagerPlayerDealMethods.HATED_THRESHOLD && this.wholeVillagePlayerRep > VillagerPlayerDealMethods.HATED_THRESHOLD)
			{
				if(ConfigHandler.debug)
					Log.info("player is on the same team as villager %s, and is not hated", this.villager);
				return true;
			}
		}
		else
		{
			//TODO slight modification to this, should make separate wholeVilRep for either case (familiar player or evil  team)
			if((this.villagerPlayerRep > 0 || this.villageCurrentTeamRep < VillagerPlayerDealMethods.UNBEARABLE_THRESHOLD) && this.wholeVillagePlayerRep > VillagerPlayerDealMethods.HATED_THRESHOLD)
			{
				if(ConfigHandler.debug)
					Log.info("villager %s has a team, player is *not* ont it,  but the villager cannot bear his own team and  will let you see his Gui", this.villager);
				return true;
			}
		}
		return false;
	}

	private void setPlayerVillageFealtyIfWorthy()
	{
		//if(vilCap.getPlayerReputation(player.getUniqueID()) != 0) return;
		if(this.villagerPlayerRep >= 5 && this.player.getTeam() != null)
			VilMethods.setTeam(this.villager, this.player.getTeam().getName());
		if(this.isVillagerInHomeVillage)
		{
			if(this.wholeVillagePlayerRep < VillagerPlayerDealMethods.GOOD_THRESHOLD && this.wholeVillagePlayerRep >= VillagerPlayerDealMethods.BAD_THRESHOLD) return;

			if(ConfigHandler.debug)
				Log.info("player is  either good, ill, or team member - will be granted either fealty or hatred", this.player);

			this.setPlayerReputationAcrossVillage();
			this.checkAndTryToClaimVillageForTeam();
		}
		else
		{
			if(this.villagerPlayerRep == 0)
				VilMethods.setPlayerReputation(this.villager, this.player.getUniqueID(), .25F, 0);
		}
		if(ConfigHandler.debug)
			Log.info("setFealtyIfWorthy --- End.");
	}
	

	public void setPlayerReputationAcrossVillage()
	{
		int reputation = this.village.getPlayerReputation(this.player.getUniqueID());
		Pair<List<EntityVillager>, long[]> pair = VillagerPlayerDealMethods.getVillagePopulation(this.village, this.world);
		List<EntityVillager> population = pair.a;
		long[] removeChunks = pair.b;
	
		for(EntityVillager entity : population)
		{
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			if(vilCap.getPlayerReputation(this.player.getUniqueID()) == 0)
			{
				vilCap.setPlayerReputation(this.player.getUniqueID(), reputation, reputation);
			}
		}
		VillagerPlayerDealMethods.putAwayChunks(this.world, removeChunks);
	}

	//TODO
	private void checkMutiny() 
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(villageCurrentTeamRep <= 0 && !this.player.getTeam().getName().equals(villageCap.getTeam()) && world.rand.nextInt((300 / Math.abs(villageCurrentTeamRep))- 10) == 0)
		{
			VillagerPlayerDealMethods.scheduleMutiny(this.village, this.player, this.world);
		}
	}

	private void checkAndTryToClaimVillageForTeam()
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(this.player.getTeam() == null || !villageCap.getTeam().isEmpty()) return;

		if(ConfigHandler.debug)
			Log.info("looking into claiming for team .. %s", this.villager, this.player);

		int teamReputation = VillagerPlayerDealMethods.updateVillageTeamReputation(village, player.getTeam());
		
		if(teamReputation <= VillagerPlayerDealMethods.HATED_THRESHOLD) return;
		if(ConfigHandler.debug)
			Log.info("team is good  enough! will be set. reputation is - %s", teamReputation);

		villageCap.setTeam(this.player.getTeam());
		Pair<List<EntityVillager>, long[]> pair = VillagerPlayerDealMethods.getVillagePopulation(village, this.world);
		List<EntityVillager> population = pair.a;
		long[] removeChunks = pair.b;		
		for(EntityVillager villager : population)
		{
			if(!villager.isChild())
			{
				if(this.isVillagerInHomeVillage)
				{
					VilMethods.setTeam(villager, this.player.getTeam().getName());
				}
			}
		}
		VillagerPlayerDealMethods.putAwayChunks(this.world, removeChunks);
	}

	private boolean isVillagerInHomeVillage()
	{
		if(this.village == null) return false;
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
		if(this.isVillagerInHomeVillage)
		{
			return this.village.getPlayerReputation(this.player.getUniqueID());
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
		if(this.isVillagerInHomeVillage)
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
		}
		return 0;
	}

	private int getVillageTeamRep()
	{
		if(!this.isVillagerInHomeVillage) return 0;
		return this.village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getCurrentTeamReputation();
	}

	private static boolean isTooFar(double dist)
	{
		return dist >= SIX_BLOCKS_SQUARED;
	}
}
