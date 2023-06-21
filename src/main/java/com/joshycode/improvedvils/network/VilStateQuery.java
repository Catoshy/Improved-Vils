package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
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
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			return VillagerPlayerDealMethods.getUpdateGuiForClient(world.getEntityByID(message.villagerId), player, message.isClosed);
		}
	}
}
