package com.joshycode.improvedmobs.network;

import java.util.UUID;

import com.joshycode.improvedmobs.entity.ai.VillagerAIGuard;
import com.joshycode.improvedmobs.handler.CapabilityHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
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
			int int1 = 0;
			if(e instanceof EntityVillager)  {
				if(getPlayerId((EntityVillager) e).equals(player.getUniqueID())) {
					if(message.guardState) {
						System.out.println("Setting guard pos ... " + e.getPosition().toString());
						clearFollowState((EntityVillager) e);
						int1 = setGuardBlock((EntityVillager) e, e.getPosition())? 2 : 1;
						if(e instanceof EntityVillager)
							((EntityVillager) e).tasks.taskEntries.forEach(t -> {
								if(t.action instanceof VillagerAIGuard) {
									((VillagerAIGuard) t.action).returnState();
								}
							});
					} else {
						clearFollowState((EntityVillager) e);
						int1 = setGuardBlock((EntityVillager) e, null)? 1 : 2;
					}
				}
			}
			if(message.guardState)
				return new VilStateQuery(2, 1, e.getPosition());
			else
				return new VilStateQuery(int1, 1);
		}
		
		private void clearFollowState(EntityVillager e) {
			try {
				e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(false);
			} catch (NullPointerException ex) {}
		}

		private boolean setGuardBlock(EntityVillager entity, BlockPos pos) {
			try {
				entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(pos);
				return true;
			} catch (NullPointerException e) {return false; }
		}
		
		private UUID getPlayerId(EntityVillager entity) {
			try {
				return entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
			} catch (NullPointerException e) {}
			return UUID.randomUUID();
		}
	}
}
