package com.joshycode.improvedmobs.network;

import com.joshycode.improvedmobs.entity.ai.VillagerAIGuard;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilGuardReturnState implements IMessage {
	
	protected VilGuardReturnState() {}

	int villId;

	protected VilGuardReturnState(int villId) {
		super();
		this.villId = villId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
	
	public static class Handler implements IMessageHandler<VilGuardReturnState, IMessage> {

		@Override
		public IMessage onMessage(VilGuardReturnState message, MessageContext ctx) {
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity e = world.getEntityByID(message.villId);
			if(e instanceof EntityVillager)
				((EntityVillager) e).tasks.taskEntries.forEach(t -> {
					if(t.action instanceof VillagerAIGuard) {
						((VillagerAIGuard) t.action).returnState();
					}
				});
			return null;
		}
		
	}

}
