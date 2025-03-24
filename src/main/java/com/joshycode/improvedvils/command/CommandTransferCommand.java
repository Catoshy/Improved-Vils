package com.joshycode.improvedvils.command;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandTransferCommand extends CommandBase{
	
	@Override
	public String getName() 
	{
		return "transferCommand";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "commands.transferCommand.usage";
	}

	@Override
	public List<String> getAliases() 
	{
		return Arrays.<String>asList("giveCommand");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length < 1)
			throw new WrongUsageException("commands.transferCommand.usage", this.getUsage(sender));
		
		if(!(sender.getCommandSenderEntity() instanceof EntityPlayer) || ((EntityPlayer) sender.getCommandSenderEntity()).isSpectator()) return;
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		IMarshalsBatonCapability officer = player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
		
		boolean emptyFlag = true;
		for(Set<UUID> platoon : officer.getPlatoonMap().values())
		{
			if(platoon.isEmpty()) emptyFlag = false;
		}
		
		if(emptyFlag)
		{
			player.sendMessage(new TextComponentString("There are no villagers under your command. Nothing done."));
			return;
		}
		
		ItemStack commandLetter = new ItemStack(CommonProxy.ItemHolder.LETTER);
		commandLetter.setStackDisplayName(String.join(" ", args));
		commandLetter.getCapability(CapabilityHandler.COMMAND_LETTER_CAPABILITY, null)
			.attachInfo(officer.getfoodStoreMap(), officer.getkitStoreMap(), officer.getPlatoonMap());
		
		int slot = player.inventory.getFirstEmptyStack();
		if(slot != -1)
		{
			player.inventory.setInventorySlotContents(slot, commandLetter);
		}
		else
		{
            EntityItem entityitem = new EntityItem(player.getEntityWorld(), player.posX, player.posY + player.eyeHeight, player.posZ, commandLetter);
            player.getEntityWorld().spawnEntity(entityitem);
		}
		officer.clearCommand();
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) 
	{
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) 
	{
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) 
	{
		return false;
	}
}
