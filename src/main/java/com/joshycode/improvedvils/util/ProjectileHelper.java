package com.joshycode.improvedvils.util;

import java.util.List;

import javax.annotation.Nullable;

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

	@Nullable
	public static Pair<RayTraceResult, String> checkForFirendlyFire(EntityLivingBase entityHost, World world, float inaccuracy)
	{
		String debugString = "\nDEBUG FOR FRIENDLY FIRE! \n";
		Entity entity = checkEntitiesNearby(entityHost, world);
	    
		double y = entityHost.posY + entityHost.getEyeHeight();
		double x = entityHost.posX;
		double z = entityHost.posZ;
	    double motionX = -MathHelper.sin(entityHost.getRotationYawHead() / 180.0F * (float) Math.PI)
	            * MathHelper.cos(entityHost.rotationPitch / 180.0F * (float) Math.PI);
	    double motionZ = MathHelper.cos(entityHost.getRotationYawHead() / 180.0F * (float) Math.PI)
	            * MathHelper.cos(entityHost.rotationPitch / 180.0F * (float) Math.PI);
	    double motionY = -MathHelper.sin(entityHost.rotationPitch / 180.0F * (float) Math.PI);

	    int range = ConfigHandler.friendlyFireSearchRange;
	    Vec3d vec1 = new Vec3d(x, y, z);
	    Vec3d vec2 = new Vec3d(x + (motionX * range), y + (motionY * range), z + (motionZ * range));
	    RayTraceResult raytraceresult = world.rayTraceBlocks(vec1, vec2, false, true, false);
	    double rangeDistance = vec2.distanceTo(vec1);
	    double sizeFactor = inaccuracy * rangeDistance * .0325D;
	    AxisAlignedBB axisalignedbb = getSearchAABB(motionX, motionZ, vec1, vec2, sizeFactor);
	    
	    double d6 = 0.0D;
	    if(raytraceresult != null)
	    {
	    	d6 = vec1.squareDistanceTo(raytraceresult.hitVec);
	    }
	    
	    List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityHost, axisalignedbb);
	    
	    //TODO DEBUG
	    debugString += "    Entity Host: " + entityHost + "\n" +
	    		"    x:" + x + "\n" +
	    		"    y:" + y + "\n" +
	    		"    z:" + z + "\n" +
	    		"    lookX:" + motionX + "\n" +
	    		"    lookY:" + motionY + "\n" +
	    		"    lookZ:" + motionZ + "\n" +
	    		"    vec1:" + vec1 + "\n" +
	    		"    vec2:" + vec2 + "\n" +
	    		"    sizeFactor:" + sizeFactor + "\n" +
	    		"    AABB for search Area:" + axisalignedbb + "\n" +
	    		"    list in AABB:" + list + "\n" +
	    		"    List data...." + "\n";
    		
        for(Entity entity1 : list)
        {	
        	//TODO DEBUG
        	debugString += "        Entity:" + entity1.toString() + "\n";
            if(entity1.canBeCollidedWith() && !entity1.noClip)
            {
            	double distance = entity1.getDistance(entityHost);
            	double grow = Math.max(sizeFactor * (distance / rangeDistance), 0.4D);
                AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().grow(grow);
                RayTraceResult raytraceresult1 = axisalignedbb1.calculateIntercept(vec1, vec2);
            	
                //TODO DEBUG
                debugString += "            can be collided & !noClip." + "\n" +
                "            AABB used for calculate intercept:" + axisalignedbb1 + "\n" +
                "            grow factor was:" + sizeFactor * (distance / rangeDistance) + "\n" +
                "            grow var was:" + grow + "\n" +
                "            RaytraceResult:" + raytraceresult1 + "\n";
                
                if(raytraceresult1 != null)
                {
                    double d7 = vec1.squareDistanceTo(raytraceresult1.hitVec);
                    if(d7 < d6 || d6 == 0.0D)
                    {
                    	//TODO DEBUG
                    	debugString += "            If this is last entry for list entity above, then entity was made the result. \n";
                        entity = entity1;
                        d6 = d7;
                    }
                }
            }
        }
	    if(entity != null)
	    {
	        raytraceresult = new RayTraceResult(entity);
	    }
	    
	    //return raytraceresult;
    	//TODO DEBUG
	    return new Pair<>(raytraceresult, debugString);
	}

	private static AxisAlignedBB getSearchAABB(double motionX, double motionZ, Vec3d vec1, Vec3d vec2, double sizeFactor) 
	{
		double mX = Math.min(vec1.x, vec2.x);
		double mY = Math.min(vec1.y, vec2.y);
		double mZ = Math.min(vec1.z, vec2.z);
		double MX = Math.max(vec1.x, vec2.x);
		double MY = Math.max(vec1.y, vec2.y);
		double MZ = Math.max(vec1.z, vec2.z);
		
		double lookZ = Math.abs(motionZ);
		double lookX = Math.abs(motionX);
		
		return new AxisAlignedBB(mX + (-lookZ * sizeFactor), mY - 2, mZ + (-lookX * sizeFactor),
	    		MX + (lookZ * sizeFactor), MY + 2, MZ + (lookX * sizeFactor));
	}

	private static Entity checkEntitiesNearby(EntityLivingBase entityHost, World world) 
	{
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityHost, entityHost.getEntityBoundingBox().grow(.667D));
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
