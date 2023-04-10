package com.joshycode.improvedvils.network;

import java.util.Set;

import com.joshycode.improvedvils.capabilities.VilCapabilityMethods;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.item.ItemMarshalsBaton;

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
	int chunkx;
	int chunkz;
	
	public VilCommandPacket() 
	{ 
		this.chunkx = Integer.MAX_VALUE; 
		this.chunkz = Integer.MAX_VALUE;
	}
	
	public VilCommandPacket(BlockPos pos) 
	{
		this.pos = pos;
	}
	
	public VilCommandPacket(int x, int z) 
	{
		this(BlockPos.fromLong(Long.MAX_VALUE));
		this.chunkx = x;
		this.chunkz = z;
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.chunkx);
		buf.writeInt(this.chunkz);
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		int x, y, z;
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		this.chunkx = buf.readInt();
		this.chunkz = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	public static class Handler implements IMessageHandler<VilCommandPacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilCommandPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			ItemStack stack = player.getHeldItemMainhand();					
			IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
			
			if(cap != null) 
			{
				if(message.pos.toLong() != Long.MAX_VALUE) 
				{
					if(world.isAreaLoaded(message.pos, 1)) 
					{
						Set<Entity> villagers = ItemMarshalsBaton.getEntitiesByUUID(cap.getVillagersSelected(), world);
						for(Entity e : villagers) 
						{
							VilCapabilityMethods.setCommBlockPos((EntityVillager) e, message.pos);
						}
					}
				} 
				else 
				{
					if(world.isChunkGeneratedAt(message.chunkx, message.chunkz)) 
					{
						Set<Entity> villagers = ItemMarshalsBaton.getEntitiesByUUID(cap.getVillagersSelected(), world);
						for(Entity e : villagers) 
						{
							 ((EntityVillager) e).tasks.taskEntries.forEach(t -> {
								 if(t.action instanceof VillagerAICampaignMove) 
								 {
									 ((VillagerAICampaignMove) t.action).giveObjectiveChunkPos(message.chunkx, message.chunkz);
								 }
							 });
						}
					}
				}
			}
			return null;
		}		
	}
}
