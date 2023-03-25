package com.joshycode.improvedmobs.network;

import java.util.UUID;

import com.joshycode.improvedmobs.ClientProxy;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.entity.ai.VillagerAIGuard;
import com.joshycode.improvedmobs.handler.CapabilityHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilGuardPacket implements IMessage {

	int id;
	boolean guardState;
	BlockPos pos;
	
	public VilGuardPacket() {}
	
	public VilGuardPacket(BlockPos pos, int entityId, boolean success) {
		this.pos = pos;
		this.id = entityId;
		this.guardState = success;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(this.guardState);
		buf.writeInt(this.id);
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int x, y, z;
		this.guardState = buf.readBoolean();
		this.id = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	public static class ServerHandler implements IMessageHandler<VilGuardPacket, IMessage> {

		@Override
		public IMessage onMessage(VilGuardPacket message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.id);
			if(e instanceof EntityVillager)  {
				if(getPlayerId((EntityVillager) e).equals(player.getUniqueID())) {
					if(message.guardState) {
						System.out.println("Setting guard pos ... " + e.getPosition().toString());
						setGuardBlock(e.getPosition(), (EntityVillager) e);
					} else {
						clearGuardPos((EntityVillager) e);
					}
				}
			}
			if(message.guardState)
				return new VilGuardPacket(e.getPosition(), 1, true);
			else
				return new VilGuardQuery(2);
		}
		
		private void clearGuardPos(EntityVillager entity) {
			try {
				entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).clearGuardPos();
			} catch (NullPointerException e) {}
		}

		private void setGuardBlock(BlockPos pos, EntityVillager entity) {
			try {
				entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(pos);
			} catch (NullPointerException e) {}
		}
		
		private UUID getPlayerId(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
			} catch (NullPointerException e) {}
			return UUID.randomUUID();
		}
	}
	
	public static class ClientHandler implements IMessageHandler<VilGuardPacket, IMessage> {

		@Override
		public IMessage onMessage(VilGuardPacket message, MessageContext ctx) {
			ClientProxy.updateVillagerGuardGUIInfo(message.pos, message.id);
			return null;
		}
		
	}
}
