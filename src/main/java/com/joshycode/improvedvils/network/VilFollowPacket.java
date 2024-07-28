package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;

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
	boolean booleanState;

	public VilFollowPacket() { this.id = 0; this.booleanState = false; }

	public VilFollowPacket(int id, boolean follow)
	{
		this.id = id;
		this.booleanState = follow;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(id);
		buf.writeBoolean(booleanState);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.id = buf.readInt();
		this.booleanState = buf.readBoolean();
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
						VilMethods.setFollowState((EntityVillager) e, message.booleanState);
						VilMethods.setCommBlockPos((EntityVillager) e, null);
					}
					ImprovedVils.proxy.updateGuiForClient((EntityVillager) e, player);
				}
			});
			return null;
		}
	}
	
	public static class VilDutyPacket extends VilFollowPacket {
		
		public VilDutyPacket() {}
		
		public VilDutyPacket(int vilId, boolean duty) { super(vilId, duty); }
		
		public static class Handler implements IMessageHandler<VilDutyPacket, IMessage> {
			@Override
			public IMessage onMessage(VilDutyPacket message, MessageContext ctx)
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
							VilMethods.setFollowState((EntityVillager) e, false);
							VilMethods.setGuardBlock((EntityVillager) e, null);
							VilMethods.setDuty((EntityVillager) e, message.booleanState);
						}
						ImprovedVils.proxy.updateGuiForClient((EntityVillager) e, player);
					}
				});
				return null;
			}
		}
	}
}
