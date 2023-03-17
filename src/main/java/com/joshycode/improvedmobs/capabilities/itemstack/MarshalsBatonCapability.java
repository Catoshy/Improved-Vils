package com.joshycode.improvedmobs.capabilities.itemstack;

import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.HashSet;
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
	
	public MarshalsBatonCapability() {
		companys = ArrayListMultimap.create();
		platoons = ArrayListMultimap.create();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(BATON_NBT_KEY_P, "h");
		/*for(int i : this.companys.keySet()) {
			String c = "";
			for(int ii : this.companys.get(i)) {
				c = c + "v" + Integer.toString(ii);
			}
			nbt.setString(BATON_NBT_KEY_C + i, c);
		}
		for(int i : this.platoons.keySet()) {
			String c = "";
			for(UUID ii : this.platoons.get(i)) {
				c = c + "v" + ii.toString();
			}
			nbt.setString(BATON_NBT_KEY_P + i, c);
		}*/
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		/*for(int i = 0; i < 50; i++) {
			String s = nbt.getString(BATON_NBT_KEY_P + i);
			StringCharacterIterator iterator = new StringCharacterIterator(s);
			String id = "";
			while(iterator.getIndex() != iterator.getEndIndex()) {
				if(iterator.current() == 'v') {
					this.platoons.put(i, UUID.fromString(id));
					id = "";
				} else {
					id = id + iterator.current();
				}
				iterator.next();
			}
		}
		for(int i = 0; i < 5; i++) {
			String s = nbt.getString(BATON_NBT_KEY_C + i);
			StringCharacterIterator iterator = new StringCharacterIterator(s);
			String num = "";
			while(iterator.getIndex() != iterator.getEndIndex()) {
				if(iterator.current() == 'v') {
					this.companys.put(i, Integer.valueOf(num));
					num = "";
				} else {
					num = num + iterator.current();
				}
				iterator.next();
			}
		}*/
	}

	@Override
	public boolean addVillager(UUID entityid, int company, int platoon) {	
		if(platoon < 10 && company < 5 && this.platoons.get(platoon).size() <= 30) {
			this.platoons.put(platoon, entityid);
			if(this.companys.values().contains(platoon) && this.companys.get(company).size() <= 5) {
				this.companys.put(company, platoon);
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<UUID> getVillagersPlatoon(int platoon) {
		return this.platoons.get(platoon);
	}

	@Override
	public Collection<UUID> getVillagersCompany(int company) {
		Collection<UUID> uuids = new HashSet();
		for(int i : this.companys.get(company)) {
			uuids.addAll(this.platoons.get(i));
		}
		return uuids;
	}

	@Override
	public void removeVillager(UUID entityid) {
		for(int i : this.platoons.keySet()) {
			for(UUID id : this.platoons.get(i)) {
				if(id.equals(entityid)) {
					this.platoons.remove(i, id);
				}
			}
		}
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

}
