package com.joshycode.improvedvils.network;

import java.util.Set;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilCommandPacket extends BlockPosPacket implements IMessage {

	public VilCommandPacket() 
	{ 
		super(); 
	}

	public VilCommandPacket(BlockPos pos)
	{
		super(pos);
	}

	public static class Handler implements IMessageHandler<VilCommandPacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilCommandPacket message, MessageContext ctx)
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				IMarshalsBatonCapability cap = player.getHeldItemMainhand().getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
	
				if(cap != null)
				{
					if(world.isAreaLoaded(message.pos, 1))
					{
						Set<Entity> villagers = CommonProxy.getEntitiesByUUID(cap.getVillagersSelected(), world);
						for(Entity e : villagers)
						{
							if(VillagerPlayerDealMethods.getPlayerFealty(player, (EntityVillager) e) && !VilMethods.getFollowing((EntityVillager) e) && VilMethods.getGuardBlockPos((EntityVillager) e) == null)
							{
								VilMethods.setCommBlockPos((EntityVillager) e, message.pos);
							}
						}
					}
				}
			});
			return null;
		}
	}
}
