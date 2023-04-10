package com.joshycode.improvedvils.network;

import java.util.UUID;

import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
import com.joshycode.improvedvils.handler.CapabilityHandler;

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
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.id);
			int int2 = 0;
			
			if(e instanceof EntityVillager) 
			{
				if(player.getUniqueID().equals(VilCapabilityMethods.getPlayerId((EntityVillager) e))) 
				{
					VilCapabilityMethods.setGuardBlock((EntityVillager) e, null);
					if(message.followState) 
					{
						VilCapabilityMethods.setFollowState((EntityVillager) e, true);
						int2 = 2;
					} 
					else
					{
						VilCapabilityMethods.setFollowState((EntityVillager) e, false);
						int2 = 1;
					}
				}
			}
			return new VilStateQuery(1, int2);
		}
	}
}
