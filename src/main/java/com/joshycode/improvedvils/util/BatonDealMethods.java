package com.joshycode.improvedvils.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.gui.EnlisteeContainer;
import com.joshycode.improvedvils.gui.GuiVillagerRollList;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;

public class BatonDealMethods {

	public static Map<Integer, UUID> getEntityIDsFromBatonPlatoon(EntityPlayerMP player, ItemStack stack) 
	{
		Map<UUID, Integer> map = new HashMap<UUID, Integer>(); //Map of entity unique ID to its in world ID (if it exists)
		Set<UUID> vilIDs = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null).getVillagersSelected();
		Set<Entity> foundEntities = CommonProxy.getEntitiesByUUID(vilIDs, player.world);
		
		int i = -1;
		for(UUID entityID : vilIDs) //UUIDs mapped to impossible entity-ID by default
			map.put(entityID, i--);
		foundEntities.forEach(t -> {map.replace(t.getUniqueID(), t.getEntityId());}); //put true entity-ID in map if already there
			
		Map<Integer, UUID> swapped = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)); //swap the map keys <-> values
		
		return swapped;
	}
	
	public static Map<Integer, Tuple<Boolean[], Integer>> getVillagerCapabilityInfoAppendMap(Set<Integer> idSet, World world)
	{
		Map<Integer, Tuple<Boolean[], Integer>> map = new HashMap<Integer, Tuple<Boolean[], Integer>>();
		idSet.forEach(entityId -> {
			if(entityId < 0)
				return;
			EntityVillager villager =  (EntityVillager) world.getEntityByID(entityId);
			IImprovedVilCapability cap = villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			Boolean[] info = new Boolean[5];
			Integer hungerInfo = 0;
			info[0] = cap.getActiveDuty();
			info[1] = cap.isFollowing();
			info[2] = cap.getGuardBlockPos() != null;
			info[3] = cap.getFoodStorePos() != null;
			info[4] = cap.getKitStorePos() != null;
			Log.info("food store = " + cap.getFoodStorePos() + " kit store = " + cap.getKitStorePos(), (Object[])null);
			hungerInfo = (int) Math.max(1F, InventoryUtil.getFoodSaturation(villager.getVillagerInventory()) /.6F / ConfigHandler.dailyBread);
			if(cap.getHungry())
				hungerInfo = 0;
			map.put(entityId, new Tuple<Boolean[], Integer>(info, hungerInfo));
		});
		return map;
	}

	public static Map<Integer, UUID> getEntityIDsFromRollList(GuiVillagerRollList rollList, boolean in_OutOfRender) 
	{
		Map<Integer, UUID> list = new HashMap<Integer, UUID>();
		Stream<Integer> selectedStream = rollList.getSelected().stream();
		
		if(!in_OutOfRender)
			selectedStream.filter(new Predicate<Integer>() {
					public boolean apply(Integer input) {
						int id = rollList.getRoll().get(input).villagerEntityId;
						return id >= 0;
					}
				}).forEach(index -> {
					EnlisteeContainer container = rollList.getRoll().get(index);
					list.put(container.villagerEntityId, container.villagerUUID);
				});
		else	
			selectedStream.forEach(index -> {
					EnlisteeContainer container = rollList.getRoll().get(index);
					list.put(container.villagerEntityId, container.villagerUUID);
				});
		return list;
	}

}
