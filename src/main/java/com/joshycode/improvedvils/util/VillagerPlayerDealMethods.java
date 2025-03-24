package com.joshycode.improvedvils.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class VillagerPlayerDealMethods {

	public static final int EVIL = -15;
	public static final int HATED_THRESHOLD = -10;
	public static final int GOOD_THRESHOLD = 5;
	public static final int BAD_THRESHOLD = -5;
	public static final int UNBEARABLE_THRESHOLD = -7;

	/**
	 * Does not check for villager in home village!
	 * @param entityIn villager in question
	 * @param village in question
	 * @param player who's reputation will be modified
	 */
	private static void updateVillagerPlayerReputation(EntityVillager entityIn, Village village, int newReputation, UUID player)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		
		int villagerReferenceVillageReputation = vilCap.getHomeVillagePlayerReputationReference(player);
		int villageReputationChange = newReputation - villagerReferenceVillageReputation;
		float playerReputation = vilCap.getPlayerReputation(player);
	
		if(newReputation > playerReputation || villageReputationChange < 0)
		{
			if(ConfigHandler.debug)
				Log.info("newReputation:" + newReputation + ", villagerReferenceVillageReputation:" + villagerReferenceVillageReputation + 
						", villageReputationChange:" +  villageReputationChange + ", playerReputation:" + playerReputation);
			vilCap.setPlayerReputation(player, playerReputation + villageReputationChange, newReputation);
		}
		else
		{
			vilCap.setPlayerReputation(player, playerReputation, newReputation);
		}
	}
	
	private static void setVillagerReferenceReputation(EntityVillager entityIn, Village village, int referenceReputation, UUID player) 
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		float playerReputation = vilCap.getPlayerReputation(player);
		vilCap.setPlayerReputation(player, playerReputation, referenceReputation);
	}

	public static void updateVillageReputation(World world, Village village, UUID player, int updateReputation, List<EntityVillager> population)
	{
		for(EntityVillager entity : population)
		{
			//Update villager's opinion grounded on village reputation
			updateVillagerPlayerReputation(entity, village, updateReputation, player);
		}
		
		//Now that villagers' opinion has been updated, get a new average and use that as a reference, so the villagers' opinions don't keep rising or falling.
		int newReferenceReputation = getVillageReputationFromMean(village, player, population);
		for(EntityVillager entity : population)
		{
			setVillagerReferenceReputation(entity, village, newReferenceReputation, player);
		}
	}
	
	public static int getVillageReputationFromMean(Village village, UUID player, List<EntityVillager> population) 
	{
		double sumReputation = 0;
		int nowtimeReputation = village.getPlayerReputation(player);
		
		for(EntityVillager entity : population)
		{	
			//Get total reputation of player in question by all villagers here
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			sumReputation +=  vilCap.getPlayerReputation(player);
		}
		//Change the village reputation to reflect the average villager's opinion
		int newReputation = (int) (sumReputation / population.size());
		int difference = newReputation - nowtimeReputation;
		village.modifyPlayerReputation(player, difference);
		return newReputation;
	}
	
	public static int updateVillageTeamReputation(Village village, Team attackingPlayerTeam) 
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		
		if(villageCap.getTeam().isEmpty() || attackingPlayerTeam == null) return 0;
		
		boolean evilPlayer = false;
		float sum = 0;
		int denom = 0;
		

		for (String playerUsername : attackingPlayerTeam.getMembershipCollection()) 
		{
			@SuppressWarnings("deprecation")
			int reputation = village.getPlayerReputation(playerUsername);
			if(reputation < EVIL)
			{
				evilPlayer = true;
				break;
			}
			sum += reputation;
			denom++;
		}
		int teamReputation = (int) (sum / denom);
		if(evilPlayer)
		{	
			return HATED_THRESHOLD;
		}
		villageCap.setTeamReputation(attackingPlayerTeam, teamReputation);
		return teamReputation;
	}

	public static Pair<List<EntityVillager>, long[]> getVillagePopulation(Village village, World world)
	{
		BlockPos villageCenter = village.getCenter();
		AxisAlignedBB villageArea = new AxisAlignedBB(
				new BlockPos(villageCenter.getX() - village.getVillageRadius(), villageCenter.getY() - village.getVillageRadius(), villageCenter.getZ() - village.getVillageRadius()),
				new BlockPos(villageCenter.getX() + village.getVillageRadius(), villageCenter.getY() + village.getVillageRadius(), villageCenter.getZ() + village.getVillageRadius()));
		int i = 0;
		long[] loadedChunks = new long[32];
		out:
		for(int x = ((int)villageArea.minX)  << 4; x < ((int)villageArea.maxX << 4); x++)
			for(int z = ((int)villageArea.minZ)  << 4; z < ((int)villageArea.maxZ << 4); z++)
			{
				if(i >= 32)
					break out;
				loadedChunks[i] = x << 32 | z & 0xFFFFFFFF;
				world.getChunkProvider().provideChunk(x, z);
				i++;
			}
		
		List<EntityVillager> list = world.getEntitiesWithinAABB(EntityVillager.class, villageArea);
		Set<EntityVillager> set = new HashSet<EntityVillager>();
		for(EntityVillager e : list)
		{
			if(!isVillagerInHomeVillage(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getUUID(), e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHomeVillageID()))
			{
				set.add(e);
			}
		}
		list.removeAll(set);
		return new Pair<List<EntityVillager>, long[]>(list, loadedChunks);
	}
	
	public static void putAwayChunks(World world, long[] removeChunks) {
		if(!world.isRemote)
		{
			PlayerChunkMap chunkHandler =((WorldServer) world).getPlayerChunkMap();
			for(long l : removeChunks)
			{
				int x = (int)l >> 32;
				int z = (int)l;
				if(!chunkHandler.contains(x, z))
				{
					Chunk chunk = ((WorldServer) world).getChunkProvider().getLoadedChunk(x, z);
					if(chunk == null) continue;
						
					((WorldServer) world).getChunkProvider().queueUnload(chunk);
					if(ConfigHandler.debug)
						Log.info("Chunk queued for unload: %s", chunk);
				}
			}
		}
	}

	public static boolean getPlayerFealty(EntityPlayer player, EntityVillager entityIn)
	{
		Village village = entityIn.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(entityIn), 0);
		return doesPlayerHaveTeamFealty(player, entityIn, village) || doesPlayerHaveFealty(player, entityIn);
	}

	private static boolean doesPlayerHaveTeamFealty(EntityPlayer player, EntityVillager entityIn, Village village)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		Team villagerTeam = entityIn.getEntityWorld().getScoreboard().getTeam(vilCap.getTeam());
		if(villagerTeam != null && villagerTeam.isSameTeam(player.getTeam()))
		{
			if(vilCap.getPlayerReputation(player.getUniqueID()) > BAD_THRESHOLD && getWholeVillagePlayerRep(village, entityIn, player) > HATED_THRESHOLD)
			{
				return true;
			}
		}
		return false;
	}

	private static boolean doesPlayerHaveFealty(EntityPlayer player, EntityVillager entityIn)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(vilCap.getTeam().isEmpty())
		{
			int reference = vilCap.getHomeVillagePlayerReputationReference(player.getUniqueID());
			float reputation = vilCap.getPlayerReputation(player.getUniqueID());
			if(vilCap.getPlayerId().equals(player.getUniqueID()) && reference > BAD_THRESHOLD && reputation > BAD_THRESHOLD)
			{
				return reference > 0 || reputation > 0;
			}
		}
		return false;
	}

	/**
	 * Please be aware the method already checks for if the villager is actually in his own home village.
	 * @param village
	 * @param entityIn
	 * @param player
	 * @return
	 */
	private static int getWholeVillagePlayerRep(Village village, EntityVillager entityIn, EntityPlayer player)
	{
		if(village != null)
		{
			if(isVillagerInHomeVillage(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getUUID(), VilMethods.getHomeVillageId(entityIn)))
			{
				return village.getPlayerReputation(player.getUniqueID());
			}
		}
		return 0;
	}

	public static boolean isVillagerInHomeVillage(UUID villageID, @Nullable UUID vilsHomeVillageID)
	{
		return villageID.equals(vilsHomeVillageID);
	}

	public static void villageBadReputationChange(World entityWorld, Village village, EntityPlayer attackingPlayer)
	{
		int villageReputation = village.getPlayerReputation(attackingPlayer.getUniqueID());
		villageReputationChange(entityWorld, village, attackingPlayer, villageReputation);
	}
	
	public static void villageGoodReputationChange(World entityWorld, Village village, EntityPlayer player)
	{
		int villageReputation = village.getPlayerReputation(player.getUniqueID());
		if(ConfigHandler.debug)
			Log.info("Player is is null? %s", player.getTeam() == null);
		String villageTeam = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam();
		if(player.getTeam() == null || villageTeam.isEmpty() || player.getTeam().getName().equals(villageTeam))
		{
			villageReputationChange(entityWorld, village, player, villageReputation);
		}
	}

	private static void villageReputationChange(World entityWorld, Village village, EntityPlayer player, int villageReputation)
	{
		if(ConfigHandler.debug)
			Log.info("villageReputationChange, villageReputation is %s", villageReputation);
		
		Pair<List<EntityVillager>, long[]> pair = getVillagePopulation(village, entityWorld);
		List<EntityVillager> population = pair.a;
		long[] removeChunks = pair.b;		
		if(villageReputation < 0 || villageReputation > 5)
		{
			updateVillageReputation(entityWorld, village, player.getUniqueID(), villageReputation, population);
		}
		int villageTeamReputation = updateVillageTeamReputation(village, player.getTeam());
		if(player.getTeam() != null && player.getTeam().getName().equals(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam()))
		{	
			if(villageTeamReputation < HATED_THRESHOLD)
			{
				for(EntityVillager villager : population)
				{
					VilMethods.setTeam(villager, "");
				}
				village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).setTeam(null);
			}
			else if(villageTeamReputation < UNBEARABLE_THRESHOLD)
			{
				for(EntityVillager villager : population)
					villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMutinous(true);
			}
			else
			{
				for(EntityVillager villager : population)
					villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMutinous(false);
			}
		}
		putAwayChunks(entityWorld, removeChunks);
	}

	public static void updateArmourWeaponsAndFood(EntityVillager entity)
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		vilCap.setArmourValue(entity.getTotalArmorValue());
		float attackVal = 0;
		if(!entity.getHeldItemMainhand().isEmpty())
		{
			Collection<AttributeModifier> modifiers = entity.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			if(!modifiers.isEmpty())
			{
				AttributeModifier attackMod = modifiers.iterator().next();
				attackVal = (float) attackMod.getAmount();
			}
		}
		vilCap.setAttackValue(attackVal).setShield(entity.getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK).setSaturation(InventoryUtil.getFoodSaturation(entity.getVillagerInventory()));
	}

	public static void checkArmourWeaponsAndFood(EntityVillager entity, UUID playerEntityByUUID)
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		int armour = getArmourTally(entity);
		int prevArmour = vilCap.getArmourValue();

		float attackVal = 0;
		boolean hasShield  = false;
		if(!entity.getHeldItemMainhand().isEmpty())
		{
			Collection<AttributeModifier> modifiers = entity.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			if(!modifiers.isEmpty())
			{
				AttributeModifier attackMod = modifiers.iterator().next();
				attackVal = (float) attackMod.getAmount();
			}
		}
		if(!entity.getHeldItemOffhand().isEmpty())
		{
			hasShield = entity.getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK;
		}
			
		float prevAttackVal = vilCap.getAttackValue();
		boolean prevShield = vilCap.getShieldValue();
		float foodSaturation = InventoryUtil.getFoodSaturation(entity.getVillagerInventory());
		float prevSaturation = vilCap.getFoodSaturation();

		float wholeReputationChange = (armour - prevArmour) + (attackVal - prevAttackVal) + ((foodSaturation - prevSaturation) * .5f);

		if(hasShield != prevShield)
			if(!hasShield)
				wholeReputationChange -= 5;
			else
				wholeReputationChange += 5;
		if(ConfigHandler.debug)
			Log.info("changing reputation for player " + playerEntityByUUID.toString() + " by %s", wholeReputationChange);
		changePlayerReputation(entity, playerEntityByUUID, wholeReputationChange);
		vilCap.setArmourValue(armour).setAttackValue(attackVal).setShield(hasShield).setSaturation(foodSaturation);
	}
	
	private static int getArmourTally(EntityVillager entity) 
	{
		double armourTally = 0D;
		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values())
        {
            ItemStack itemstack = entity.getItemStackFromSlot(entityequipmentslot);
            if(!itemstack.isEmpty())
            {
            	Iterator<AttributeModifier> attributeIter = itemstack.getAttributeModifiers(entityequipmentslot).get(SharedMonsterAttributes.ARMOR.getName()).iterator();
            	while(attributeIter.hasNext())
            	{
            		armourTally += attributeIter.next().getAmount();
            	}
            }
        }
		return MathHelper.floor(armourTally);
	}

	public static void changePlayerReputation(EntityVillager entity, UUID playerEntityUUID, float wholeReputationChange) 
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(!vilCap.getTeam().isEmpty())
		{
			if(ConfigHandler.debug)
				Log.info("Villager team not empty, is %s", vilCap.getTeam());
			for(UUID playerId : vilCap.getKnownPlayers())
			{
				GameProfile playerProfile = entity.world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(playerId);
				ScorePlayerTeam playersTeam = entity.world.getScoreboard().getPlayersTeam(playerProfile.getName());
				if(playersTeam != null && playersTeam.getName().equals(vilCap.getTeam()))
				{
					int referenceReputation = vilCap.getHomeVillagePlayerReputationReference(playerEntityUUID);
					float nowReputation = vilCap.getPlayerReputation(playerEntityUUID);
					vilCap.setPlayerReputation(playerEntityUUID, nowReputation + (wholeReputationChange / playersTeam.getMembershipCollection().size()), referenceReputation);
				}
			}
		}
		else
		{
			int referenceReputation = vilCap.getHomeVillagePlayerReputationReference(playerEntityUUID);
			float nowReputation = vilCap.getPlayerReputation(playerEntityUUID);
			vilCap.setPlayerReputation(playerEntityUUID, nowReputation + wholeReputationChange, referenceReputation);
		}
	}

	public static void scheduleMutiny(Village village, EntityPlayer player, World world) 
	{
		// TODO Auto-generated method stub	
	}
}