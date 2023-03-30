package com.joshycode.improvedmobs.network;

import java.util.UUID;

import com.joshycode.improvedmobs.handler.CapabilityHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilFollowPacket implements IMessage {

	int id;
	boolean followState;
	
	public VilFollowPacket() {this.id = 0; this.followState = false;}
	
	public VilFollowPacket(int id, boolean follow) {
		this.id = id; 
		this.followState = follow;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeBoolean(followState);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.id = buf.readInt();
		this.followState = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<VilFollowPacket, IMessage> {

		@Override
		public IMessage onMessage(VilFollowPacket message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.id);
			int int2 = 0;
			if(e instanceof EntityVillager) {
				if(player.getUniqueID().equals(getId((EntityVillager) e))) {
					if(message.followState) {
						clearGuardState((EntityVillager) e);
						int2 = setFollowState((EntityVillager) e, true)? 2 : 1;
					} else {
						clearGuardState((EntityVillager) e);
						int2 = setFollowState((EntityVillager) e, false)? 1 : 2;
					}
				}
			}
			System.out.println("Serverside VIlFollowPackage - " +1+ " " + int2);
			return new VilStateQuery(1, int2);
		}

		private void clearGuardState(EntityVillager e) {
			try {
				e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(null);
			} catch (NullPointerException ex) {}
		}

		private UUID getId(EntityVillager e) {
			try {
				return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
			} catch (NullPointerException ex) {}
			return null;
		}

		private boolean setFollowState(EntityVillager e, boolean followState) {
			try {
				e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(followState);
				return true;
			} catch (NullPointerException ex) { return false; }
		}
		
	}
}
