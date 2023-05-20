package com.joshycode.improvedvils.network;

import java.util.Set;

import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.item.ItemMarshalsBaton;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilFoodStorePacket extends BlockPosPacket implements IMessage {
	
	public VilFoodStorePacket() {}
	
	public VilFoodStorePacket(BlockPos pos) {
		super(pos);
	}

	public static class Handler implements IMessageHandler<VilFoodStorePacket, IMessage> 
	{

		@Override
		public IMessage onMessage(VilFoodStorePacket message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = ctx.getServerHandler().player.getServerWorld();			
			IMarshalsBatonCapability cap = player.getHeldItemMainhand().getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
			if(!world.getBlockState(message.pos).getBlock().hasTileEntity(world.getBlockState(message.pos))) return null;
		
			if(cap != null &&  world.getTileEntity(message.pos) != null) 
			{
				cap.setPlatoonFoodStore(message.pos);
				Set<Entity> villagers = ItemMarshalsBaton.getEntitiesByUUID(cap.getVillagersSelected(), world);
				for(Entity e : villagers) 
				{
					;
					e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFoodStore(message.pos);
				}
			}
			return null;
		}
		
	}
}
