package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.entity.ai.VillagerAIGuard;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilGuardPacket extends BlockPosPacket implements IMessage {

	int id;
	boolean guardState;

	public VilGuardPacket() {}

	public VilGuardPacket(BlockPos pos, int entityId, boolean success)
	{
		super(pos);
		this.id = entityId;
		this.guardState = success;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		super.toBytes(buf);
		buf.writeBoolean(this.guardState);
		buf.writeInt(this.id);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		super.fromBytes(buf);
		this.guardState = buf.readBoolean();
		this.id = buf.readInt();
	}

	public static class ServerHandler implements IMessageHandler<VilGuardPacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilGuardPacket message, MessageContext ctx)
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				Entity e = world.getEntityByID(message.id);
	
				if(e instanceof EntityVillager)
				{
					if(player.getUniqueID().equals(VilMethods.getPlayerId((EntityVillager) e)))
					{
						VilMethods.setFollowing((EntityVillager) e, false);
						VilMethods.setCommBlockPos((EntityVillager) e, null);
						if(message.guardState)
							VilMethods.setGuardBlock((EntityVillager) e, e.getPosition());
						else
							VilMethods.setGuardBlock((EntityVillager) e, null);
	
						((EntityVillager) e).tasks.taskEntries.forEach(t -> {
							if(t.action instanceof VillagerAIGuard) {
								((VillagerAIGuard) t.action).returnState();
							}
						});
					}
					ImprovedVils.proxy.updateGuiForClient((EntityVillager) e, player);
				}
			});
			return null;
		}
	}
}
