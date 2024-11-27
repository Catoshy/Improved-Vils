package com.joshycode.improvedvils.command;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.joshycode.improvedvils.util.LookHelper;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CommandGetEntityName implements ICommand {

	@Override
	public int compareTo(ICommand arg0) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() 
	{
		return "getEntityName";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "commands.entityName.usage";
	}

	@Override
	public List<String> getAliases() 
	{
		return Collections.<String>emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(!(sender.getCommandSenderEntity() instanceof EntityPlayer) || ((EntityPlayer) sender.getCommandSenderEntity()).isSpectator()) return;
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		RayTraceResult result = LookHelper.checkForFirendlyFire(player, player.getEntityWorld(), 0F, 16).a;
		if(result != null && result.entityHit != null)
			for(Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries())
				if(entry.getValue().getEntityClass().equals(result.entityHit.getClass()))
					player.sendMessage(new TextComponentString("Entity name: \"" + entry.getKey().getResourceDomain() + ":" + entry.getKey().getResourcePath() + "\""));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) 
	{
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) 
	{
		return false;
	}

}
