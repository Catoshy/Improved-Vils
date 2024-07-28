package com.joshycode.improvedvils.network;

import java.util.Random;

import com.joshycode.improvedvils.ImprovedVils;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GunFiredPacket implements IMessage {

	int id;
	double[] data;
	
	public GunFiredPacket() {}
	
	public GunFiredPacket(int id, double[] data) {
		super();
		this.id = id;
		this.data = data;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.id = buf.readInt();
		this.data = new double[5];
		for(int i = 0; i < 5; i++)
		{
			this.data[i] = buf.readDouble();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.id);
		for(int i = 0; i < 5; i++)
		{
			buf.writeDouble(data[i]);
		}
	}
	
	public static class Handler implements IMessageHandler<GunFiredPacket, IMessage> {

		@Override
		public IMessage onMessage(GunFiredPacket message, MessageContext ctx) 
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				Entity entity = ImprovedVils.proxy.getWorld(ctx).getEntityByID(message.id);
				if(entity == null) return;
				
				double rotationYaw = message.data[3];
				double rotationPitch = message.data[4];
				double rotateX = -MathHelper.sin((float) Math.toRadians(rotationYaw))
		                * MathHelper.cos((float) Math.toRadians(rotationPitch));
			    double rotateZ = MathHelper.cos((float) Math.toRadians(rotationYaw))
			                * MathHelper.cos((float) Math.toRadians(rotationPitch));
			    double rotateY = -MathHelper.sin((float) Math.toRadians(rotationPitch));
			    
			    double x = message.data[0] + rotateX;
				double y = message.data[1] + entity.getEyeHeight();
				double z = message.data[2] + rotateZ;	
				
				Random rand = Minecraft.getMinecraft().world.rand;
				for(int i = 0; i < rand.nextInt(48) + 32; i++) 
				{
					double n_rotateX = rotateX * (rand.nextGaussian() + 2) * .25;
					double n_rotateY = rotateY * (rand.nextGaussian() + 2) * .25;;
					double n_rotateZ = rotateZ * (rand.nextGaussian() + 2) * .25;;
					
					n_rotateX += rand.nextGaussian() * 0.007499999832361937D * 8D;
					n_rotateZ += rand.nextGaussian() * 0.007499999832361937D * 8D + .1D;
					n_rotateY += rand.nextGaussian() * 0.007499999832361937D * 8D;
					EnumParticleTypes type = EnumParticleTypes.EXPLOSION_NORMAL;
					if(rand.nextFloat() < .15F)
						type = EnumParticleTypes.FIREWORKS_SPARK;
					entity.getEntityWorld().spawnParticle(type, x, y, z, n_rotateX, n_rotateY, n_rotateZ, 0);
				}
			});
			return null;
		}
		
	}
}
