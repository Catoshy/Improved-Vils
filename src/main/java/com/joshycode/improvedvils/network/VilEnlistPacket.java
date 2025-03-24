package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilEnlistPacket implements IMessage {

	private boolean isEnlisted;
	private int company;
	private int platoon;
	private int entityID;

	public VilEnlistPacket()
	{
		this.entityID = 0;
		this.company = 0;
		this.platoon = 0;
		this.isEnlisted = false;
	}

	public VilEnlistPacket(int id, int company, int platoon, boolean isEnlisted)
	{
		this.entityID = id;
		this.company = company;
		this.platoon = platoon;
		this.isEnlisted = isEnlisted;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.entityID);
		buf.writeInt(this.company);
		buf.writeInt(this.platoon);
		buf.writeBoolean(this.isEnlisted);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.company = buf.readInt();
		this.platoon = buf.readInt();
		this.isEnlisted = buf.readBoolean();
	}

	private VilEnlistPacket updateEnlistInfoForStack(EntityPlayer player, boolean isEnlisted, int company, int platoon, Entity entity, IImprovedVilCapability vilCap)
	{
		if(player != null)
		{
		 	IMarshalsBatonCapability cap = player.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
		 	if(cap != null)
		 	{
		 		if(isEnlisted)
		 		{
		 			BlockPos foodStore = cap.getPlatoonFoodStore(company, platoon);
		 			if(vilCap != null && foodStore != null)
		 				vilCap.setFoodStore(foodStore);
		 			
		 			BlockPos kitStore = cap.getPlatoonKitStore(company, platoon);
		 			if(vilCap != null && kitStore != null)
		 				vilCap.setKitStore(kitStore);
		 			
		 			cap.addVillager(entity.getUniqueID(), company, platoon);
		 			return new VilEnlistPacket(entity.getEntityId(), company, platoon, true);
		 		}
		 		else
		 		{
		 			vilCap.setFoodStore(null).setKitStore(null);
		 			cap.removeVillager(entity.getUniqueID());
		 			return new VilEnlistPacket(entity.getEntityId(), 0, 0, false);
		 		}
		 	}
		}
		return null;
	}

	public static class ServerHandler implements IMessageHandler<VilEnlistPacket, IMessage> {

	@Override
	public IMessage onMessage(VilEnlistPacket message, MessageContext ctx)
	{
		ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
		{
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			World server = ImprovedVils.proxy.getWorld(ctx);
			ItemStack stack = InventoryUtil.getOnly1StackByItem(serverPlayer.inventory, CommonProxy.ItemHolder.BATON);
			if(stack == null)
			{
				NetWrapper.NETWORK.sendTo(new VilStateUpdate(), serverPlayer);
				return;
			}
			
			Entity entity = server.getEntityByID(message.entityID);
			IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
	
			if(entity instanceof EntityVillager && VillagerPlayerDealMethods.getPlayerFealty(serverPlayer, (EntityVillager) entity))
			{
				NetWrapper.NETWORK.sendTo(message.updateEnlistInfoForStack(serverPlayer, message.isEnlisted, message.company, message.platoon, entity, vilCap),
						serverPlayer);
			}
			else
			{
				NetWrapper.NETWORK.sendTo(new VilStateUpdate(), serverPlayer);
			}
		});
		return null;
	}	
}

	public static class ClientHandler implements IMessageHandler<VilEnlistPacket, IMessage> {

		@Override
		public IMessage onMessage(VilEnlistPacket message, MessageContext ctx)
		{
			ClientProxy.updateVillagerEnlistGUIInfo(message.isEnlisted, message.company, message.platoon);
			return null;
		}
	}
}
