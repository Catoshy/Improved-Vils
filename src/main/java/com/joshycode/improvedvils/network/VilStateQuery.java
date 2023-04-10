package com.joshycode.improvedvils.network;

import java.util.UUID;

import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
import com.joshycode.improvedvils.entity.VillagerInvListener;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilStateQuery implements IMessage{

	int int1, int2;
	Vec3i vec;
	
	public VilStateQuery() { int1 = 0; int2 = 0; vec = Vec3i.NULL_VECTOR; }
	
	public VilStateQuery(int int1, int int2 , Vec3i vec) 
	{
		this.int1 = int1;
		this.int2 = int2;
		this.vec = vec;
	}
	
	public VilStateQuery(int int1, int int2) 
	{
		this(int1, int2, Vec3i.NULL_VECTOR);
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.int1);
		buf.writeInt(this.int2);
		buf.writeInt(vec.getX());
		buf.writeInt(vec.getY());
		buf.writeInt(vec.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.int1 = buf.readInt();
		this.int2 = buf.readInt();
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		this.vec = new Vec3i(x, y, z);
	}

	public static class ServerHandler implements IMessageHandler<VilStateQuery, IMessage> {

		@Override
		public IMessage onMessage(VilStateQuery message, MessageContext ctx) 
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.int1);
			Vec3i vec = null; int int1 = 0, int2 = 0;
			
			if(e instanceof EntityVillager)  
			{
				if(int2 == -2) 
				{
					resetInvListeners((EntityVillager) e);
				}
				else if(player.getUniqueID().equals(VilCapabilityMethods.getPlayerId((EntityVillager) e))) 
				{
					int1 += InventoryUtil.doesInventoryHaveItem
							(((EntityVillager) e).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 ? 1 : 0;
					int1 = VilCapabilityMethods.getHungry((EntityVillager) e) ? 0 : int1;
					
					if(int1 == 0) 
					{
						VilCapabilityMethods.setFollowing((EntityVillager) e, false);
						VilCapabilityMethods.setGuardBlock((EntityVillager) e, null);
					}
					
					int2 = int1;
					int1 += VilCapabilityMethods.getGuardBlockPos((EntityVillager) e) != null ? 1 : 0;
					int2 += VilCapabilityMethods.getFollowing((EntityVillager) e) ? 1 : 0;
					
					if(int1 == 2)
						vec = VilCapabilityMethods.getGuardBlockPos((EntityVillager) e);
				}
			}
			
			if(vec != null) 
			{
				return new VilStateQuery(int1, int2, vec);
			}
			return new VilStateQuery(int1, int2);
		}

		private void resetInvListeners(EntityVillager entity) 
		{
			try 
			{
				VillagerInvListener list = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getListener();
				entity.getVillagerInventory().removeInventoryChangeListener(list);
				entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setInvListener(null);
			} catch (NullPointerException e) {}
		}
	}
	
	public static class ClientHandler implements IMessageHandler<VilStateQuery, IMessage> {

		@Override
		public IMessage onMessage(VilStateQuery message, MessageContext ctx) 
		{
			 ClientProxy.updateVillagerGuardGUIInfo(message.vec, message.int1, message.int2);
			  return null;
		}

	}
}