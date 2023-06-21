package com.joshycode.improvedvils.entity.ai;

import java.util.Map;

public class RangeAttackEntry {

	public static final class BallisticData {
		
		public final double mass;
		public final double low_coefficient;
		public final double high_coefficient;
		public final float velocity;
		public final float accuracy;
		
		public BallisticData(double mass, double low_coefficient, double high_coefficient, float velocity, float accuracy) 
		{
			this.mass = mass;
			this.low_coefficient = low_coefficient;
			this.high_coefficient = high_coefficient;
			this.velocity = velocity;
			this.accuracy = accuracy;
		}
		
		@Override
		public String toString()
		{
			String s = "mass;" + mass + "low_coefficient; " + low_coefficient + "high_coefficient; " + high_coefficient + "velocity; " + velocity;
			return s;
		}
	}
	
	public static final class WeaponBrooksData {
		
		public final int coolDown;
		public final int burstCoolDown;
		public final int shotsForBurst;
		public final int projectiles;
		public final boolean meleeInRange;
		public final String itemUnlocalizedName;
		
		public WeaponBrooksData(String itemUnlocalizedName, int coolDown, int burstCoolDown, int shotsForBurst, int projectiles, boolean meleeInRange) 
		{
			this.coolDown = coolDown;
			this.burstCoolDown = burstCoolDown;
			this.shotsForBurst = shotsForBurst;
			this.projectiles = projectiles;
			this.meleeInRange = meleeInRange;
			this.itemUnlocalizedName = itemUnlocalizedName;
		}
		
		@Override
		public String toString()
		{
			String s = "coolDown;" + coolDown + "burstCoolDown; " + burstCoolDown + "shotsForBurst; " + shotsForBurst + "projectiles; " + projectiles + "meleeInRange; " + meleeInRange + "itemUnlocalizedName; " + itemUnlocalizedName;
			return s;
		}
	}
	
	public final BallisticData ballisticData;
	public final RangeAttackType type;
	private Map<String, Integer> consumables;

	/**
	 * @param mass of the projectile, in grams
	 * @param low_coef Ballistic coefficient in subsonic range, generally about.43
	 * @param high_coef Ballistic coefficient in supersonic range, generally about .8
	 * @param velocity Velocity in blocks per tick (multiply by 20 for meters per sec)
	 * @param coolDown Ticks between shots/bursts
	 * @param burstCoolDown Ticks between shots in a bursts
	 * @param burstCount How many shots in a burst (if app.)
	 * @param type Bow, Shingleshot, Full-auto, etc.
	 * @param consumables Map of items to be consumed each shot and how many. String shall be unlocalized name
	 * of the item. Int shall be number of items to be consumed.
	 */
	public RangeAttackEntry(RangeAttackType type, Map<String, Integer> consumables, BallisticData data2)
	{
		this.type = type;
		this.consumables = consumables;
		this.ballisticData = data2;
	}

	public Map<String, Integer> getConsumables()
	{
		return consumables;
	}

	public void setConsumables(Map<String, Integer> consumables)
	{
		this.consumables = consumables;
	}

	public enum RangeAttackType
	{
		BOW,
		SINGLESHOT,
		SHOT,
		BURST,
		AUTO
	}
	
	@Override
	public String toString()
	{
		String s = "BallisticData; " + this.ballisticData.toString() + "RangeAttackType; " + type + "consumables; " + consumables;
		return s;
	}
}
