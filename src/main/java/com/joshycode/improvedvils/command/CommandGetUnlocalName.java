package com.joshycode.improvedvils.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandGetUnlocalName implements ICommand {

	@Override
	public int compareTo(ICommand arg0)
	{
		return 0;
	}

	@Override
	public String getName() 
	{
		return "getUnlocalizedName";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "commands.getUnlocalizedName.usage";
	}

	@Override
	public List<String> getAliases() 
	{
		return Arrays.<String>asList("getUnlocalizedName");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(!(sender.getCommandSenderEntity() instanceof EntityPlayer) || ((EntityPlayer) sender.getCommandSenderEntity()).isSpectator()) return;
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		String name = player.getHeldItemMainhand().getUnlocalizedName();
		player.sendMessage(new TextComponentString("Unlocalized name for item in main Hand:\"" + name + "\""));
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
