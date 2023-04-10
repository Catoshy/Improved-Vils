package com.joshycode.improvedvils.handler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.CommonProxy.LoadState;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.RangeAttackType;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConfigHandlerVil {
	
	public static Configuration config;
	
	//public static boolean villagerGreifing; 
	public static boolean whiteListMobs;
	public static Multimap<String, RangeAttackEntry> configuredGuns = ArrayListMultimap.create();
	public static String[] attackableMobs;
	public static float villagerDeBuffMelee;
	public static float villagersPerDoor;
	public static float villagerHealth;
	public static float villagerBaseArmour;
	public static float meleeAttackCooldown;
	public static float attackReach;
	public static float attackReachSq;
	public static float blockChance;
	public static float commandDist;
	public static boolean openBlocksLoaded = false;
	
	public static void load(com.joshycode.improvedvils.CommonProxy.LoadState postinit) throws IOException {
		if(config == null) 
		{
			config = new Configuration(new File(Loader.instance().getConfigDir(), "improvedvils/main.cfg"));
			config.load();
		}
		ConfigCategory general = config.getCategory("general");
		general.setLanguageKey("improvedvils.general");
		whiteListMobs = config.getBoolean("whitelist only", "general", false, "Set to true to have Villagers only attack whitelisted Mobs, "
				+ "otherwise Villagers will attack all instances of \"EntityMob\" + any mobs listed in whitelist");
		attackableMobs = config.getStringList("mob whitelist", "general", new String[]{EntityRegistry.getEntry(EntitySlime.class).getRegistryName().toString()}, "use the forge-registered name of the mob, this can generally "
				+ "be found in the entity registring method for the applicable mod");
		villagerDeBuffMelee = config.getFloat("VillagerDe-BuffMelee", "general", .75F, .25F, 1.0F, "How much of a fraction of damage an item would cause if held by player will be caused by villager");
		villagersPerDoor = config.getFloat("Villagers per door", "general", 1, .33f, 4, "Villagers will increase population size (given they are well fed) to a maximum of \"this number\" x \"# of village doors\"");
		villagerHealth = config.getFloat("Villager health", "general", 20f, 20f, 60f, "Base health of villager");
		villagerBaseArmour = config.getFloat("Villager base armour", "general", 0.0f, 0f, 20f, "Base armour value of the villager, try to keep this low because armour can only add up to a certain point");
		meleeAttackCooldown = config.getFloat("Melee attack cooldown fraction", "general", 1f, .1f, 1.5f, "Multiplied by the number of ticks a weapon uses to cooldown to increase or decrease attack interval");
		attackReach = config.getFloat("Attack-reach", "general", 2f, 1f, 5f, "Attack reach of Villager in blocks");
		attackReachSq = attackReach * attackReach;
		blockChance = config.getFloat("Block Chance", "general", .6f, 0f, 1f, "how likely a villager's attempted block is to succeed");
		commandDist = config.getFloat("Command Distance", "general", 250f, 0f, 300f, "maximum distance in blocks a ray trace will go when right click the baton to order a villager movement");
		if(postinit == LoadState.SYNC || postinit == LoadState.POSTINIT)
		{
			readEntryJson();
			if(!whiteListMobs) 
			{
				CommonProxy.TARGETS.add(EntityMob.class);
			}
			for(String s : attackableMobs)
				CommonProxy.TARGETS.add(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(s)).getEntityClass());

			for(ModContainer s : Loader.instance().getModList()) 
			{
				if(s.getModId() == "openblocks") 
				{
					openBlocksLoaded = true;
				}
			}
			config.save();
		}
	}
	
	public static void readEntryJson() throws IOException 
	{
		Gson gson = new GsonBuilder().create();
		File dir = new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) 
		{
			boolean flag = false;
			for (File child : directoryListing) 
			{
				if(!flag && child.getName().equals("bowBasic.json")) 
				{
					flag = true;
				}
				if(child.getName().endsWith("json")) 
				{
					JsonReader jsonReader = new JsonReader(new FileReader(child));
					RangeAttackEntry entry = gson.fromJson(jsonReader, RangeAttackEntry.class);
					ConfigHandlerVil.configuredGuns.put(entry.getHandItem(), entry);
				}
			}
			if(!flag) 
			{
				generateDefaultJson(gson);
			}
		} 
		else
		{
			Files.createDirectory(dir.toPath());
			generateDefaultJson(gson);
		}
	}

	public static void generateDefaultJson(Gson  gson) throws IOException 
	{
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
