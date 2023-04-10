package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilEnlistPacket implements IMessage {
	
	private boolean isEnlisted;
	private int company;
	private int platoon;
	private int entityID;
	
	public VilEnlistPacket() 
	{
		this.entityID = 0;
		this.company = 0;
		this.platoon = 0;
		this.isEnlisted = false;
	}

	public VilEnlistPacket(int uuid, int company, int platoon, boolean isEnlisted) 
	{
		this.entityID = uuid;
		this.company = company;
		this.platoon = platoon;
		this.isEnlisted = isEnlisted;
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.entityID);
		buf.writeInt(this.company);
		buf.writeInt(this.platoon);
		buf.writeBoolean(this.isEnlisted);
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.entityID = buf.readInt();
		this.company = buf.readInt();
		this.platoon = buf.readInt();
		this.isEnlisted = buf.readBoolean();
	}
	
	public static class ServerHandler implements IMessageHandler<VilEnlistPacket, IMessage> {

		  @Override 
		  public IMessage onMessage(VilEnlistPacket message, MessageContext ctx) 
		  {
			  EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			  WorldServer server = ctx.getServerHandler().player.getServerWorld();
			  ItemStack stack = InventoryUtil.get1StackByItem(serverPlayer.inventory, CommonProxy.ItemHolder.BATON);
		    
			  if(stack != null) 
			  {
				  IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				  if(cap != null) 
				  {
					  if(message.isEnlisted) 
					  {	
						  cap.addVillager(server.getEntityByID(message.entityID).getUniqueID(), message.company, message.platoon);
						  return new VilEnlistPacket(0, message.company, message.platoon, true);
					  } 
					  else 
					  {
						  cap.removeVillager(server.getEntityByID(message.entityID).getUniqueID());
						  return new VilEnlistPacket(0, 0, 0, false);
					  }
				  }
			  }
		  return null;
		}
	}
	
	public static class ClientHandler implements IMessageHandler<VilEnlistPacket, IMessage> {

		@Override 
		public IMessage onMessage(VilEnlistPacket message, MessageContext ctx) 
		{
			ClientProxy.updateVillagerEnlistGUIInfo(message.isEnlisted, message.company, message.platoon);
			return null;
		}
	}
}
