package com.joshycode.improvedvils.util;

public class GenFileStrings {

	public static final String README = 
			" Each entry in this Json file represents an item in game that the villager may use as either a gun or bow, "
		+ "\n along with the items that will be used up when using said item as a weapon."
		+ "\n The 'list' is truely a map, or list of two values which are both linked together."
		+ "\n The map begins with two left brackets, '[[' after this is a left parenthesis '{', which marks the begining of the weapon info section"
		+ "\n Here is an example which you may use, simply by removing the double dashes which comment it out."
		+ "\n The item names work for the Reforged Mod"
		+ "\n some important info to keep in mind;"
		+ "\n 1. Any number without a period like '0.0' is an integer. It is an error to use anything other than whole numbers here"
		+ "\n 2. You must use the 'unlocalized' name of the items both in the consumables or main item section."
		+ "\n 	If you do not know the unlocalized name of an item, you can go into the mod's jar or zip file and delete the .lang files."
		+ "\n 	These should be in the jar file under the folders 'assets' then 'modname (what ever that is)' then 'lang' -or- 'language'"
		+ "\n	Then, once in game, the items for that mod will only have the unlocalized name displayed when you hover over it in game."
		+ "\n 3. There are 5 Ranged Attack Types; BOW, SINGLESHOT, SHOT, BURST, & AUTO. These determine how the weapon is used. SHOT means shotgun"
		+ "\n 4. Be careful not to omit commas or brackets. Any new entry must be separated by a comma and is contained within the brackets."
		+ "\n 5. Be extra careful to avoid spelling mistakes or smaller errors in type names (the names in the quotations). "
		+ "\n 	 Gson may not throw an error even though it will result in incorrect or undefined behavior ingame."
		+ "\n"
		+ "\n 'coolDown' is ticks between shots or bursts"
		+ "\n 'burstCoolDown is ticks between shots IN a burst"
		+ "\n 'shotsForBursts' means how many bangs are in a burst, the M16 had 3"
		+ "\n 'projectiles' means the number of projectiles the 'SHOT' shotgun will shoot, if not a 'SHOT' type then number means nothing"
		+ "\n 'meleeInRange' means the villager will 'fix bayonets!' and go hands on if the bad guy gets too close"
		+ "\n 'farnessFactor' means the that when set to true, the villager will shoot faster (faster cooldown) when enemies are closer."
		+ "\n      This is more for semi-auto type weapons and bows, when shooting quickly at far off enemies will drain ammo, but close up"
		+ "\n      enemies risk harming or killing the villager."
		+ "\n  The ballistic section determines how the bullet flies and how much damages it does, means nothing if of 'BOW' type."
		+ "\n 'mass' is in grams, and determines deceleration & damage"
		+ "\n The low and high coefficients are based on the behaviour of projectiles transitioning from supersonic (high) to subsonic (low)"
		+ "\n You can do some reading on this if you like, but the coefficient determines how much the projectile slows down due to air resistance."
		+ "\n The higher the number the more it slows."
		+ "\n 'velocity' is in blocks per tick, (or meters per second divided by 20)"
		+ "\n 'inaccuraccy' means how inaccurate it is, higher is worse. a number of about 5 will make hits 1 out of every 4 shots at an enemy 32 blocks away on normal."
		+ "\n The difficulty also contributes in inaccuracy, (harder is worse)."
		+ "\n"
		+ "[[\n"
		+ "	{\n"
		+ "	\"coolDown\":60,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":0,\n"
		+ "	\"projectiles\":0,\n"
		+ " \"attackRange\":16.0 \n"
		+ "	\"meleeInRange\":false,\n"
		+ " \"farnessFactor\":true,\n"
		+ "	\"itemUnlocalizedName\":\"item.bow\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":0.0,\n"
		+ "		\"low_coefficient\":0.0,\n"
		+ "		\"high_coefficient\":0.0,\n"
		+ "		\"velocity\":0.0,\n"
		+ "		\"inaccuracy\":0.0\n"
		+ "		},\n"
		+ "		\"type\":\"BOW\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.arrow\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"meleeInRange\":false,\n"
		+ " \"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]]";
	public static final String WEAPON_CONFIG_JSON =
		"[[\n"
		+ "	{\n"
		+ "	\"coolDown\":60,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":0,\n"
		+ "	\"projectiles\":0,\n"
		+ "	\"attackRange\":0,\n"
		+ "	\"meleeInRange\":false,\n"
		+ "	\"farnessFactor\":true,\n"
		+ "	\"itemUnlocalizedName\":\"item.bow\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":0.0,\n"
		+ "		\"low_coefficient\":0.0,\n"
		+ "		\"high_coefficient\":0.0,\n"
		+ "		\"velocity\":0.0,\n"
		+ "		\"inaccuracy\":0.0\n"
		+ "		},\n"
		+ "		\"type\":\"BOW\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.arrow\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":false,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":true,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.diamond_musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":true,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.iron_musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":true,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.golden_musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":true,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.stone_musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":20.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":30.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]\n"
		+ ",\n"
		+ "[\n"
		+ "	{\n"
		+ "	\"coolDown\":300,\n"
		+ "	\"burstCoolDown\":0,\n"
		+ "	\"shotsForBurst\":1,\n"
		+ "	\"projectiles\":1,\n"
		+ "	\"attackRange\":32.0,\n"
		+ "	\"meleeInRange\":true,\n"
		+ "	\"farnessFactor\":false,\n"
		+ "	\"itemUnlocalizedName\":\"item.wooden_musket\"\n"
		+ "	},\n"
		+ "	[{\"ballisticData\":\n"
		+ "		{\n"
		+ "		\"mass\":15.0,\n"
		+ "		\"low_coefficient\":0.5,\n"
		+ "		\"high_coefficient\":0.9,\n"
		+ "		\"velocity\":20.0,\n"
		+ "		\"inaccuracy\":3.0\n"
		+ "		},\n"
		+ "		\"type\":\"SINGLESHOT\",\n"
		+ "		\"consumables\":\n"
		+ "			{\n"
		+ "			\"item.musket_bullet\":1\n"
		+ "			}\n"
		+ "	}]\n"
		+ "]]";
}