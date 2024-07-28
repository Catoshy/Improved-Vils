package com.joshycode.improvedvils.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandDestroyCommand implements ICommand {

	@Override
	public int compareTo(ICommand arg0)
	{
		return 0;
	}

	@Override
	public String getName() 
	{
		return "destroyCommand";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "commands.destroyCommand.usage";
	}

	@Override
	public List<String> getAliases() 
	{
		return Arrays.<String>asList("clearCommand");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(!(sender.getCommandSenderEntity() instanceof EntityPlayer) || ((EntityPlayer) sender.getCommandSenderEntity()).isSpectator()) return;
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		IMarshalsBatonCapability officer = player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
		if(!officer.isSureClearCommand(player.getEntityWorld().getWorldTime()))
		{
			player.sendMessage(new TextComponentString("Clearing command will destroy the enlisted command structure over your villagers. If you are sure repeat the command."));
			officer.setSureClearCommand(player.getEntityWorld().getWorldTime());
			return;
		}
		officer.clearCommand();
		player.sendMessage(new TextComponentString("Command cleared."));
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
