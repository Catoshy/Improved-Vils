package com.joshycode.improvedvils.util;

import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityVillager;

public class VilAttributes {

	public static final IAttribute VIL_DAMAGE = new RangedAttribute(null, "iv.vil_damage", 1.0D, 0.0D, 1.0D);

	public static void apply(EntityVillager e)
	{
		e.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(45.0D);
		e.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.55d);
        e.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		e.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ConfigHandler.villagerHealth);
		e.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ARMOR).setBaseValue(ConfigHandler.villagerBaseArmour);
	}

	public static void applyConstr(EntityVillager e)
	{
		e.getAttributeMap().registerAttribute(VIL_DAMAGE);
	}
}
