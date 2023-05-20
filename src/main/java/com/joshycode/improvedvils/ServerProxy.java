package com.joshycode.improvedvils;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.entity.VillagerInvListener;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilStateUpdate;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class ServerProxy extends CommonProxy {
	
	private static final double SIX_BLOCKS_SQUARED = 36D;
	private static final int DRAFTED = 1;
	private static final int NULL = 0;
	private static final double GUARDING = 1;
	private static final int FOLLOWING = 1;
	private static final int EVIL = -15;
	private static final int HATED_THRESHOLD = -10;
	private static final int GOOD_THRESHOLD = 5;
	private static final int BAD_THRESHOLD = -5;
	private static final int UNBEARABLE_THREASHOLD = -7;


	public static void openVillagerGUI(EntityPlayer player, int entityInId, World world) 
	{
		EntityVillager entityIn = (EntityVillager) world.getEntityByID(entityInId);
		Village village = entityIn.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(entityIn), 0);
		
		double dist = player.getDistanceSq(entityIn);
		
		Team vilTeam = getVillagerTeam(entityIn);
		UUID vilsPlayerId = VilMethods.getPlayerId(entityIn);
		float villagerPlayerRep = getPlayerReputation(entityIn, player.getUniqueID());
		int wholeVillagePlayerRep = getWholeVillagePlayerRep(village, entityIn, player);
		
		int villageTeamRep = getVillageTeamRep(village);
		boolean isCurrentPlayer = false;
		boolean noCurrentPlayer = false;
		
		if(vilsPlayerId == null)
		{
			noCurrentPlayer = true;
		}
		else
		{
			isCurrentPlayer = vilsPlayerId.equals(player.getUniqueID());
		}
		
		int currentPlayerVillageRep = getCurrentPlayerWholeVillageRep(vilsPlayerId, village, wholeVillagePlayerRep, isCurrentPlayer);
		//TODO remove excess params, use state methods already in class
		if(isTooFar(dist)  
				|| 
		   !isPlayerOfGoodStanding(
				player, 
				entityIn, 
				vilTeam, 
				isCurrentPlayer, 
				noCurrentPlayer, 
				wholeVillagePlayerRep,
				villagerPlayerRep, 
				currentPlayerVillageRep, 
				villageTeamRep) 
				|| 
		    entityIn.isChild()) return;
		
		updateArmourWeaponsAndFood(entityIn, player);
		VilMethods.setPlayerId(player, entityIn);
		
		if(vilTeam == null || player.getTeam().isSameTeam(vilTeam))
			setPlayerVillagerFealtyIfWorthy(player, entityIn, village);
		
		int platoonAndEnlistedStanding = -2;
		int company = 0;
		
		ItemStack stack = InventoryUtil.get1StackByItem(player.inventory, CommonProxy.ItemHolder.BATON);
		
		if(stack != null && getPlayerFealty(player, entityIn))
		{
			IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
			if(cap != null) 
			{
				Pair<Integer, Integer> p = cap.getVillagerPlace(entityIn.getUniqueID());
				platoonAndEnlistedStanding = -1; /* Has the Baton but is not Enlisted*/
				
				if(p != null) 
				{
					platoonAndEnlistedStanding = p.a;
					company = p.b;
				}
			}
		} 
		/* platoonAndEnlistedStanding = -2; Does not have baton, cannot be Enlisted*/
		player.openGui(ImprovedVils.instance, 100, world, entityIn.getEntityId(), platoonAndEnlistedStanding, company);
	}
	
	public static void setPlayerReputationAcrossVillage(World world, Village village, EntityPlayer player)
	{
		int reputation = village.getPlayerReputation(player.getUniqueID());
		List<EntityVillager> population = getVillagePopulation(village, world);

		for(EntityVillager entity : population)
		{
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			if(isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID()) && vilCap.getPlayerReputation(player.getUniqueID()) == 0)
			{
				vilCap.setPlayerReputation(player.getUniqueID(), reputation);
			}
		}
	}
	
	public static void updateVillagerPlayerReputation(EntityVillager entityIn, EntityPlayer player, float reputationChange)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		Village village = entityIn.getEntityWorld().getVillageCollection().getNearestVillage(entityIn.getPosition(), 0);
		if(village != null)
		{
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			if(isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID()))
			{
				int newVillageReputation = village.getPlayerReputation(player.getUniqueID());
				int villagerReferenceVillageReputation = vilCap.getHomeVillagePlayerReputationReference(player.getUniqueID());
				int villageReputationChange = newVillageReputation - villagerReferenceVillageReputation;
				float playerReputation = vilCap.getPlayerReputation(player.getUniqueID());
				vilCap.setPlayerReputation(player.getUniqueID(), playerReputation + reputationChange + villageReputationChange);
				vilCap.setHomeVillagePlayerReputationReference(player.getUniqueID(), newVillageReputation);
				updateVillageReputationFromMean(entityIn.getEntityWorld(), village, player);
			}
			else
			{
				float playerReputation = vilCap.getPlayerReputation(player.getUniqueID());
				vilCap.setPlayerReputation(player.getUniqueID(), playerReputation + reputationChange);
			}
		}
	}
	
	public static void updateVillageReputationFromMean(World world, Village village, EntityPlayer player)
	{
		List<EntityVillager> population = getVillagePopulation(village, world);
		double sumReputation = 0;
		int truePopulation = 0;
		int nowtimeReputation = village.getPlayerReputation(player.getUniqueID());

		for(EntityVillager entity : population)
		{
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			if(villageCap.getUUID().equals(vilCap.getHomeVillageID()))
			{
				truePopulation++;
				sumReputation +=  vilCap.getPlayerReputation(player.getUniqueID());
			}
		}
		int difference = (int) (sumReputation/truePopulation) - nowtimeReputation;
		village.modifyPlayerReputation(player.getUniqueID(), difference);
	}
	
	public static List<EntityVillager> getVillagePopulation(Village village, World world)
	{
		BlockPos villageCenter = village.getCenter();
		return world.getEntitiesWithinAABB(EntityVillager.class, 
				new AxisAlignedBB(
						new Vec3d(villageCenter.getX() - village.getVillageRadius(), villageCenter.getY() - village.getVillageRadius(), villageCenter.getZ() - village.getVillageRadius()), 
						new Vec3d(villageCenter.getX() + village.getVillageRadius(), villageCenter.getY() + village.getVillageRadius(), villageCenter.getZ() + village.getVillageRadius())));
	}

	private static void checkAndTryToClaimVillageForTeam(EntityPlayer player, EntityVillager entityIn, Village village, int wholeVillageReputation) 
	{
		IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(wholeVillageReputation < GOOD_THRESHOLD || player.getWorldScoreboard().getTeam(villageCap.getTeam()) != null) return;
		
		int teamReputation = villageCap.getTeamReputation(player.getTeam());
		boolean evilPlayer = false;
		
		if(teamReputation == 0)
		{
			float sum = 0;
			int denom = 0;
			Iterator<String> iterator = player.getTeam().getMembershipCollection().iterator();
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
		villageCap.setTeam(player.getTeam());
		List<EntityVillager> population = getVillagePopulation(village, entityIn.getEntityWorld());
		for(EntityVillager villager : population)
		{
			if(!villager.isChild())
			{
				IImprovedVilCapability vilCap = villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
				if(isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID()))
				{
					vilCap.setTeam(player.getTeam());
				}
			}
		}
	}

	public static boolean getPlayerFealty(EntityPlayer player, EntityVillager entityIn)
	{
		Village village = entityIn.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(entityIn), 0);
		return doesPlayerHaveTeamFealty(player, entityIn, village) || doesPlayerHaveFealty(player, entityIn);
	}
	
	private static void setPlayerVillagerFealtyIfWorthy(EntityPlayer player, EntityVillager entityIn, Village village) 
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		int wholeVillageReputation = village.getPlayerReputation(player.getUniqueID());
		
		//if(vilCap.getPlayerReputation(player.getUniqueID()) != 0) return;
		
		if(isVillagerInHomeVillage(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getUUID(), vilCap.getHomeVillageID()))
		{
			if(wholeVillageReputation < GOOD_THRESHOLD && wholeVillageReputation > 0) return;
			
			setPlayerReputationAcrossVillage(entityIn.getEntityWorld(), village, player);
			checkAndTryToClaimVillageForTeam(player, entityIn, village, wholeVillageReputation);
			vilCap.setPlayerReputation(player.getUniqueID(), wholeVillageReputation);
		}
		else
		{
			vilCap.setPlayerReputation(player.getUniqueID(), .25F);
		}
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
		if(vilCap.getTeam() == null)
		{
			return vilCap.getPlayerReputation(player.getUniqueID()) > 0 && vilCap.getPlayerId().equals(player.getUniqueID());
		}
		return false;
	}

	private static int getCurrentPlayerWholeVillageRep(UUID vilsPlayerId, Village village, int wholeVillagePlayerRep, boolean isCurrentPlayer) 
	{
		if(!isCurrentPlayer)
		{
			if(vilsPlayerId != null)
				return village.getPlayerReputation(vilsPlayerId);
		}
		else
		{
			return wholeVillagePlayerRep;
		}
		return 0;
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

	private static int getVillageTeamRep(Village village) 
	{
		return village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getCurrentTeamReputation();
	}

	private static float getPlayerReputation(EntityVillager entityIn, UUID uuid) 
	{
		return entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerReputation(uuid);
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

	private static boolean isPlayerOfGoodStanding(EntityPlayer player, EntityVillager entityIn, Team vilTeam, boolean isCurrentPlayer, boolean noCurrentPlayer, int wholeVillagePlayerRep, float villagerPlayerRep, int currentPlayerVillageRep, int villageCurrentTeamRep)
	{
		if(vilTeam == null) 
		{
			if(!isCurrentPlayer)
			{
				if(noCurrentPlayer && wholeVillagePlayerRep > BAD_THRESHOLD)
				{
					return true;
				}
				float currentPlayerRep = currentPlayerReputation(entityIn);
				if((villagerPlayerRep >  currentPlayerRep && currentPlayerRep < BAD_THRESHOLD) ||
						(wholeVillagePlayerRep > 0  && currentPlayerVillageRep < UNBEARABLE_THREASHOLD))
				{
					return true;
				}
			}
			else if(villagerPlayerRep > UNBEARABLE_THREASHOLD && wholeVillagePlayerRep > HATED_THRESHOLD)
			{
				return true;
			}
		}
		else if(player.getTeam().isSameTeam(vilTeam))
		{
			if(villagerPlayerRep > HATED_THRESHOLD && wholeVillagePlayerRep > HATED_THRESHOLD)
				return true;
		}
		else
		{
			//TODO slight modification to this, should make separate wholeVilRep for either case (familiar player or evil  team) 
			if((villagerPlayerRep > 0 || villageCurrentTeamRep < UNBEARABLE_THREASHOLD) && wholeVillagePlayerRep > HATED_THRESHOLD)
				return true;
		}
		return false;
	}

	private static float currentPlayerReputation(EntityVillager entityIn) 
	{
		UUID id = VilMethods.getPlayerId(entityIn);
		if(id != null)
		{
			return entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerReputation(id);
		}
		return 0;
	}

	private static boolean isVillagerInHomeVillage(UUID villageID, @Nullable UUID vilsHomeVillageID) 
	{
		return villageID.equals(vilsHomeVillageID);
	}

	private static boolean isTooFar(double dist) {
		return dist >= SIX_BLOCKS_SQUARED;
	}

	public static VilStateUpdate getUpdateGuiForClient(Entity e, EntityPlayer player, boolean isClosed)
	{
		Vec3i vec = null; int guardStateVal = 0, followStateVal = 0, enlistedCompany = 0, enlistedPlatoon = 0;
		
		if(e instanceof EntityVillager)  
		{
			if(isClosed) 
			{
				resetInvListeners((EntityVillager) e);
			}
			else if(player.getUniqueID().equals(VilMethods.getPlayerId((EntityVillager) e))) 
			{
				guardStateVal += InventoryUtil.doesInventoryHaveItem
						(((EntityVillager) e).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 ? DRAFTED : NULL;
				guardStateVal = VilMethods.getHungry((EntityVillager) e) ? NULL : guardStateVal;
				
				if(guardStateVal == 0) 
				{
					VilMethods.setFollowing((EntityVillager) e, false);
					VilMethods.setGuardBlock((EntityVillager) e, null);
				}
				
				followStateVal = guardStateVal;
				guardStateVal += VilMethods.getGuardBlockPos((EntityVillager) e) != null ? GUARDING : NULL;
				followStateVal += VilMethods.getFollowing((EntityVillager) e) ? FOLLOWING : NULL;
				
				if(guardStateVal == 2)
					vec = VilMethods.getGuardBlockPos((EntityVillager) e);
			}
		}
		
		if(vec != null) 
		{
			return new VilStateUpdate(guardStateVal, followStateVal, enlistedCompany, enlistedPlatoon, vec);
		}
		return new VilStateUpdate(guardStateVal, followStateVal, enlistedCompany, enlistedPlatoon);
	}
	
	private static void resetInvListeners(EntityVillager entity) 
	{
		VillagerInvListener list = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getListener();
		entity.getVillagerInventory().removeInventoryChangeListener(list);
		entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(null);
	}

	//TODO I assume that the cast is safe, double check if crash!
	public static void updateGuiForClient(EntityVillager entity, EntityPlayer playerEntityByUUID, boolean isClosed) 
	{
		NetWrapper.NETWORK.sendTo(getUpdateGuiForClient(entity, playerEntityByUUID, isClosed), (EntityPlayerMP) playerEntityByUUID);
	}

	public static void villageBadReputationChange(World entityWorld, Village village, EntityPlayer attackingPlayer)
	{
		int villageReputation = village.getPlayerReputation(attackingPlayer.getUniqueID());
		if(villageReputation < 0)
		{
			villageReputationChange(entityWorld, village, attackingPlayer, villageReputation);
		}
	}
	
	private static void villageReputationChange(World entityWorld, Village village, EntityPlayer attackingPlayer, int villageReputation) 
	{
		if(villageReputation < 0 || villageReputation > 5)
		{
			List<EntityVillager> population = getVillagePopulation(village, entityWorld);
			for(EntityVillager entity : population)
			{
					updateVillagerPlayerReputation(entity, attackingPlayer, 0);
			}
		}
	}

	public static void childGrown(EntityVillager entity) 
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		IVillageCapability villageCap =  entity.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(vilCap.getHomeVillageID() != null) return;
		
		vilCap.setHomeVillageID(villageCap.getUUID());
		vilCap.setTeam(entity.getEntityWorld().getScoreboard().getTeam(villageCap.getTeam()));
	}
	
	public static void updateArmourWeaponsAndFood(EntityVillager entity, EntityPlayer playerEntityByUUID)
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		vilCap.setArmourValue(entity.getTotalArmorValue());
		AttributeModifier attackMod = entity.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next();
		float attackVal = (float) attackMod.getAmount();
		vilCap.setAttackValue(attackVal);
		vilCap.setShield(entity.getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK);
		vilCap.setSaturation(InventoryUtil.getFoodSaturation(entity.getVillagerInventory()));
	}

	public static void checkArmourWeaponsAndFood(EntityVillager entity, EntityPlayer playerEntityByUUID) 
	{
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		int armour = entity.getTotalArmorValue();
		int prevArmour = vilCap.getArmourValue();
		AttributeModifier attackMod = entity.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next();
		float attackVal = (float) attackMod.getAmount();
		float prevAttackVal = vilCap.getAttackValue();
		boolean hasShield = entity.getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK;
		boolean prevShield = vilCap.getShieldValue();
		float foodSaturation = InventoryUtil.getFoodSaturation(entity.getVillagerInventory());
		float prevSaturation = vilCap.getFoodSaturation();
		
		float wholeReputationChange = (armour - prevArmour) + (attackVal - prevAttackVal) + ((foodSaturation - prevSaturation) * .5f);
		
		if(hasShield != prevShield)
			if(!hasShield)
				wholeReputationChange -= 5;
			else
				wholeReputationChange += 5;
		
		vilCap.setPlayerReputation(playerEntityByUUID.getUniqueID(), wholeReputationChange);
		vilCap.setArmourValue(armour);
		vilCap.setAttackValue(attackVal);
		vilCap.setShield(hasShield);
		vilCap.setSaturation(foodSaturation);
	}
}