package com.joshycode.improvedvils.network;

import org.jline.utils.Log;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MarshalKeyEvent implements IMessage {
	
	private int keyPressed;
	
	public MarshalKeyEvent() {}
	
	public MarshalKeyEvent(int deed)
	{
		this.keyPressed = deed;
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(keyPressed);
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.keyPressed = buf.readInt();
	}
	
	public static class Handler implements IMessageHandler<MarshalKeyEvent, IMessage>{

		@Override
		public IMessage onMessage(MarshalKeyEvent message, MessageContext ctx) 
		{
			if(ConfigHandler.debug)
				Log.info("MarshalKeyEvent onMessage");
			
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				//TODO offhand compatibility
				if(ConfigHandler.debug)
					Log.info("running deed for MarshalKeyEvent, player is: ", ctx.getServerHandler().player);
				
				EntityPlayer player = ImprovedVils.proxy.getPlayerEntity(ctx);
				ItemStack stack;
				if(player.getHeldItemMainhand().getItem() == CommonProxy.ItemHolder.BATON)
					stack = player.getHeldItemMainhand();
				else if(player.getHeldItemOffhand().getItem() != CommonProxy.ItemHolder.BATON)
					stack = player.getHeldItemOffhand();
				else
					return;
				
				IMarshalsBatonCapability batonCap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				int selectedPlatoon = batonCap.selectedUnit();
				
				if(message.keyPressed == CommonProxy.PLATOON_UP)
				{
					if(selectedPlatoon % 10 == 9)
					{
						batonCap.setPlatoon(selectedPlatoon / 10, 0);
					}
					else
					{
						selectedPlatoon++;
						int remainder = selectedPlatoon % 10;
						batonCap.setPlatoon(selectedPlatoon / 10, remainder);
					}
				}
				else if(message.keyPressed == CommonProxy.PLATOON_DOWN)
				{
					if(selectedPlatoon % 10 == 0)
					{
						batonCap.setPlatoon(selectedPlatoon / 10, 9);
					}
					else
					{
						selectedPlatoon--;
						int remainder = selectedPlatoon % 10;
						batonCap.setPlatoon(selectedPlatoon / 10, remainder);
					}
				}
				else if(message.keyPressed == CommonProxy.COMPANY_UP)
				{
					if(selectedPlatoon >= 40)
					{
						batonCap.setPlatoon(0, selectedPlatoon % 10);
					}
					else
					{
						batonCap.setPlatoon(selectedPlatoon / 10 + 1, 0);
					}
				}
				else if(message.keyPressed == CommonProxy.COMPANY_DOWN)
				{
					if(selectedPlatoon < 10)
					{
						batonCap.setPlatoon(4, selectedPlatoon % 10);
					}
					else
					{
						batonCap.setPlatoon(selectedPlatoon / 10 - 1, 0);
					}
				}
				else if(message.keyPressed == CommonProxy.BATON_GUI)
				{
					NetWrapper.NETWORK.sendTo(new OpenClientGui(selectedPlatoon), (EntityPlayerMP) player);
					return;
				}
			NetWrapper.NETWORK.sendTo(new BatonSelectData(batonCap.selectedUnit()), (EntityPlayerMP) player);
			});
			
			return null;
		}
		
	}
}