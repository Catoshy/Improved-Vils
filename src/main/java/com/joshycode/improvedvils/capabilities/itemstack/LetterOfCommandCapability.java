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

import net.minecraft.nbt.NBTTagCompound;

public class LetterOfCommandCapability implements ILetterOfCommandCapability {

	public static final String LETTER_NBT_KEY_P = "improved-vils:letterplatoon";
	private static final String LETTER_NBT_KEY_B = "improved-vils:letterfoodstores";
	private static final String LETTER_NBT_KEY_K = "improved-vils:letterkitstores";
	private Map<Integer, Set<UUID>> platoons;
	private Map<Integer, Long> foodStorePos;
	private Map<Integer, Long> kitStorePos;
	
	public LetterOfCommandCapability() 
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
		   nbt.setByteArray(LETTER_NBT_KEY_P, baplatoon.toByteArray());
		
		   oos = new ObjectOutputStream(bastores);
		   oos.writeObject(this.foodStorePos);
		   oos.close();
		   nbt.setByteArray(LETTER_NBT_KEY_B, bastores.toByteArray());
		    
		   bastores.reset();
		    
		   oos = new ObjectOutputStream(bastores);
		   oos.writeObject(this.kitStorePos);
		   oos.close();
		   nbt.setByteArray(LETTER_NBT_KEY_K, bastores.toByteArray());
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
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(LETTER_NBT_KEY_P)));
			this.platoons = (Map<Integer, Set<UUID>>) ois.readObject();
			ois.close();
			
			/*ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_C)));
			this.companys = (Multimap<Integer, Integer>) ois.readObject();
			ois.close();*/

			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(LETTER_NBT_KEY_B)));
			this.foodStorePos = (Map<Integer, Long>) ois.readObject();
			ois.close();
						
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(LETTER_NBT_KEY_K)));
			this.kitStorePos = (Map<Integer, Long>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<Integer, Long> getfoodStoreMap() { return this.foodStorePos; }

	@Override
	public Map<Integer, Long> getkitStoreMap() { return this.kitStorePos; }

	@Override
	public Map<Integer, Set<UUID>> getPlatoonMap() { return this.platoons; }

	@Override
	public void attachInfo(Map<Integer, Long> food, Map<Integer, Long> kit, Map<Integer, Set<UUID>> platoons) 
	{
		this.foodStorePos = food;
		this.kitStorePos = kit;
		this.platoons = platoons;
	}

}
