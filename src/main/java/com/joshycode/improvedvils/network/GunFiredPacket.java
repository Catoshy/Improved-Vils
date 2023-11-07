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
	
	public GunFiredPacket() {}
	
	public GunFiredPacket(int id) {
		super();
		this.id = id;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(this.id);
	}
	
	public static class Handler implements IMessageHandler<GunFiredPacket, IMessage> {

		@Override
		public IMessage onMessage(GunFiredPacket message, MessageContext ctx) 
		{
			ImprovedVils.proxy.getListener(ctx).addScheduledTask(() ->
			{
				Entity entity = ImprovedVils.proxy.getWorld(ctx).getEntityByID(message.id);
				if(entity == null) return;
				
				Random rand = Minecraft.getMinecraft().world.rand;
				for(int i = 0; i < rand.nextInt(48) + 32; i++) 
				{
					double rotateX = -MathHelper.sin((float) Math.toRadians(entity.getRotationYawHead()))
				                * MathHelper.cos((float) Math.toRadians(entity.rotationPitch));
				    double rotateZ = MathHelper.cos((float) Math.toRadians(entity.getRotationYawHead()))
				                * MathHelper.cos((float) Math.toRadians(entity.rotationPitch));
				    double rotateY = -MathHelper.sin((float) Math.toRadians(entity.rotationPitch));
					double x = entity.posX + rotateX;
					double z = entity.posZ + rotateZ;
					double y = entity.posY + entity.getEyeHeight();
					
					rotateX *= (rand.nextGaussian() + 2) * .25;
					rotateY *= (rand.nextGaussian() + 2) * .25;;
					rotateZ *= (rand.nextGaussian() + 2) * .25;;
					
					rotateX += rand.nextGaussian() * 0.007499999832361937D * 8D;
					rotateZ += rand.nextGaussian() * 0.007499999832361937D * 8D + .1D;
					rotateY += rand.nextGaussian() * 0.007499999832361937D * 8D;
					EnumParticleTypes type = EnumParticleTypes.EXPLOSION_NORMAL;
					if(rand.nextFloat() < .15F)
						type = EnumParticleTypes.FIREWORKS_SPARK;
					entity.getEntityWorld().spawnParticle(type, x, y, z, rotateX, rotateY, rotateZ, 0);
				}
			});
			return null;
		}
		
	}
}
