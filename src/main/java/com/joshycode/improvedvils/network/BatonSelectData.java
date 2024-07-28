package com.joshycode.improvedvils.network;

import java.util.Map;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.VillagerListPacket.BatonBefolkPacket;
import com.joshycode.improvedvils.util.BatonDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BatonSelectData implements IMessage {

	int platoon;
	
	public BatonSelectData() {}
	
	public BatonSelectData(int platoon)
	{
		this.platoon = platoon;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.platoon = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(platoon);
	}
	
	public static class Handler implements IMessageHandler<BatonSelectData, IMessage>{

		@Override
		public IMessage onMessage(BatonSelectData message, MessageContext ctx) 
		{
			ImprovedVils.proxy.setHUDinfo(message.platoon);
			return null;
		}
	
	}
	
	public static class BatonSelectServerData extends BatonSelectData {
		
		public BatonSelectServerData() {}
		
		public BatonSelectServerData(int platoon)
		{
			super(platoon);
		}
		
		public static class Handler implements IMessageHandler<BatonSelectServerData, IMessage>{

			@Override
			public IMessage onMessage(BatonSelectServerData message, MessageContext ctx) 
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					EntityPlayerMP player = (EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx);
					ItemStack stack;
					if(player.getHeldItemMainhand().getItem() == CommonProxy.ItemHolder.BATON)
						stack = player.getHeldItemMainhand();
					else if(player.getHeldItemOffhand().getItem() == CommonProxy.ItemHolder.BATON)
						stack = player.getHeldItemOffhand();
					else
						return;
					
					player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null).setPlatoon(message.platoon / 10, message.platoon % 10);
					
					Map<Integer, UUID> villagerIds = BatonDealMethods.getEntityIDsFromBatonPlatoon(player);
					Map<Integer, Tuple<Boolean[], Integer>> villagerInfo = BatonDealMethods.getVillagerCapabilityInfoAppendMap(villagerIds.keySet(), player.world);
					NetWrapper.NETWORK.sendTo(new BatonBefolkPacket(villagerIds, villagerInfo), player);
				});
				return null;
			}
		
		}
	}

}
