package com.joshycode.improvedvils.capabilities.itemstack;

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

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class MarshalsBatonCapability implements IMarshalsBatonCapability {

	public static final String BATON_NBT_KEY_C = "improved-vils:marshalsbatoncompany";
	public static final String BATON_NBT_KEY_P = "improved-vils:marshalsbatonplatoon";
	private static final String BATON_NBT_KEY_B = "improved-vils:marshalsbatonfoodstores";
	private static final String BATON_NBT_KEY_K = "improved-vils:marshalsbatonkitstores";
	private static final Set<UUID> emptySet = new HashSet<UUID>();

	private Map<Integer, Set<UUID>> platoons;
	private Map<Integer, Long> foodStorePos;
	private Map<Integer, Long> kitStorePos;
	private int selectedUnit;

	public MarshalsBatonCapability()
	{
		platoons = this.generateEmptyPlatoonMap();
		foodStorePos = new HashMap<>();
		kitStorePos = new HashMap<>();
	}

	private Map<Integer, Set<UUID>> generateEmptyPlatoonMap() 
	{
		Map<Integer, Set<UUID>> map = new HashMap<Integer, Set<UUID>>();
		for(int i = 0; i < 50; i++)
		{
			map.put(i, emptySet);
		}
		return map;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound nbt = new NBTTagCompound();

		 //ByteArrayOutputStream bacompany = new ByteArrayOutputStream();
		 ByteArrayOutputStream baplatoon = new ByteArrayOutputStream();
		 ByteArrayOutputStream bafoodstores = new ByteArrayOutputStream();
		 ObjectOutputStream oos;

	     try
	     {
			/*oos = new ObjectOutputStream(bacompany);
			oos.writeObject(this.companys);
			oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_C, bacompany.toByteArray());*/

		    oos = new ObjectOutputStream(baplatoon);
		    oos.writeObject(this.platoons);
			oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_P, baplatoon.toByteArray());

		    oos = new ObjectOutputStream(bafoodstores);
		    oos.writeObject(this.foodStorePos);
		    oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_B, bafoodstores.toByteArray());
		    
		    oos = new ObjectOutputStream(bafoodstores);
		    oos.writeObject(this.kitStorePos);
		    oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_K, bafoodstores.toByteArray());
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
			
			/*ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_C)));
			this.companys = (Multimap<Integer, Integer>) ois.readObject();
			ois.close();*/
			
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
	public boolean removeVillager(UUID entityid) {
		boolean flag = false;
		for(Set<UUID> platoon : this.platoons.values())
		{
			flag |= platoon.remove(entityid);
		}
		return flag;
	}

	@Override
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID) 
	{
		for(int p = 0; p < this.platoons.size(); p++)
			for(UUID id : this.platoons.get(p))
				if(id.equals(uniqueID))
				{
					if(ConfigHandler.debug)
						Log.info("an Id found in baton: %s", uniqueID);
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
	public void setPlatoonKitStore(BlockPos pos) 
	{
		this.kitStorePos.put(this.selectedUnit, pos.toLong());
	}
	
	public enum Provisions
	{
		KIT, PROVISIONS;
	}
}
