package com.joshycode.improvedvils.network;

import java.util.Set;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilCommandPacket extends BlockPosPacket implements IMessage {

	private TroopCommands command;
	
	public VilCommandPacket() 
	{ 
		super(); 
	}

	public VilCommandPacket(BlockPos pos, TroopCommands troopCommands)
	{
		super(pos);
		this.command = troopCommands;
	}
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		super.toBytes(buf);
		int id = this.command == null ? 0 : this.command.getID();
		buf.writeInt(id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		super.fromBytes(buf);
		this.command = TroopCommands.getCommand(buf.readInt());
	}

	public static class Handler implements IMessageHandler<VilCommandPacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilCommandPacket message, MessageContext ctx)
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				IMarshalsBatonCapability cap = player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
	
				if(cap != null)
				{
					if(world.isAreaLoaded(message.pos, 1))
					{
						Set<EntityVillager> villagers = ImprovedVils.proxy.getEntitiesByUUID(EntityVillager.class, cap.getVillagersSelected(), world);
						for(EntityVillager e : villagers)
						{
							if(VillagerPlayerDealMethods.getPlayerFealty(player, e) && !VilMethods.getFollowing(e) && VilMethods.getGuardBlockPos(e) == null)
							{
								VilMethods.setCommBlockPos(e, message.pos);
								VilMethods.setTroopFaring(e, message.command);
							}
						}
					}
				}
			});
			return new VilCommandPacket();
		}
	}
	
	public static class ClientHandler implements IMessageHandler<VilCommandPacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilCommandPacket message, MessageContext ctx) 
		{
			ImprovedVils.proxy.setTroopCommand(TroopCommands.NONE);
			return null;
		}
		
	}
}
