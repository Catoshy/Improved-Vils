package com.joshycode.improvedvils.network;

import java.util.Set;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.item.ItemMarshalsBaton;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilKitStorePacket extends BlockPosPacket implements IMessage {
	
	int provisioningUnit;
	
	public VilKitStorePacket() 
	{ 
		super();
		this.provisioningUnit = -1;
	}
	
	public VilKitStorePacket(BlockPos pos, int provisioningUnit) 
	{
		super(pos);
		this.provisioningUnit = provisioningUnit;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		super.fromBytes(buf);
		this.provisioningUnit = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		super.toBytes(buf);
		buf.writeInt(this.provisioningUnit);
	}
	
	public static class Handler implements IMessageHandler<VilKitStorePacket, IMessage>
	{

		@Override
		public IMessage onMessage(VilKitStorePacket message, MessageContext ctx) {
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				IMarshalsBatonCapability cap = player.getHeldItemMainhand().getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				if(world.getBlockState(message.pos).getBlock().hasTileEntity(world.getBlockState(message.pos)))
				{
					if(cap != null &&  world.getTileEntity(message.pos) != null)
					{
						if(ConfigHandler.debug)
							Log.info("Kit Store for unit ... %s", message.provisioningUnit);
						int prevSelectedUnit = cap.selectedUnit();
						cap.setPlatoon(message.provisioningUnit / 10, message.provisioningUnit % 10);
						cap.setPlatoonKitStore(message.pos);
						Set<Entity> villagers = ItemMarshalsBaton.getEntitiesByUUID(cap.getVillagersSelected(), world);
						for(Entity e : villagers)
						{
							e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setKitStore(message.pos);
						}
						cap.setPlatoon(prevSelectedUnit / 10, prevSelectedUnit % 10);
					}
				}
			});
			return null;
		}
	}
	
}
