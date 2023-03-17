package com.joshycode.improvedmobs.network;

import java.util.UUID;

import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.handler.VillagerCapabilityHandler;
import com.joshycode.improvedmobs.item.ItemMarshalsBaton;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilEnlistPacket implements IMessage {
	
	private boolean isEnlisted;
	private int company;
	private int platoon;
	private int entityID;
	
	public VilEnlistPacket() {
		this.entityID = 0;
		this.company = 0;
		this.platoon = 0;
		this.isEnlisted = false;
	}

	public VilEnlistPacket(int uuid, int company, int platoon, boolean isEnlisted) {
		this.entityID = uuid;
		this.company = company;
		this.platoon = platoon;
		this.isEnlisted = isEnlisted;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityID);
		buf.writeInt(this.platoon);
		buf.writeInt(this.company);
		buf.writeBoolean(this.isEnlisted);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityID = buf.readInt();
		this.company = buf.readInt();
		this.platoon = buf.readInt();
		this.isEnlisted = buf.readBoolean();
	}
	
	public static class Handler implements IMessageHandler<VilEnlistPacket, IMessage> {

	  @Override public IMessage onMessage(VilEnlistPacket message, MessageContext ctx) {
	    EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
	    WorldServer server = ctx.getServerHandler().player.getServerWorld();
	    boolean flag = false;
	    ItemStack stack = null;
	    for(int i = 0; i < serverPlayer.inventory.getSizeInventory(); i++) {
	    	ItemStack stack2 = serverPlayer.inventory.getStackInSlot(i);
	    	if(stack2.getItem() instanceof ItemMarshalsBaton) {
	    		if(flag) {
	    			break;
	    		} else {
	    			stack = stack2;
	    		}	
	    	}
	    }
	    if(!flag) {
	    	IMarshalsBatonCapability cap = stack.getCapability(VillagerCapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
	    	if(cap != null) {
		    	if(message.isEnlisted) {	
		    		cap.addVillager(server.getEntityByID(message.entityID).getUniqueID(), message.company, message.platoon);
		    	} else {
		    		cap.removeVillager(server.getEntityByID(message.entityID).getUniqueID());
		    	}
	    	}
	    }
	    return null;
	  }
	}
}
