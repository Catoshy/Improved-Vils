package com.joshycode.improvedvils.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class BlockPosPacket implements IMessage {

	BlockPos pos;

	public BlockPosPacket()
	{
		this(BlockPos.ORIGIN);
	}

	public BlockPosPacket(Vec3i pos)
	{
		this.pos = new BlockPos(pos);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int x, y, z;
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

}
