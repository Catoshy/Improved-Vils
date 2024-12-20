package com.joshycode.improvedvils.network;

import java.util.Map;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy.ItemHolder;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.gui.GuiBatonTroopSettings;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.VillagerListPacket.BatonBefolkPacket;
import com.joshycode.improvedvils.util.BatonDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class BlankNotePacket implements IMessage {

	public static class BatonBefolkQuery extends BlankNotePacket {
		
		public static class Handler implements IMessageHandler<BatonBefolkQuery, IMessage>{

			@Override
			public IMessage onMessage(BatonBefolkQuery message, MessageContext ctx) 
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					
					EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
					if(serverPlayer.getHeldItem(EnumHand.MAIN_HAND).getItem() != ItemHolder.BATON && serverPlayer.getHeldItem(EnumHand.OFF_HAND).getItem() != ItemHolder.BATON)
						return;
					
					Map<Integer, UUID> villagerIds = BatonDealMethods.getEntityIDsFromBatonPlatoon(serverPlayer);
					Map<Integer, Tuple<Boolean[], Integer>> villagerInfo = BatonDealMethods.getVillagerCapabilityInfoAppendMap(villagerIds.keySet(), serverPlayer.world);
					
					if(ConfigHandler.debug)
						Log.info("BatonBefolkQuery, BlankNotePacket ids are... %s", villagerIds);
					
					NetWrapper.NETWORK.sendTo(new BatonBefolkPacket(villagerIds, villagerInfo), serverPlayer);
				});
				return null;
			}
		}
	}
	
	public static class WarnNoRoom extends BlankNotePacket {
		
		public static class Handler implements IMessageHandler<WarnNoRoom, IMessage>{

			@Override
			public IMessage onMessage(WarnNoRoom message, MessageContext ctx) 
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					if(Minecraft.getMinecraft().currentScreen instanceof GuiBatonTroopSettings)
					{
						((GuiBatonTroopSettings) Minecraft.getMinecraft().currentScreen).tooManyToMove();
					}
				});
				return null;
			}
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}

}
