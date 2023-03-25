package com.joshycode.improvedmobs.handler;

import java.util.Map;

import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry;
import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry.RangeAttackType;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class ConfigHandlerVil {
	
	public static Configuration config;
	
	//public static boolean villagerGreifing; 
	public static boolean whiteListMobs;
	public static Multimap<String, RangeAttackEntry> configuredGuns = ArrayListMultimap.create();
	public static String[] attackableMobs;
	public static float villagerDeBuffMelee;

	public static void load(LoadState state) throws IOException {
		if(config == null) {
			config = new Configuration(new File(Loader.instance().getConfigDir(), "improvedvils/main.cfg"));
			config.load();
		}
		ConfigCategory general = config.getCategory("general");
		general.setLanguageKey("improvedvils.general");
		whiteListMobs = config.getBoolean("general", "whitelist only", false, "Set to true to have Villagers only attack whitelisted Mobs, "
				+ "otherwise Villagers will attack all instances of \"EntityMob\" + any mobs listed in whitelist");
		attackableMobs = config.getStringList("mob whitelist", "general", new String[]{EntityRegistry.getEntry(EntityWitch.class).getRegistryName().toString()}, "use the forge-registered name of the mob, this can generally "
				+ "be found in the entity registring method for the applicable mod");
		villagerDeBuffMelee = config.getFloat("villagerDe-BuffMelee", "general", .75F, .25F, 1.0F, "how much of a fraction of damage an item would cause if held by player will be caused by villager");
		if(state == LoadState.SYNC || state == LoadState.POSTINIT){
			readEntryJson();
			config.save();
		}
	}
	
	public static void readEntryJson() throws IOException {
		Gson gson = new GsonBuilder().create();
		File dir = new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			boolean flag = false;
			for (File child : directoryListing) {
				if(!flag && child.getName().equals("bowBasic.json")) {
					flag = true;
				}
				if(child.getName().endsWith("json")) {
					JsonReader jsonReader = new JsonReader(new FileReader(child));
					RangeAttackEntry entry = gson.fromJson(jsonReader, RangeAttackEntry.class);
					ConfigHandlerVil.configuredGuns.put(entry.getHandItem(), entry);
				}
			}
			if(!flag) {
				generateDefaultJson(gson);
			}
	  } else {
	    throw new IOException("ImprovedVils config file cannot be reached! Please check the config directory in your minecraft folder");
	  }
	}

	public static void generateDefaultJson(Gson  gson) throws IOException {
		Map<String, Integer> bow = new HashMap();
		bow.put(Items.ARROW.getUnlocalizedName(), 1);
		RangeAttackEntry entry =  new RangeAttackEntry(Items.BOW.getUnlocalizedName(), 0, 100, 0, 1, RangeAttackType.BOW, bow);
		try(Writer bowBasic = new FileWriter
				(new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/bowBasic.json"))) {
			gson.toJson(entry, bowBasic);
			bowBasic.close();
			configuredGuns.put(entry.getHandItem(), entry);
		}
	}
}
