package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ClientProxy;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class VilStateUpdate implements IMessage{

	int guardStateVal, followStateVal, dutyStateVal, enlistC, enlistP;
	boolean hungry;
	Vec3i vec;

	public VilStateUpdate() { guardStateVal = 0; followStateVal = 0; enlistC = 0; enlistP = 0; hungry = false; vec = Vec3i.NULL_VECTOR; }

	public VilStateUpdate(int int1, int int2, int int3, boolean hungry, int enlistC, int enlistP, Vec3i vec)
	{
		this.guardStateVal = int1;
		this.followStateVal = int2;
		this.dutyStateVal = int3;
		this.hungry = hungry;
		this.enlistC = enlistC;
		this.enlistP = enlistP;
		this.vec = vec;
	}

	public VilStateUpdate(int int1, int int2, int int3, boolean hungry, int enlistC, int enlistP)
	{
		this(int1, int2, int3, hungry, enlistC, enlistP, Vec3i.NULL_VECTOR);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.guardStateVal);
		buf.writeInt(this.followStateVal);
		buf.writeInt(this.dutyStateVal);
		buf.writeBoolean(this.hungry);
		buf.writeInt(enlistC);
		buf.writeInt(enlistP);
		buf.writeInt(vec.getX());
		buf.writeInt(vec.getY());
		buf.writeInt(vec.getZ());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.guardStateVal = buf.readInt();
		this.followStateVal = buf.readInt();
		this.dutyStateVal = buf.readInt();
		this.hungry = buf.readBoolean();
		this.enlistC = buf.readInt();
		this.enlistP = buf.readInt();
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		this.vec = new Vec3i(x, y, z);
	}

	public static class ClientHandler implements IMessageHandler<VilStateUpdate, IMessage> {

		@Override
		public IMessage onMessage(VilStateUpdate message, MessageContext ctx)
		{
			 ClientProxy.updateVillagerGuardGUIInfo(message.vec, message.guardStateVal, message.followStateVal, message.dutyStateVal, message.hungry, message.enlistC, message.enlistP);
			 return null;
		}
	}
}