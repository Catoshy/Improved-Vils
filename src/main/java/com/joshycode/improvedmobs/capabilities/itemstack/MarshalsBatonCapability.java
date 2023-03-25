package com.joshycode.improvedmobs.capabilities.itemstack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.joshycode.improvedmobs.util.Pair;

import net.minecraft.nbt.NBTTagCompound;

public class MarshalsBatonCapability implements IMarshalsBatonCapability{
	public static final String BATON_NBT_KEY_C = "improved-vils:marshalsbatoncompany";
	public static final String BATON_NBT_KEY_P = "improved-vils:marshalsbatonplatoon";

	
	private Multimap<Integer, Integer> companys;
	private Multimap<Integer, UUID> platoons;
	private int selectedUnit;
	
	public MarshalsBatonCapability() {
		companys = ArrayListMultimap.create();
		platoons = ArrayListMultimap.create();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		 ByteArrayOutputStream bacompany = new ByteArrayOutputStream();
		 ByteArrayOutputStream baplatoon = new ByteArrayOutputStream();
		 ObjectOutputStream oos;
	     try {
			oos = new ObjectOutputStream(bacompany);
			oos.writeObject(this.companys);
			oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_C, bacompany.toByteArray());
		    oos = new ObjectOutputStream(baplatoon);
		    oos.writeObject(this.platoons);
			oos.close();
		    nbt.setByteArray(BATON_NBT_KEY_P, baplatoon.toByteArray());    
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_P)));
			this.platoons = (Multimap<Integer, UUID>) ois.readObject();
			ois.close();
			ois = new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(BATON_NBT_KEY_C)));
			this.companys = (Multimap<Integer, Integer>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean addVillager(UUID entityid, int company, int platoon) {	
		int uniqueplatoonid = platoon + 10 * company;
		if(platoon < 10 && company < 5 && this.platoons.get(platoon).size() <= 30) {
			this.platoons.put(uniqueplatoonid, entityid);
			if(!this.companys.values().contains(platoon) && this.companys.get(company).size() <= 5) {
				this.companys.put(company, uniqueplatoonid);
				return true;
			}
			System.out.println("Marshal Cap Class - addVillager -- companys  = " + this.companys.toString());
			System.out.println("Marshal Cap Class - addVillager -- platoons  = " + this.platoons.toString());
		}
		return false;
	}

	private Set<UUID> getVillagersPlatoon(int platoon) {
		Set<UUID> platoonSet = new HashSet();
		platoonSet.addAll(this.platoons.get(platoon));
		return platoonSet;
	}

	private Set<UUID> getVillagersCompany(int company) {
		Set<UUID> uuids = new HashSet();
		for(int i : this.companys.get(company)) {
			uuids.addAll(this.platoons.get(i));
		}
		return uuids;
	}

	@Override
	public boolean removeVillager(UUID entityid) {
		Pair<Integer, UUID> p = findUUID(entityid);
		if(p != null) {
			this.platoons.remove(p.a, p.b);
			if(this.platoons.values().isEmpty()) {
				Pair<Integer, Integer> p2 = findPlatoon(p.a);
				if(p2 != null)
					this.companys.remove(p2.a, p2.b);
			}	
		}
		return false;
	}
	
	private Pair<Integer, UUID> findUUID(UUID entityid) {
		for(int i : this.platoons.keySet()) {
			for(Iterator<UUID> iterator = this.platoons.get(i).iterator(); iterator.hasNext(); ) {
				UUID id = iterator.next();
				if(id.equals(entityid)) {
					System.out.println("Marshal Cap Class - removeVillager id = " + id.toString() + " entityid = " + entityid.toString() + " i = " + i);
					return new Pair<Integer, UUID>(i, id);
				}
			}
		}
		return null;
	}
	
	private Pair<Integer, Integer> findPlatoon(int platoonIn) {
		for(int i : this.companys.keySet())
			if(this.companys.get(i).contains(platoonIn))
				return new Pair  (i, platoonIn);
		return null;
	}

	@Override
	public Pair<Integer, Integer> getVillagerPlace(UUID uniqueID) {
		for(int c : this.companys.keySet())
			for(int p : this.companys.get(c))
				for(UUID id : this.platoons.get(p))
					if(id.equals(uniqueID))
						return new Pair(c, p);
			
		return null;
	}

	@Override
	public int selectedUnit() {
		return this.selectedUnit;
	}

	@Override
	public void setCompany(int company) {
		this.selectedUnit = -company - 1;
	}

	@Override
	public void setPlatoon(int company, int platoon) {
		this.selectedUnit = (10 * company) + platoon;
	}

	@Override
	public Set<UUID> getVillagersSelected() {
		if(this.selectedUnit < 0)
			return this.getVillagersCompany(Math.abs(selectedUnit) - 1);
		else 
			return this.getVillagersPlatoon(selectedUnit);
	}

}
