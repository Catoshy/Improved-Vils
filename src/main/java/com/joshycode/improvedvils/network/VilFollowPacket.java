package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.VilPlayerDealData;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilFollowPacket implements IMessage {

	int id;
	boolean followState;

	public VilFollowPacket() { this.id = 0; this.followState = false; }

	public VilFollowPacket(int id, boolean follow)
	{
		this.id = id;
		this.followState = follow;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(id);
		buf.writeBoolean(followState);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.id = buf.readInt();
		this.followState = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<VilFollowPacket, IMessage> {

		@Override
		public IMessage onMessage(VilFollowPacket message, MessageContext ctx)
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				Entity e = world.getEntityByID(message.id);
	
				if(e instanceof EntityVillager)
				{
					if(player.getUniqueID().equals(VilMethods.getPlayerId((EntityVillager) e)))
					{
						VilMethods.setGuardBlock((EntityVillager) e, null);
						if(message.followState)
						{
							VilMethods.setFollowState((EntityVillager) e, true);
						}
						else
						{
							VilMethods.setFollowState((EntityVillager) e, false);
						}
					}
					VillagerPlayerDealMethods.updateGuiForClient((EntityVillager) e, player);
				}
			});
			return null;
		}
	}
}
