package com.joshycode.improvedmobs.network;

import java.util.UUID;

import com.joshycode.improvedmobs.ClientProxy;
import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.InventoryUtil;

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
	
	public VilStateQuery() {int1 = 0; int2 = 0; vec = Vec3i.NULL_VECTOR; }
	
	public VilStateQuery(int int1, int int2 , Vec3i vec) {
		this.int1 = int1;
		this.int2 = int2;
		this.vec = vec;
	}
	
	public VilStateQuery(int int1, int int2) {
		this(int1, int2, Vec3i.NULL_VECTOR);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.int1);
		buf.writeInt(this.int2);
		buf.writeInt(vec.getX());
		buf.writeInt(vec.getY());
		buf.writeInt(vec.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.int1 = buf.readInt();
		this.int2 = buf.readInt();
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		this.vec = new Vec3i(x, y, z);
	}

	public static class ServerHandler implements IMessageHandler<VilStateQuery, IMessage> {

		@Override
		public IMessage onMessage(VilStateQuery message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.int1);
			Vec3i vec = null; int int1 = 0, int2 = 0;
			if(e instanceof EntityVillager)  {
				if(getPlayerId((EntityVillager) e).equals(player.getUniqueID())) {;
					int1 += InventoryUtil.doesInventoryHaveItem
							(((EntityVillager) e).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 ? 1 : 0;
					int2 = int1;
					int1 += hasGuardBlockPos((EntityVillager) e) ? 1 : 0;
					int1 = getHungry((EntityVillager) e) ? 0 : int1;
					int2 += isFollowing((EntityVillager) e) ? 1 : 0;
					int2 = getHungry((EntityVillager) e) ? 0 : int2;
					if(int1 == 2)
						vec = getGuardPos((EntityVillager) e);
				}
			}
			
			if(vec != null) {
				return new VilStateQuery(int1, int2, vec);
			}
			return new VilStateQuery(int1, int2);
		}

		private Vec3i getGuardPos(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getGuardBlockPos();
			} catch (NullPointerException e) {}
			return null;
		}

		private boolean isFollowing(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isFollowing();
			} catch (NullPointerException e) {}
			return true;
		}

		private boolean getHungry(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isHungry();
			} catch (NullPointerException e) {}
			return true;
		}

		private boolean hasGuardBlockPos(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getGuardBlockPos() != null;
			} catch (NullPointerException e) {}
			return true;
		}

		private UUID getPlayerId(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
			} catch (NullPointerException e) {}
			return UUID.randomUUID();
		}

	}
	
	public static class ClientHandler implements IMessageHandler<VilStateQuery, IMessage> {

		@Override
		public IMessage onMessage(VilStateQuery message, MessageContext ctx) {
			 ClientProxy.updateVillagerGuardGUIInfo(message.vec, message.int1, message.int2);
			  return null;
		}

	}
}