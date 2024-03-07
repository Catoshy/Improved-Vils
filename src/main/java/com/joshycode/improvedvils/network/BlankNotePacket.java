package com.joshycode.improvedvils.network;

import java.util.Map;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy.ItemHolder;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.gui.GuiBatonStelling;
import com.joshycode.improvedvils.network.VillagerListPacket.BatonBefolkPacket;
import com.joshycode.improvedvils.util.BatonDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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
					
					EntityPlayerMP player = ctx.getServerHandler().player;
					ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
					
					if(stack.getItem() != ItemHolder.BATON)
						stack = player.getHeldItem(EnumHand.OFF_HAND);
					if(stack.getItem() != ItemHolder.BATON)
						return;
					
					Map<Integer, UUID> villagerIds = BatonDealMethods.getEntityIDsFromBatonPlatoon(player, stack);
					Map<Integer, Tuple<Boolean[], Integer>> villagerInfo = BatonDealMethods.getVillagerCapabilityInfoAppendMap(villagerIds.keySet(), player.world);
					
					NetWrapper.NETWORK.sendTo(new BatonBefolkPacket(villagerIds, villagerInfo), player);
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
					if(Minecraft.getMinecraft().currentScreen instanceof GuiBatonStelling)
					{
						((GuiBatonStelling) Minecraft.getMinecraft().currentScreen).tooManyToMove();
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
