package com.joshycode.improvedmobs.util;

import com.joshycode.improvedmobs.CommonProxy;
import net.minecraft.entity.passive.EntityVillager;

public class PositionUtil {

	public static boolean isOutsideHomeDist(EntityVillager attacker) {
		if(!attacker.isWithinHomeDistanceCurrentPosition()) {
			if(InventoryUtil.doesInventoryHaveItem(((EntityVillager) attacker).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

}
