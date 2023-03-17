package com.joshycode.improvedmobs.handler;

import java.util.Map;

import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry;
import com.joshycode.improvedmobs.entity.ai.RangeAttackEntry.RangeAttackType;

import net.minecraft.init.Items;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;

public class ConfigHandlerVil {
	
	public static boolean villagerGreifing; 
	public static Multimap<String, RangeAttackEntry> configuredGuns = ArrayListMultimap.create();
	public static int maxBurst;
	
	public static void testRegis() {
		Map<String, Integer> consume = new HashMap();
		consume.put(Items.DIAMOND.getUnlocalizedName(), 1);
		consume.put(Items.EMERALD.getUnlocalizedName(), 0);
		configuredGuns.put(Items.STICK.getUnlocalizedName(), new RangeAttackEntry(.125F, 80, 20, 1, RangeAttackType.SHOT, consume));
		Map<String, Integer> bow = new HashMap();
		bow.put(Items.ARROW.getUnlocalizedName(), 1);
		configuredGuns.put(Items.BOW.getUnlocalizedName(), new RangeAttackEntry(0, 100, 0, 1, RangeAttackType.BOW, bow));

	}
}
