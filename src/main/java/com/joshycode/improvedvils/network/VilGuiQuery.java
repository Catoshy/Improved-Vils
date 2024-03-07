package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.handler.VilPlayerDeal;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
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
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(new VilPlayerDeal(message.villagerId, (EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx), ImprovedVils.proxy.getWorld(ctx)));
			return null;
		}
	}
}
