package com.joshycode.improvedvils.network;

import java.util.UUID;

import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
import com.joshycode.improvedvils.entity.ai.VillagerAIGuard;
import com.joshycode.improvedvils.handler.CapabilityHandler;

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
	
	public VilGuardPacket(BlockPos pos, int entityId, boolean success) 
	{
		this.pos = pos;
		this.id = entityId;
		this.guardState = success;
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeBoolean(this.guardState);
		buf.writeInt(this.id);
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		int x, y, z;
		this.guardState = buf.readBoolean();
		this.id = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	public static class ServerHandler implements IMessageHandler<VilGuardPacket, IMessage> 
	{

		@Override
		public IMessage onMessage(VilGuardPacket message, MessageContext ctx) 
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.id);
			int int1 = 0;
			
			if(e instanceof EntityVillager) 
			{
				if(player.getUniqueID().equals(VilCapabilityMethods.getPlayerId((EntityVillager) e))) 
				{
					VilCapabilityMethods.setFollowState((EntityVillager) e, false);
					if(message.guardState) 
					{
						int1 = VilCapabilityMethods.setGuardBlock((EntityVillager) e, e.getPosition())? 2 : 1;
						((EntityVillager) e).tasks.taskEntries.forEach(t -> {
							if(t.action instanceof VillagerAIGuard) {
								((VillagerAIGuard) t.action).returnState();
							}
						});
					} 
					else 
					{
						int1 = VilCapabilityMethods.setGuardBlock((EntityVillager) e, null)? 1 : 2;
					}
				}
			}
			if(message.guardState)
				return new VilStateQuery(int1, 1, e.getPosition());
			else
				return new VilStateQuery(int1, 1);
		}
	}
}
