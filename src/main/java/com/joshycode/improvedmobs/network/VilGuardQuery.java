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
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilGuardQuery implements IMessage{

	int id;
	
	public VilGuardQuery() {}
	
	public VilGuardQuery(int entityId) {
		this.id = entityId;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.id);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.id = buf.readInt();
	}

	public static class ServerHandler implements IMessageHandler<VilGuardQuery, IMessage> {

		@Override
		public IMessage onMessage(VilGuardQuery message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.id);
			boolean flag = false, flag2 = false;
			if(e instanceof EntityVillager)  {
				if(getPlayerId((EntityVillager) e).equals(player.getUniqueID())) {
					flag = hasGuardBlockPos((EntityVillager) e);
					flag2 = InventoryUtil.doesInventoryHaveItem
							(((EntityVillager) e).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0;
					flag2 &= !getHungry((EntityVillager) e);
				}
			}
			if(flag2) {
				if(flag) {
					return new VilGuardQuery(1);
				}
				return new VilGuardQuery(2);
			}
			return new VilGuardQuery(0);
		}

		private boolean getHungry(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
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
	
	public static class ClientHandler implements IMessageHandler<VilGuardQuery, IMessage> {

		@Override
		public IMessage onMessage(VilGuardQuery message, MessageContext ctx) {
			 ClientProxy.updateVillagerGuardGUIInfo(null, message.id);
			  return null;
		}

	}
}