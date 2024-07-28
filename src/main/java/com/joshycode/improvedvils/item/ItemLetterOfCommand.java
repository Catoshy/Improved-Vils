package com.joshycode.improvedvils.item;

import java.util.Set;
import java.util.UUID;

import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.itemstack.ILetterOfCommandCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemLetterOfCommand extends Item {

	
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItemMainhand();
		if(!world.isRemote && stack.getItem() instanceof ItemLetterOfCommand)
		{
			ILetterOfCommandCapability toBeAssumed = stack.getCapability(CapabilityHandler.COMMAND_LETTER_CAPABILITY, null);
			IMarshalsBatonCapability officer = player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
			
			boolean emptyFlag = true;
			for(Set<UUID> platoon : officer.getPlatoonMap().values())
			{
				if(platoon.isEmpty()) emptyFlag = false;
			}
			
			if(!emptyFlag)
			{
				officer.attachInfo(toBeAssumed.getfoodStoreMap(), toBeAssumed.getkitStoreMap(), toBeAssumed.getPlatoonMap());
				player.sendMessage(new TextComponentString("Assumed command of " + stack.getDisplayName()));
				stack = ItemStack.EMPTY;
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
			player.sendMessage(new TextComponentString("You have villagers already under your command. You must give or destroy your command before you assume a new one. See '/transferCommand' & '/clearCommand'"));
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}
}
