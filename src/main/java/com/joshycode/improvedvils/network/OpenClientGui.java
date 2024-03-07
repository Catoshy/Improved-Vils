package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenClientGui implements IMessage {

	int selectedPlatoon;

	public OpenClientGui() { this.selectedPlatoon = -1; }
	
	public OpenClientGui(int selectedPlatoon) 
	{
		this.selectedPlatoon = selectedPlatoon;
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.selectedPlatoon = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.selectedPlatoon);
	}
	
	public static class Handler implements IMessageHandler<OpenClientGui, IMessage> {

		@Override
		public IMessage onMessage(OpenClientGui message, MessageContext ctx) 
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				ImprovedVils.proxy.getPlayerEntity(ctx).openGui(ImprovedVils.instance, CommonProxy.BATON_GUI_ID, ImprovedVils.proxy.getWorld(ctx), message.selectedPlatoon, 0, 0);
			});
			return null;
		}
	}

}
