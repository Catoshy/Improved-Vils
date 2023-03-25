package com.joshycode.improvedmobs.network;

import java.util.Set;

import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.item.ItemMarshalsBaton;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilCommandPacket implements IMessage {

	BlockPos pos;
	
	public VilCommandPacket() {}
	
	public VilCommandPacket(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int x, y, z;
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	public static class Handler implements IMessageHandler<VilCommandPacket, IMessage> {

		@Override
		public IMessage onMessage(VilCommandPacket message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if(world.isAreaLoaded(message.pos, 1)) {
				ItemStack stack = player.getHeldItemMainhand();
				IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				
				if(cap != null) {
					Set<Entity> villagers = ItemMarshalsBaton.getEntitiesByUUID(cap.getVillagersSelected(), world);
					for(Entity e : villagers) {
						((EntityVillager)e).tasks.taskEntries.forEach(t -> {
							if(t.action instanceof VillagerAICampaignMove) {
								System.out.println(e.getUniqueID() + " found AI for movement, BlockPos is " + message.pos.toString());
								((VillagerAICampaignMove)t.action).giveObjectiveBlockPos(message.pos);
							}
						});
					}
				}
			}
			return null;
		}
		
	}
}
