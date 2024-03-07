package com.joshycode.improvedvils.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilStateUpdate;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class VillagerPlayerDealMethods {

	private static final int DRAFTED = 1;
	private static final int NULL = 0;
	private static final int GUARDING = 1;
	private static final int FOLLOWING = 1;
	public static final int EVIL = -15;
	public static final int HATED_THRESHOLD = -10;
	public static final int GOOD_THRESHOLD = 5;
	public static final int BAD_THRESHOLD = -5;
	public static final int UNBEARABLE_THRESHOLD = -7;

	public static void setPlayerReputationAcrossVillage(World world, Village village, EntityPlayer player)
	{
		int reputation = village.getPlayerReputation(player.getUniqueID());
		List<EntityVillager> population = getVillagePopulation(village, world);

		for(EntityVillager entity : population)
		{
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			if(vilCap.getPlayerReputation(player.getUniqueID()) == 0)
			{
				vilCap.setPlayerReputation(player.getUniqueID(), reputation, reputation);
			}
		}
	}
	/**
	 * Does not check for villager in home village!
	 * @param entityIn villager in question
	 * @param village in question
	 * @param player who's reputation will be modified
	 */
	private static void updateVillagerPlayerReputation(EntityVillager entityIn, Village village, UUID player)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		
		int newVillageReputation = village.getPlayerReputation(player);
		int villagerReferenceVillageReputation = vilCap.getHomeVillagePlayerReputationReference(player);
		int villageReputationChange = newVillageReputation - villagerReferenceVillageReputation;
		float playerReputation = vilCap.getPlayerReputation(player);
		//If villager's perception of the player is higher than the mean it does not make sense to further raise his opinion
		//given that his opinion is one of the sources of the better reputation. Lower reputations always come from outside
		//the villager's personal experience
		if(newVillageReputation > playerReputation || villageReputationChange < 0)
		{
			vilCap.setPlayerReputation(player, playerReputation + villageReputationChange, newVillageReputation);
		}
		else
		{
			vilCap.setPlayerReputation(player, playerReputation, newVillageReputation);
		}
	}

	public static void updateFromVillageReputation(EntityVillager villager, Village village)
	{
		IImprovedVilCapability vilCap = villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(!isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID())) return;

		if(villageCap.getTeam() != null && !villageCap.getTeam().equals(vilCap.getTeam()))
		{
			vilCap.setTeam(villageCap.getTeam());
		}
		for(UUID playerId : vilCap.getKnownPlayers())
		{
			int playerReputation = village.getPlayerReputation(playerId);
			if(playerReputation > 5 || playerReputation < 0 || vilCap.getPlayerReputation(playerId) > 15F)
			{
				updateVillageReputationFromMean(villager.getEntityWorld(), village, playerId);
			}
		}
	}

	public static void updateVillageReputationFromMean(World world, Village village, UUID player)
	{
		//Get villagers at home here
		List<EntityVillager> population = getVillagePopulation(village, world);
		double sumReputation = 0;
		int nowtimeReputation = village.getPlayerReputation(player);
		
		for(EntityVillager entity : population)
		{
			//Update villager's opinion grounded on village reputation
			updateVillagerPlayerReputation(entity, village, player);
			
			//Get total reputation of player in question by all villagers here
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			sumReputation +=  vilCap.getPlayerReputation(player);
		}
		//Change the village reputation to reflect the average villager's opinion
		int difference = (int) (sumReputation/population.size()) - nowtimeReputation;
		village.modifyPlayerReputation(player, difference);
	}
	
	public static int updateVillageTeamReputation(Village village, EntityPlayer attackingPlayer) 
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		
		if(villageCap.getTeam() == null || attackingPlayer.getTeam() == null) return 0;
		
		boolean evilPlayer = false;
		float sum = 0;
		int denom = 0;
		
		for (String playerUsername : attackingPlayer.getTeam().getMembershipCollection()) 
		{
			@SuppressWarnings("deprecation")
			int reputation = village.getPlayerReputation(playerUsername); /* Must test multiplayer TODO*/
			if(ConfigHandler.debug)
				Log.info("Deprecated methods?! Village reputation by this String usrname is  %s", reputation);
			if(reputation < VillagerPlayerDealMethods.EVIL)
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
			return VillagerPlayerDealMethods.HATED_THRESHOLD;
		}
		villageCap.setTeamReputation(attackingPlayer.getTeam(), teamReputation);
		return teamReputation;
	}

	public static List<EntityVillager> getVillagePopulation(Village village, World world)
	{
		BlockPos villageCenter = village.getCenter();
		AxisAlignedBB villageArea = new AxisAlignedBB(
				new BlockPos(villageCenter.getX() - village.getVillageRadius(), villageCenter.getY() - village.getVillageRadius(), villageCenter.getZ() - village.getVillageRadius()),
				new BlockPos(villageCenter.getX() + village.getVillageRadius(), villageCenter.getY() + village.getVillageRadius(), villageCenter.getZ() + village.getVillageRadius()));
		int i = 0;
		for(int x = ((int)villageArea.minX)  << 4; x < ((int)villageArea.maxX << 4); x++)
			for(int z = ((int)villageArea.minZ)  << 4; z < ((int)villageArea.maxZ << 4); z++)
			{
				if(i++ > 32)
					break;
				world.getChunkProvider().provideChunk(x, z);
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
		return list;
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
			return vilCap.getPlayerReputation(player.getUniqueID()) > 0 && vilCap.getPlayerId().equals(player.getUniqueID());
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

	@Nullable
	private static Team getVillagerTeam(EntityVillager entityIn)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(vilCap.getTeam() != null)
		{
			String s = vilCap.getTeam();
			return entityIn.getEntityWorld().getScoreboard().getTeam(s);
		}
		return null;
	}

	public static boolean isVillagerInHomeVillage(UUID villageID, @Nullable UUID vilsHomeVillageID)
	{
		return villageID.equals(vilsHomeVillageID);
	}

	public static VilStateUpdate getUpdateGuiForClient(EntityVillager e, EntityPlayer player)
	{
		Vec3i vec = null; int guardStateVal = 0, followStateVal = 0, dutyStateVal = 0, enlistedCompany = 0, enlistedPlatoon = 0; boolean hungry = false;

		if(player.getUniqueID().equals(VilMethods.getPlayerId((EntityVillager) e)))
		{
			guardStateVal += VilMethods.getDuty(e) ? DRAFTED : NULL;
			hungry = VilMethods.getHungry(e);
			guardStateVal = hungry ? NULL : guardStateVal;

			if(guardStateVal == 0)
			{
				VilMethods.setFollowing((EntityVillager) e, false);
				VilMethods.setGuardBlock((EntityVillager) e, null);
			}

			followStateVal = guardStateVal;
			guardStateVal += VilMethods.getGuardBlockPos(e) != null ? GUARDING : NULL;
			followStateVal += VilMethods.getFollowing(e) ? FOLLOWING : NULL; 
			
			dutyStateVal = VilMethods.getDuty(e) ? 2 : 1;
			//dutyStateVal = InventoryUtil.doesInventoryHaveItem(player.inventory, ItemHolder.BATON) != 0 ? dutyStateVal : NULL;
			if(guardStateVal == 2)
				vec = VilMethods.getGuardBlockPos(e);
			
		}

		if(vec != null)
		{
			return new VilStateUpdate(guardStateVal, followStateVal, dutyStateVal, hungry, enlistedCompany, enlistedPlatoon, vec);
		}
		return new VilStateUpdate(guardStateVal, followStateVal, dutyStateVal, hungry, enlistedCompany, enlistedPlatoon);
	}

	public static void resetInvListeners(EntityVillager entity)
	{
		if(ConfigHandler.debug)
			Log.info("closing out inv changed listener %s", entity);
		entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(false);
	}

	public static void updateGuiForClient(EntityVillager entity, EntityPlayer playerEntityByUUID)
	{
		NetWrapper.NETWORK.sendTo(getUpdateGuiForClient(entity, playerEntityByUUID), (EntityPlayerMP) playerEntityByUUID);
	}
	
	public static void villageBadReputationChange(World entityWorld, Village village, EntityPlayer attackingPlayer)
	{
		int villageReputation = village.getPlayerReputation(attackingPlayer.getUniqueID());
		if(villageReputation < 0)
		{
			villageReputationChange(entityWorld, village, attackingPlayer, villageReputation);
		}
	}
	
	public static void villageGoodReputationChange(World entityWorld, Village village, EntityPlayer player)
	{
		int villageReputation = village.getPlayerReputation(player.getUniqueID());
		if(ConfigHandler.debug)
			Log.info("Player is is null? %s", player.getTeam() == null);
		String villageTeam = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam();
		if(player.getTeam() == null || villageTeam == null || player.getTeam().getName().equals(villageTeam))
		{
			villageReputationChange(entityWorld, village, player, villageReputation);
		}
	}

	private static void villageReputationChange(World entityWorld, Village village, EntityPlayer player, int villageReputation)
	{
		List<EntityVillager> population = getVillagePopulation(village, entityWorld);
		
		if(villageReputation < 0 || villageReputation > 5)
		{
			updateVillageReputationFromMean(entityWorld, village, player.getUniqueID());
		}
		int villageTeamReputation = updateVillageTeamReputation(village, player);
		if(player.getTeam() != null && player.getTeam().getName().equals(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam()))
		{	
			if(villageTeamReputation < HATED_THRESHOLD)
			{
				for(EntityVillager villager : population)
				{
					villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMutinous(false).setTeam("");
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
	}

	public static void childGrown(EntityVillager entity)
	{
		if(entity.getEntityWorld().isRemote) return;
		
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		Village village = entity.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(entity), 0);
		if(vilCap.getHomeVillageID() != null || village == null) return;
		
		IVillageCapability villageCap =  village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		vilCap.setHomeVillageID(villageCap.getUUID());
		//TODO testing needed, seems to make more sense!
		if(/*vilCap.getTeam() != null &&*/ entity.getEntityWorld().getScoreboard().getTeam(villageCap.getTeam()) != null)
			vilCap.setTeam(villageCap.getTeam());
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

	public static void checkArmourWeaponsAndFood(EntityVillager entity, @Nullable UUID playerEntityByUUID)
	{
		if(playerEntityByUUID == null)
			return;
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		int armour = entity.getTotalArmorValue();
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

	public static VilEnlistPacket updateEnlistInfoForStack(ItemStack stack, boolean isEnlisted, int company, int platoon, Entity entity, IImprovedVilCapability vilCap)
	{
		if(stack != null)
		{
		 	IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
		 	if(cap != null)
		 	{
		 		if(isEnlisted)
		 		{
		 			BlockPos foodStore = cap.getPlatoonFoodStore(company, platoon);
		 			if(vilCap != null && foodStore != null)
		 				vilCap.setFoodStore(foodStore);
		 			
		 			BlockPos kitStore = cap.getPlatoonKitStore(company, platoon);
		 			if(vilCap != null && kitStore != null)
		 				vilCap.setKitStore(kitStore);
		 			
		 			cap.addVillager(entity.getUniqueID(), company, platoon);
		 			return new VilEnlistPacket(0, company, platoon, true);
		 		}
		 		else
		 		{
		 			vilCap.setFoodStore(null).setKitStore(null);
		 			cap.removeVillager(entity.getUniqueID());
		 			return new VilEnlistPacket(0, 0, 0, false);
		 		}
		 	}
		}
		return null;
	}
}