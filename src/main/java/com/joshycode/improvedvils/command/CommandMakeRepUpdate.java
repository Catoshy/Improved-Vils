package com.joshycode.improvedvils.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.joshycode.improvedvils.util.Pair;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;

public class CommandMakeRepUpdate extends CommandBase{

	@Override
	public String getName() 
	{
		return "updateVillageReputationNow";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "commands.updateVillageReputationNow.usage";
	}

	@Override
	public List<String> getAliases() 
	{
		return Arrays.<String>asList("updateVillageReputationNow");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		Village village = sender.getEntityWorld().getVillageCollection().getNearestVillage(sender.getPosition(), 0);
		if(village == null)
		{
			sender.sendMessage(new TextComponentString("No village found nearby."));
			return;
		}
		for(int i = 0; i < args.length; i++)
		{
			UUID playerId = server.getPlayerProfileCache().getGameProfileForUsername(args[i]).getId();
			if(playerId != null)
			{
				Pair<List<EntityVillager>, long[]> pair = VillagerPlayerDealMethods.getVillagePopulation(village, sender.getEntityWorld());
				List<EntityVillager> population = pair.a;
				long[] removeChunks = pair.b;
				
				int updateReputation = VillagerPlayerDealMethods.getVillageReputationFromMean(village, playerId, population) ;
				VillagerPlayerDealMethods.updateVillageReputation(sender.getEntityWorld(), village, playerId, updateReputation, population);
				VillagerPlayerDealMethods.putAwayChunks(sender.getEntityWorld(), removeChunks);
				sender.sendMessage(new TextComponentString("Updated player reputation for player " + args[i] + " UUID: " + playerId));
			}
			else
			{
				sender.sendMessage(new TextComponentString("No player found with username " + args[i]));
			}
		}
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 1;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) 
	{
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames()) : Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) 
	{
		return false;
	}
}
