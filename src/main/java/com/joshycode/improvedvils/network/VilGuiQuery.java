package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ServerProxy;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilGuiQuery implements IMessage {

	private int villagerId;
	
	public VilGuiQuery() {}
	
	public VilGuiQuery(int villagerId) 
	{
		this.villagerId = villagerId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.villagerId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.villagerId);
	}

	public static class Handler implements IMessageHandler<VilGuiQuery, IMessage> {

		@Override
		public IMessage onMessage(VilGuiQuery message, MessageContext ctx) {
			ServerProxy.openVillagerGUI(ctx.getServerHandler().player, message.villagerId, ctx.getServerHandler().player.getServerWorld());
			return null;
		}
	}
}
