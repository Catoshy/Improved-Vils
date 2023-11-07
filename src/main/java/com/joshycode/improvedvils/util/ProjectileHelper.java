package com.joshycode.improvedvils.util;

import java.util.List;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.handler.ConfigHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ProjectileHelper {

	public static RayTraceResult checkForFirendlyFire(EntityLivingBase entityHost, World world)
	{
		double startY = entityHost.posY + entityHost.getEyeHeight();
		double startX = entityHost.posX;
		double startZ = entityHost.posZ;
	    double motionX = -MathHelper.sin(entityHost.getRotationYawHead() / 180.0F * (float) Math.PI)
	            * MathHelper.cos(entityHost.rotationPitch / 180.0F * (float) Math.PI);
	    double motionZ = MathHelper.cos(entityHost.getRotationYawHead() / 180.0F * (float) Math.PI)
	            * MathHelper.cos(entityHost.rotationPitch / 180.0F * (float) Math.PI);
	    double motionY = -MathHelper.sin(entityHost.rotationPitch / 180.0F * (float) Math.PI);
	    AxisAlignedBB axisalignedbb = new AxisAlignedBB(startX - motionX, startY - motionY, startZ - motionZ, startX + motionX, startY + motionY, startZ + motionZ).grow(1.75F);
	    Vec3d vec1 = new Vec3d(startX, startY, startZ);
	    Vec3d vec2 = new Vec3d(startX + (motionX * 8), startY + (motionY * 8), startZ + (motionZ * 8));
	    RayTraceResult raytraceresult = world.rayTraceBlocks(vec1, vec2, false, true, false);
	    
	    Entity entity = checkEntitiesNearby(entityHost, world);
	    if(entity != null)
	    {
	    	return new RayTraceResult(entity);
	    }
	    double d6 = 0.0D;
	    for(int i = 0; i < 16; i++)
	    {
	    	List<Entity> list;

    		list = world.getEntitiesWithinAABBExcludingEntity(entityHost, axisalignedbb);
    		axisalignedbb = axisalignedbb.offset(motionX, motionY, motionZ).grow(.5D);
	
	        for (int ii = 0; ii < list.size(); ++ii)
	        {
	            Entity entity1 = list.get(ii);
	
	            if (entity1.canBeCollidedWith() && !entity1.isEntityEqual(entityHost) && !entity1.noClip)
	            {
	                AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
	                RayTraceResult raytraceresult1 = axisalignedbb1.calculateIntercept(vec1, vec2);
	
	                if (raytraceresult1 != null)
	                {
	                    double d7 = vec1.squareDistanceTo(raytraceresult1.hitVec);
	
	                    if (d7 < d6 || d6 == 0.0D)
	                    {
	                        entity = entity1;
	                        d6 = d7;
	                    }
	                }
	            }
	        }
	    }
	    if (entity != null)
	    {
	        raytraceresult = new RayTraceResult(entity);
	    }
	    
	    if(ConfigHandler.debug)
	    	Log.info("Raytrace result: %s", raytraceresult);
	    
	    return raytraceresult;
	}

	private static Entity checkEntitiesNearby(EntityLivingBase entityHost, World world) 
	{
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityHost, entityHost.getEntityBoundingBox().grow(.5D));
        Entity entity = null;
        double d0 = 0D;
		for (Entity entity1 : list)
        {
            double d1 = entityHost.getDistanceSq(entity1);

            if (d1 < d0 || d0 == 0.0D)
            {
                entity = entity1;
                d0 = d1;
            }
        }
		return entity;
    }

}
