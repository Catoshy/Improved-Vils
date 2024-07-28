package com.joshycode.improvedvils.capabilities.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

public class MarshalsBatonCapability implements IMarshalsBatonCapability {

	public static final String BATON_NBT_KEY_C = "improved-vils:marshalsbatoncompany";
	public static final String BATON_NBT_KEY_P = "improved-vils:marshalsbatonplatoon";
	private static final String BATON_NBT_KEY_B = "improved-vils:marshalsbatonfoodstores";
	private static final String BATON_NBT_KEY_K = "improved-vils:marshalsbatonkitstores";

	private Map<Integer, Set<UUID>> platoons;
	private Map<Integer, Long> foodStorePos;
	private Map<Integer, Long> kitStorePos;
	private int selectedUnit;
	private long isSureClearCommand;

	public MarshalsBatonCapability()
	{
		this.platoons = this.generateEmptyPlatoonMap();
		this.foodStorePos = new HashMap<>();
		this.kitStorePos = new HashMap<>();
		this.isSureClearCommand = 0;
	}
	
	public void clearCommand()
	{
		this.platoons = this.generateEmptyPlatoonMap();
		this.foodStorePos = new HashMap<>();
		this.kitStorePos = new HashMap<>();
	}

	private Map<Integer, Set<UUID>> generateEmptyPlatoonMap() 
	{
		Map<Integer, Set<UUID>> map = new HashMap<Integer, Set<UUID>>();
		for(int i = 0; i < 50; i++)
		{
			map.put(i, new HashSet<UUID>());
		}
		return map;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound nbt = new NBTTagCompound();
		ByteArrayOutputStream baplatoon = new ByteArrayOutputStream();
		ByteArrayOutputStream bastores = new ByteArrayOutputStream();
		ObjectOutputStream oos;

	    try
	    {    	 
		    oos = new ObjectOutputStream(baplatoon);
		    oos.writeObject(this.platoons);
			oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_P, baplatoon.toByteArray());

		    oos = new ObjectOutputStream(bastores);
		    oos.writeObject(this.foodStorePos);
		    oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_B, bastores.toByteArray());
		    
		    bastores.reset();
		    
		    oos = new ObjectOutputStream(bastores);
		    oos.writeObject(this.kitStorePos);
		    oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_K, bastores.toByteArray());
		} catch (IOException e) { e.printStackTrace(); }
	     
		return nbt;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeNBT(NBTTagCompound nbt) 
	{
		ObjectInputStream ois;
		try 
		{
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_P)));
			this.platoons = (Map<Integer, Set<UUID>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_B)));
			this.foodStorePos = (Map<Integer, Long>) ois.readObject();
			ois.close();
						
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_K)));
			this.kitStorePos = (Map<Integer, Long>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean addVillager(UUID entityid, int company, int platoon) 
	{
		if(platoon > 10 || company > 5 ) return false;
		
		if(this.platoons.values().stream().anyMatch(new Predicate<Set<UUID>>() {
				@Override 
				public boolean test(Set<UUID> arg0) 
				{
					return arg0.contains(entityid);
				}
			}))
		{
			return false;
		}
		
		int uniqueplatoonid = platoon + 10 * company;
		Set<UUID> platoonSet = this.platoons.get(uniqueplatoonid);
		
		if(platoonSet.isEmpty())
		{
			platoonSet = new HashSet<UUID>();
		}
		
		if(platoonSet.size() >= 30) return false;
		
		boolean flag = platoonSet.add(entityid);
		
		this.platoons.put(uniqueplatoonid, platoonSet);
		
		return flag;
	}

	private Set<UUID> getVillagersPlatoon(int platoon) 
	{
		return this.platoons.get(platoon);
	}

	private Set<UUID> getVillagersCompany(int company) 
	{
		Set<UUID> uuids = new HashSet<UUID>();
		for(int i = 0; i < 10; i++ ) 
		{
			uuids.addAll(this.platoons.get((company *  10) + i));
		}
		return uuids;
	}

	@Override
	public boolean removeVillager(UUID entityid) 
	{
		for(Set<UUID> platoon : this.platoons.values())
		{
			if(platoon.remove(entityid)) return true;
		}
		return false;
	}

	@Override
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID) 
	{
		for(int p = 0; p < this.platoons.size(); p++)
			for(UUID id : this.platoons.get(p))
				if(id.equals(uniqueID))
				{
					if(ConfigHandler.debug)
						Log.info("an ID found in baton: %s", uniqueID);
					return new Pair<Integer, Integer>(p / 10, p);
				}
		return null;
	}

	@Override
	public int selectedUnit() 
	{
		return this.selectedUnit;
	}

	@Override
	public void setPlatoon(int company, int platoon) 
	{
		this.selectedUnit = (10 * company) + platoon;
	}

	@Override
	public Set<UUID> getVillagersSelected() {
		if(this.selectedUnit < 0)
			return this.getVillagersCompany(Math.abs(selectedUnit) - 1);
		else
			return this.getVillagersPlatoon(selectedUnit);
	}

	@Override
	public BlockPos getPlatoonFoodStore(int platoon, int company)
	{
		Long serialized = this.foodStorePos.get(platoon + 10 * company);
		if(serialized != null)
			return BlockPos.fromLong(this.foodStorePos.get(platoon + 10 * company));
		else
			return null;
	}

	@Override
	public void setPlatoonFoodStore(BlockPos pos)
	{
		this.foodStorePos.put(this.selectedUnit, pos.toLong());
	}
	
	@Override
	public BlockPos getPlatoonKitStore(int company, int platoon) 
	{
		Long serialized = this.kitStorePos.get(platoon + 10 * company);
		if(serialized != null)
			return BlockPos.fromLong(this.kitStorePos.get(platoon + 10 * company));
		else
			return null;
	}
	
	@Override
	public void setPlatoonKitStore(BlockPos pos) 
	{
		this.kitStorePos.put(this.selectedUnit, pos.toLong());
	}
	
	public enum Provisions
	{
		KIT, PROVISIONS;
	}

	@Override
	public Map<Integer, Long> getfoodStoreMap() { return this.foodStorePos; }

	@Override
	public Map<Integer, Long> getkitStoreMap() { return this.kitStorePos; }

	@Override
	public Map<Integer, Set<UUID>> getPlatoonMap() { return this.platoons; }

	@Override
	public void attachInfo(Map<Integer, Long> foodStoreMap, Map<Integer, Long> kitStoreMap,
			Map<Integer, Set<UUID>> platoonMap) 
	{
		this.foodStorePos = foodStoreMap;
		this.kitStorePos = kitStoreMap;
		this.platoons = platoonMap;
	}

	@Override
	public boolean isSureClearCommand(long worldTime) 
	{
		return (worldTime - this.isSureClearCommand) < 800;
	}

	@Override
	public void setSureClearCommand(long worldTime) 
	{
		this.isSureClearCommand = worldTime;
	}
}
