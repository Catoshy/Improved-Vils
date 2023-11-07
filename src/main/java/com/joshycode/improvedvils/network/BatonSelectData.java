package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;

import io.netty.buffer.ByteBuf;
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
			if(ConfigHandler.debug)
				Log.info("received packet at client side for baton selection data");
			ImprovedVils.proxy.setHUDinfo(message.platoon);
			return null;
		}
	
	}

}
