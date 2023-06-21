package com.joshycode.improvedvils.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
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

	public static void updateVillagerPlayerReputation(EntityVillager entityIn, EntityPlayer player, float reputationChange)
	{
		updateVillagerPlayerReputation(entityIn, player.getUniqueID(), reputationChange);
	}

	private static void updateVillagerPlayerReputation(EntityVillager entityIn, UUID player, float reputationChange)
	{
		IImprovedVilCapability vilCap = entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		Village village = entityIn.getEntityWorld().getVillageCollection().getNearestVillage(entityIn.getPosition(), 0);
		if(village != null)
		{
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			if(isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID()))
			{
				int newVillageReputation = village.getPlayerReputation(player);
				int villagerReferenceVillageReputation = vilCap.getHomeVillagePlayerReputationReference(player);
				int villageReputationChange = newVillageReputation - villagerReferenceVillageReputation;
				float playerReputation = vilCap.getPlayerReputation(player);
				vilCap.setPlayerReputation(player, playerReputation + reputationChange + villageReputationChange, newVillageReputation);
			}
			else
			{
				float playerReputation = vilCap.getPlayerReputation(player);
				vilCap.setPlayerReputationIfEstablished(player, playerReputation + reputationChange);
			}
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
			if(playerReputation > 5 || playerReputation < 0)
			{
				updateVillagerPlayerReputation(villager, playerId, 0);
				updateVillageReputationFromMean(villager.getEntityWorld(), village, playerId);
			}
		}
	}

	public static void updateVillageReputationFromMean(World world, Village village, UUID player)
	{
		List<EntityVillager> population = getVillagePopulation(village, world);
		double sumReputation = 0;
		int nowtimeReputation = village.getPlayerReputation(player);

		for(EntityVillager entity : population)
		{
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			sumReputation +=  vilCap.getPlayerReputation(player);
		}
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
		List<EntityVillager> list = world.getEntitiesWithinAABB(EntityVillager.class,
										new AxisAlignedBB(
												new Vec3d(villageCenter.getX() - village.getVillageRadius(), villageCenter.getY() - village.getVillageRadius(), villageCenter.getZ() - village.getVillageRadius()),
												new Vec3d(villageCenter.getX() + village.getVillageRadius(), villageCenter.getY() + village.getVillageRadius(), villageCenter.getZ() + village.getVillageRadius())));
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
		if(vilCap.getTeam() == null)
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
	
	public static void villageGoodReputationChange(World entityWorld, Village village, EntityPlayer player)
	{
		int villageReputation = village.getPlayerReputation(player.getUniqueID());
		if(player.getTeam() != null && player.getTeam().getName().equals(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam()))
		{
			villageReputationChange(entityWorld, village, player, villageReputation);
		}
	}

	private static void villageReputationChange(World entityWorld, Village village, EntityPlayer player, int villageReputation)
	{
		List<EntityVillager> population = getVillagePopulation(village, entityWorld);
		
		if(villageReputation < 0 || villageReputation > 5)
		{
			for(EntityVillager entity : population)
			{
					updateVillagerPlayerReputation(entity, player, 0);
			}
			updateVillageReputationFromMean(entityWorld, village, player.getUniqueID());
		}
		int villageTeamReputation = updateVillageTeamReputation(village, player);
		if(player.getTeam().getName().equals(village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null).getTeam()))
		{	
			if(villageTeamReputation < HATED_THRESHOLD)
			{
				for(EntityVillager villager : population)
				{
					villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMutinous(false);
					villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setTeam(null);
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
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		IVillageCapability villageCap =  entity.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		if(vilCap.getHomeVillageID() != null) return;

		vilCap.setHomeVillageID(villageCap.getUUID());
		vilCap.setTeam(entity.getEntityWorld().getScoreboard().getTeam(villageCap.getTeam()).getName());
	}

	public static void updateArmourWeaponsAndFood(EntityVillager entity, EntityPlayer playerEntityByUUID)
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
		vilCap.setAttackValue(attackVal);
		vilCap.setShield(entity.getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK);
		vilCap.setSaturation(InventoryUtil.getFoodSaturation(entity.getVillagerInventory()));
	}

	public static void checkArmourWeaponsAndFood(EntityVillager entity, EntityPlayer playerEntityByUUID)
	{
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

		vilCap.setPlayerReputationIfEstablished(playerEntityByUUID.getUniqueID(), wholeReputationChange);
		vilCap.setArmourValue(armour);
		vilCap.setAttackValue(attackVal);
		vilCap.setShield(hasShield);
		vilCap.setSaturation(foodSaturation);
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
		 			cap.addVillager(entity.getUniqueID(), company, platoon);
		 			return new VilEnlistPacket(0, company, platoon, true);
		 		}
		 		else
		 		{
		 			cap.removeVillager(entity.getUniqueID());
		 			return new VilEnlistPacket(0, 0, 0, false);
		 		}
		 	}
		}
		return null;
	}
}