package com.joshycode.improvedmobs.util;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityVillager;

public class VilAttributes {

	public static final IAttribute VIL_DAMAGE = new RangedAttribute(null, "iv.vil_damage", 1.0D, 0.0D, 1.0D);
	
	public static void apply(EntityVillager e) {
		e.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(45.0D);
	}
	
	public static void applyConstr(EntityVillager e) {
		e.getAttributeMap().registerAttribute(VIL_DAMAGE);
	}
}
