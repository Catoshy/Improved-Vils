package com.joshycode.improvedvils.entity;

import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/*
 *  Thanks to ReforgedMod & Co. for some of the methods used!
 */
public class EntityBullet extends EntityThrowable {

	public EntityBullet(World worldIn) 
	{
		super(worldIn);
		this.damageName = "Villager's Bullet";
		this.debugInfo = "";
		this.lowCoef = 0;
		this.highCoef = 0;
		this.n_val = 0;
		this.supersonicStartVal = 0;
	}

	//All these vals and related equations are derived from a ballistics study and are general in scope
	//A Direct-Fire Trajectory Model for Supersonic, Transonic, and Subsonic Projectile Flight
	//by Paul Weinach
	public static final double SUB_SONIC_BOUND = .85d; //Mach
	public static final double TRANS_SONIC_BOUND = 1.1d; //Mach
	public static final double AIR_DENSITY = 1.225d; //kg/m^3
	public static final double GRAMS_TO_POUNDS = 453.592f;
	
	private final String damageName;
	
	public final double lowCoef;
	public final double highCoef;
	public final double n_val;
	public final double supersonicStartVal;
	private double mass;
	private double halfWidthInMeter;
	private double speed;
	
	private final String debugInfo;

	public EntityBullet(World worldIn, EntityLivingBase throwerIn, RangeAttackEntry entry, float accuracyModifier, String damageName, String debugInfo) 
	{
		super(worldIn, throwerIn);
        this.setSize(.1F, .1F);
		this.mass = entry.ballisticData.mass; //mass is in grams
		this.halfWidthInMeter = EntityBullet.getSphereRadius(entry.ballisticData.mass, 11.35d) / 100; //result of func yields centimeters
		this.lowCoef = entry.ballisticData.low_coefficient;
		this.highCoef = entry.ballisticData.high_coefficient;
		this.n_val = -(Math.log(this.lowCoef/this.highCoef))/Math.log(SUB_SONIC_BOUND/TRANS_SONIC_BOUND);
		this.supersonicStartVal = this.highCoef * 1/Math.pow(TRANS_SONIC_BOUND, this.highCoef);
        this.ignoreEntity = throwerIn;
        this.damageName = damageName;
        //TODO
        this.debugInfo = debugInfo;
        //TODO
        setLocationAndAngles(throwerIn.posX, throwerIn.posY + throwerIn.getEyeHeight(), throwerIn.posZ, throwerIn.rotationYawHead,
        		throwerIn.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.1;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.1;
        setPosition(posX, posY, posZ);
        this. motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionY = -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI);
        shoot(this.motionX, this.motionY, this.motionZ, entry.ballisticData.velocity, entry.ballisticData.inaccuracy * accuracyModifier);
	}

	@Override
	public void onUpdate()
	{
		this.speed = Math.sqrt((this.motionX*this.motionX)+(this.motionY*this.motionY)+(this.motionZ*this.motionZ));
		
		super.onUpdate();
		double speedInMps = this.speed * 20d;
		double drag_coef = getDragCoefficient();
		double forceOfDragInNewt = .5d * AIR_DENSITY * (Math.PI * (this.halfWidthInMeter * this.halfWidthInMeter)) * drag_coef * (speedInMps * speedInMps);
		double slowdown = (forceOfDragInNewt / (mass / 1000d)) / 200D;	//product of first equation in meters/second^s, divide by 20 to get meters/second/tick, 
		applyVectorChange(slowdown);									//then 20 again to get MC velocity, then multiply by 2 to increase drag for MC, or just 200
		if(!this.world.isRemote && this.inGround && this.speed == 0)
			this.setDead();
	}

	private void applyVectorChange(double slowdown) 
	{
		double new_speed = this.speed - slowdown;
		if(new_speed < 0)
			new_speed = 0;
		boolean flagx = this.motionX >= 0;
		boolean flagz = this.motionZ >= 0;
		double xzhypotenus = Math.sqrt(Math.abs(this.motionX * this.motionX) + Math.abs(this.motionZ * this.motionZ));
		double xzPlaneAngle = Math.abs(Math.asin(this.motionZ/xzhypotenus));
		double xyzPlaneAngle = Math.asin(this.motionY/this.speed);
		double new_hypotenus = Math.cos(xyzPlaneAngle) * new_speed;
		if(flagx)
			this.motionX = Math.cos(xzPlaneAngle) * new_hypotenus;
		else
			this.motionX = Math.cos(xzPlaneAngle) * new_hypotenus * -1;
		if(flagz)
			this.motionZ = Math.sin(xzPlaneAngle) * new_hypotenus;
		else
			this.motionZ = Math.sin(xzPlaneAngle) * new_hypotenus * -1;
		this.motionY = Math.sin(xyzPlaneAngle) * new_speed;
		
	}

	public double getDragCoefficient()
	{
		double mach = this.speed / 17.15d;
		if(mach < SUB_SONIC_BOUND)
		{
			return this.lowCoef;
		}
		else if(mach < TRANS_SONIC_BOUND)
		{
			return this.supersonicStartVal * Math.pow((TRANS_SONIC_BOUND/mach), this.n_val);
		}
		else
		{
			return this.highCoef * (1/Math.pow(mach, this.highCoef));
		}
	}
	
	protected float getImpactDamage()
	{
		double speedConversion = this.speed * 65.6168;
		float bulletEnergy = (float) (.5 * (this.mass / GRAMS_TO_POUNDS) * (speedConversion * speedConversion)) / 32.174f; //in foot pounds
		float damage = MathHelper.sqrt(bulletEnergy / 6f);
		return damage;
	}
	
	/**
     * Causes the damage, which is set in the constructor
     *
     * @param target  the entity which got hit
     * @param shooter the mob which shot
     * @return the specified DamageSource
     */
    protected DamageSource causeImpactDamage(Entity target, EntityLivingBase shooter) 
    {
        return new EntityDamageSourceIndirect(damageName, target, shooter).setProjectile().setDamageBypassesArmor();
    }
	
	public static double getSphereRadius(double mass, double density)
	{
		return Math.pow(((3 * mass) / (4 * density * Math.PI)), 1d/3d);
	}
	
	 /**
     * Called when the entity hits a block
     *
     * @param blockPos The position where the entity landed
     * @return should the entity get setDead() ?
     */
    protected boolean onBlockHit(BlockPos blockPos) 
    {
        return true;
    }

    /**
     * Called when the entity hits an other entity
     *
     * @param entity The entity which got hit
     * @return should the entity get setDead() ?
     */
    protected boolean onEntityHit(Entity entity) 
    {
    	if(entity instanceof EntityVillager)
    		Log.info(this.debugInfo + "\n    Shot Entity:" + entity);
        entity.attackEntityFrom(causeImpactDamage(entity, getThrower()), getImpactDamage());
        return true;
    }

    /**
     * Called when the entity hits a living entity
     *
     * @param living The mob which got hit
     * @return should the entity get setDead() ?
     */
    protected boolean onEntityHit(EntityLivingBase living) 
    {
        return onEntityHit((Entity) living);
    }

    @Override
    protected void onImpact(RayTraceResult target) {
        if (!world.isRemote) {
            boolean broken;
            if (target.entityHit == null) {
                broken = world.getBlockState(target.getBlockPos()).getCollisionBoundingBox(world,
                        target.getBlockPos()) != null && onBlockHit(target.getBlockPos());
            } else {
                if (target.entityHit instanceof EntityLivingBase && target.entityHit.equals(getThrower())
                        && ticksExisted < 5) {
                    return;
                }
                broken = onEntityHit(target.entityHit);
            }
            if (broken)
                setDead();
        }
    }
    /*
    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender()
    {
    	int light = getAmbientLight();
            
        if(light <= 7340032)
        {
        	light = 7340032 - light + 7340032;
        }
        return light;
    }
    
    public int getAmbientLight()
    {
    	BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
    	int light = 0;
    	if (this.world.isBlockLoaded(blockpos$mutableblockpos))
        {
            blockpos$mutableblockpos.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
            light = this.world.getCombinedLight(blockpos$mutableblockpos, 0);
        }
    	return light;
    }*/
}
