package com.joshycode.improvedvils.entity.ai;

import java.util.Map;

public class RangeAttackEntry {

	public final float damage;
	public final int coolDown;
	public final int burstCoolDown;
	public final int shotsForBurst;
	public final int projectiles;
	public final RangeAttackType type;
	private Map<String, Integer> consumables;
	private String handItem;

	/**
	 * @param dmg Damage the projectile shall do to the intended target. Multiply value by 8 to get number
	 * of hearts. Irrelevant if Entry is Bow
	 * @param coolDown Ticks between shots/bursts
	 * @param burstCoolDown Ticks between shots in a bursts
	 * @param burstCount How many shots in a burst (if app.)
	 * @param type Bow, Shingleshot, Full-auto, etc.
	 * @param consumables Map of items to be consumed each shot and how many. String shall be unlocalized name
	 * of the item. Int shall be number of items to be consumed.
	 */
	public RangeAttackEntry(String mainItem, float dmg, int coolDown, int burstCoolDown, int burstCount, RangeAttackType type, Map<String, Integer> consumables)
	{
		this.handItem = mainItem;
		this.damage = dmg;
		this.coolDown = coolDown;
		this.burstCoolDown = burstCoolDown;
		this.shotsForBurst = burstCount;
		this.type = type;
		this.consumables = consumables;
		this.projectiles = 1;
	}
	
	public RangeAttackEntry(float dmg, int coolDown, int burstCoolDown,int burstCount, int projectiles, RangeAttackType type, Map<String, Integer> consumables)
	{
		this.damage = dmg;
		this.coolDown = coolDown;
		this.burstCoolDown = burstCoolDown;
		this.shotsForBurst = burstCount;
		this.type = type;
		this.consumables = consumables;
		this.projectiles = projectiles;
	}
	
	public String getHandItem()
	{
		return handItem;
	}

	public void setHandItem(String handItem)
	{
		this.handItem = handItem;
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
}
