package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilStateQuery implements IMessage {

	private boolean isClosed;
	private int villagerId;

	public VilStateQuery() { villagerId = 0; }

	public VilStateQuery(int villagerId)
	{
		this.villagerId = villagerId;
		this.isClosed = false;
	}

	public VilStateQuery(int vilId, boolean b) {
		this(vilId);
		this.isClosed = b;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.villagerId = buf.readInt();
		this.isClosed = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(villagerId);
		buf.writeBoolean(isClosed);
	}

	public static class Handler implements IMessageHandler<VilStateQuery, IMessage> {

		@Override
		public IMessage onMessage(VilStateQuery message, MessageContext ctx) {
			if(!message.isClosed)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
				{
					EntityPlayerMP player = ctx.getServerHandler().player;
					WorldServer world = ctx.getServerHandler().player.getServerWorld();
					ImprovedVils.proxy.updateGuiForClient((EntityVillager) world.getEntityByID(message.villagerId), player);
				});
			}
			else
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
				{
					WorldServer world = ctx.getServerHandler().player.getServerWorld();
					EntityVillager entity = (EntityVillager) world.getEntityByID(message.villagerId);
					if(ConfigHandler.debug)
						Log.info("closing out inv changed listener %s", entity);
					if(!entity.isDead)
					{
						entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(false);
					}
				});
			}
			return null;
		}
	}
}
