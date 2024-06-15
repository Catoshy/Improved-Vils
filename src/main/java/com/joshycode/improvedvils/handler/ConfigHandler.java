package com.joshycode.improvedvils.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.CommonProxy.LoadState;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.util.GenFileStrings;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConfigHandler {

	public static Configuration config;

	//public static boolean villagerGreifing;
	public static boolean whiteListMobs;
	public static Map<WeaponBrooksData, ArrayList<RangeAttackEntry>> configuredGuns = new HashMap<WeaponBrooksData, ArrayList<RangeAttackEntry>>();
	public static String[] attackableMobs;
	public static String[] rangeAttackBlacklist;
	public static float villagerDeBuffMelee;
	public static float villagersPerDoor;
	public static float villagerHealth;
	public static float villagerBaseArmour;
	public static float meleeAttackCooldown;
	public static float attackReach;
	public static float attackReachSq;
	public static float followRange;
	public static float followRangeNormal;
	public static float blockChance;
	public static float commandDist;
	public static float dailyBread;
	public static float collectFoodThreshold;
	public static float movementSpeed;
	public static boolean openBlocksLoaded = false;
	public static boolean debug = false;
	public static boolean renderItemsAndArmour;
	public static int friendlyFireSearchRange;
	public static int targetDistance;

	public static void load(com.joshycode.improvedvils.CommonProxy.LoadState postinit) throws IOException, JsonParseException {
		if(config == null)
		{
			config = new Configuration(new File(Loader.instance().getConfigDir(), "improvedvils/main.cfg"));
			config.load();
		}
		ConfigCategory general = config.getCategory("general");
		general.setLanguageKey("improvedvils.general");
		ConfigCategory stats = config.getCategory("stats");
		stats.setLanguageKey("improvedvils.stats");
		ConfigCategory attack = config.getCategory("attack");
		attack.setLanguageKey("improvedvils.attack");
		
		whiteListMobs = config.getBoolean("Whitelist Only", "general", false, "Set to true to have Villagers only attack whitelisted Mobs, "
				+ "otherwise Villagers will attack all instances of \"EntityMob\" + any mobs listed in whitelist");
		attackableMobs = config.getStringList("Mob Whitelist", "general", new String[]{EntityRegistry.getEntry(EntitySlime.class).getRegistryName().toString()}, "Use the forge-registered name of the mob, this can generally "
				+ "be found in the entity registring method for the applicable mod");
		rangeAttackBlacklist = config.getStringList("Ranged Attack Blacklist", "general", new String[]{EntityRegistry.getEntry(EntityEnderman.class).getRegistryName().toString()}, "Entities in this list will not be"
				+ " attacked with ranged weapons, generally the Enderman, and will instead be attacked by melee (if the weapon is also configured for melee)");
		villagerDeBuffMelee = config.getFloat("VillagerDe-BuffMelee", "stats", .75F, .25F, 1.0F, "How much of a fraction of damage an item would cause if held by player will be caused by villager");
		villagersPerDoor = config.getFloat("Villagers per door", "general", 1, .33f, 4, "Villagers will increase population size (given they are well fed) to a maximum of \"this number\" x \"# of village doors\"");
		villagerHealth = config.getFloat("Villager health", "stats", 20f, 20f, 60f, "Base health of villager");
		villagerBaseArmour = config.getFloat("Villager base armour", "stats", 0.0f, 0f, 20f, "Base armour value of the villager, try to keep this low because armour can only add up to a certain point");
		meleeAttackCooldown = config.getFloat("Melee attack cooldown fraction", "attack", 1f, .1f, 1.5f, "Multiplied by the number of ticks a weapon uses to cooldown to increase or decrease attack interval");
		attackReach = config.getFloat("Attack-reach", "attack", 2f, 1f, 5f, "Attack reach of Villager in blocks");
		attackReachSq = attackReach * attackReach;
		targetDistance = config.getInt("Targeting Range", "attack", 32, 16, 128, "How far a villagers will seek out a fiend");
		friendlyFireSearchRange = config.getInt("Friendly Fire Check Range", "general", 16, 1, 64, "How far out villager's checking for friendlies goes (roughly in blocks), may bear upon perforamce and higher numbers may make villagers more hesitant to fire.");
		followRange = config.getFloat("Follow-range", "general", 16f, 3f, 64f, "How far a villager can stray from a player to attack an enemy.");
		followRangeNormal = config.getFloat("Follow-range; No Enemy", "general", 6f, 2f, 64f, "How far a villager can stray from a player when no enemy present and is following.");
		blockChance = config.getFloat("Block Chance", "attack", .6f, 0f, 1f, "how likely a villager's attempted block is to succeed");
		commandDist = config.getFloat("Command Distance", "general", 250f, 0f, 300f, "maximum distance in blocks a ray trace will go when right click the baton to order a villager movement");
		dailyBread = config.getFloat("Daily Bread", "general", 3f, 1f, 20f, "how much food saturation a villager will consume as measured in \"bread per day\" while drafted");
		collectFoodThreshold = config.getFloat("Food Refill Threshold", "general", 16f, 1f, 256f, "how far the food saturation of a villager will decrease before villager goes to refill inventory at food store");
		movementSpeed = config.getFloat("Movement Speed", "general", .67f, .25f, 1f, "how fast the villager moves. Default for villagers in MC is .55");
		debug = config.getBoolean("Debug", "general", false, "more Log info");
		renderItemsAndArmour = config.getBoolean("Render items and armour", "general", true, "Do you want your client to render items and armour on villagers? May conflict with other mods.");
		
		if(postinit == LoadState.SYNC || postinit == LoadState.POSTINIT)
		{
			readEntryJson();
			configuredGuns.values().forEach(rangeEntries -> {
				rangeEntries.forEach(rangeEntry ->{
					rangeEntry.init();
				});
			});
			
			if(!whiteListMobs)
			{
				CommonProxy.TARGETS.add(EntityMob.class);
			}
			for(String s : attackableMobs)
				CommonProxy.TARGETS.add(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(s)).getEntityClass());
			
			for(String s : rangeAttackBlacklist)
				CommonProxy.RANGE_BLACKLIST.add(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(s)).getEntityClass());
			
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

	public static void readEntryJson() throws JsonParseException, IOException
	{
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		File dir = new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null)
		{
			boolean flag = false;
			for (File child : directoryListing)
			{
				if(!flag && child.getName().equals("gunConfigFile.json"))
				{
					flag = readJson(gson, child);
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
		File configJson = new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/gunConfigFile.json");
		Writer gunConfigFile = new FileWriter(configJson);
		gunConfigFile.write(GenFileStrings.WEAPON_CONFIG_JSON);
		gunConfigFile.close();
		
		readJson(gson, configJson);
		
		Writer gunConfigReadme = new FileWriter(new File(Loader.instance().getConfigDir(), "improvedvils/rangedAttackEntries/Readme.txt"));
		gunConfigReadme.write(GenFileStrings.README);
		gunConfigReadme.close();
	}
	
	public static boolean readJson(Gson gson, File child) throws FileNotFoundException
	{
		boolean flag;
		flag = true;
		JsonReader jsonReader = new JsonReader(new FileReader(child));
		Type type = new TypeToken<Map<WeaponBrooksData, ArrayList<RangeAttackEntry>>>(){}.getType();
		ConfigHandler.configuredGuns = gson.fromJson(jsonReader, type);
		return flag;
	}

	@Nullable
	public static WeaponBrooksData weaponFromItemName(String s) 
	{
		for(WeaponBrooksData data : configuredGuns.keySet())
		{
			if(data.itemUnlocalizedName.equals(s))
				return data;
		}
		return null;
	}
}
