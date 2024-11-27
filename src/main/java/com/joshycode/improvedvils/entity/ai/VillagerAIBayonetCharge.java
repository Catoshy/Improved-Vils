package com.joshycode.improvedvils.entity.ai;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jline.utils.Log;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.LookHelper;
import com.joshycode.improvedvils.util.VilAttributes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class VillagerAIBayonetCharge extends EntityAIGoFar {

	protected final Set<Predicate<? super EntityLivingBase>> filters;
	private TroopCommands order;
	private EntityLivingBase attackTarget;
	Vec3d seekingVec;
	private int chargeCounter;
	private int attackTick;
	private int earlierPathFindFails;
//	private float rotation;
//	private float prevRotation;
	private boolean failedCharge;
	private boolean goodCharge;
    private static final UUID CHARGE_BOOST_I_UUID = UUID.fromString("57a7e16b-78fe-4679-a235-dd8feb31edb2");
    private static final AttributeModifier CHARGE_BOOST_I = (new AttributeModifier(CHARGE_BOOST_I_UUID, "Bayonet charge boost 1", 2.5D, 0)).setSaved(true);
    private static final UUID CHARGE_BOOST_II_UUID = UUID.fromString("40ae74a1-cf47-4ab5-bde3-767ea79cfe47");
    private static final AttributeModifier CHARGE_BOOST_II = (new AttributeModifier(CHARGE_BOOST_II_UUID, "Bayonet charge boost 2", 2.5D, 0)).setSaved(true);
    private static final UUID CHARGE_BOOST_III_UUID = UUID.fromString("29126cc9-8d73-498f-a4e3-931670fb9a23");
    private static final AttributeModifier CHARGE_BOOST_III = (new AttributeModifier(CHARGE_BOOST_III_UUID, "Bayonet charge boost 3", 3.0D, 0)).setSaved(true);
    //private static final UUID FAILED_CHARGE_LAG_UUID = UUID.fromString("9afcbf19-744f-49f8-98df-58f458bdf272");
    //private static final AttributeModifier FAILED_CHARGE_LAG = (new AttributeModifier(FAILED_CHARGE_LAG_UUID, "Bayonet charge fail-debuff", -1.0D, 0)).setSaved(true);
    
	public VillagerAIBayonetCharge(EntityVillager villager, Set<Predicate<? super EntityLivingBase>> filters, int imHereDistanceSq, int mostFails) 
	{
		super(villager, imHereDistanceSq, mostFails);
		this.filters = filters;
		this.seekingVec = null;
		this.chargeCounter = 0;
		this.attackTick = 0;
		this.failedCharge = false;
		this.goodCharge = false;
		this.earlierPathFindFails = 0;
//		this.rotation = 0F;
//		this.prevRotation = 0F;
	}
	
	@Override
	public boolean shouldExecute()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		this.order = VilMethods.getTroopFaring(villager) ;
		if(!isRightCommands(villager) || (VilMethods.getGuardBlockPos(villager) != null) || villager.isMating() || VilMethods.getFollowing(villager))
			return false;
		if(!VilMethods.getHungry(villager) && VilMethods.getDuty(villager)) 
		{
			ItemStack stack = InventoryUtil.findAndDecrItem(((EntityVillager) entityHost).getVillagerInventory(), ItemFood.class);
			if(stack == null)
				return false;
			IAttributeInstance attribute = this.entityHost.getAttributeMap().getAttributeInstance(VilAttributes.VIL_DAMAGE);
			attribute.removeModifier(CHARGE_BOOST_I);
			attribute.applyModifier(CHARGE_BOOST_I);
			attribute.removeModifier(CHARGE_BOOST_II);
			attribute.applyModifier(CHARGE_BOOST_II);
			attribute.removeModifier(CHARGE_BOOST_III);
			attribute.applyModifier(CHARGE_BOOST_III);

			this.entityHost.setSprinting(true);
//			this.prevRotation = this.entityHost.rotationYaw;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldContinueExecuting()
	{
		int revengeTime = this.entityHost.ticksExisted - this.entityHost.getRevengeTimer();
		if(this.order != VilMethods.getTroopFaring((EntityVillager) this.entityHost) || VilMethods.getGuardBlockPos((EntityVillager) this.entityHost) != null || VilMethods.getFollowing((EntityVillager) this.entityHost))
			return false;
		if((this.goodCharge && this.chargeCounter > 140) || (this.entityHost.getNavigator().noPath() && this.finished))
			return false;
		if(this.failedCharge && revengeTime < 20 && revengeTime >= 0)
			return false;
		if(this.failedCharge && this.goodCharge) //The charge failed, but then found a hit anyways.
			return false;
		return super.shouldContinueExecuting();
	}

	private boolean isRightCommands(EntityVillager villager) 
	{
		return VilMethods.getCommBlockPos(villager) != null && this.order == TroopCommands.CHARGE;
	}
	
	@Override
	public void updateTask()
	{
		if(!this.failedCharge && !this.goodCharge)
		{
			if(this.checkHurdle())
			{
				this.chargeCounter = 0;
				IAttributeInstance attribute = this.entityHost.getAttributeMap().getAttributeInstance(VilAttributes.VIL_DAMAGE);
				if(attribute.hasModifier(CHARGE_BOOST_III))
				{
					Log.info("taking down from charge3");
					attribute.removeModifier(CHARGE_BOOST_III);
				}
				else if(attribute.hasModifier(CHARGE_BOOST_II))
				{
					Log.info("taking down from charge2");
					attribute.removeModifier(CHARGE_BOOST_II);
				}
				else if(attribute.hasModifier(CHARGE_BOOST_I))
				{
					Log.info("taking down from charge1");
					attribute.removeModifier(CHARGE_BOOST_I);
				}
				else
				{
					this.failedCharge = true;
					super.updateTask();
					return;
				}
			}
			RayTraceResult result = LookHelper.checkClosestLineOfSight(this.entityHost, this.entityHost.getEntityWorld(), 16, 16);
			if(result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && checkTarget(result.entityHit))
			{
				this.attackTarget = (EntityLivingBase) result.entityHit;
			}
			if(this.attackTarget != null)
			{
				seekAndAttack(true);
			}
		}
		else if(this.goodCharge)
		{
			if(this.attackTarget != null && this.attackTarget.isEntityAlive() && this.attackTarget.getDistanceSq(this.entityHost) < 256.0D)
			{
				seekAndAttack(false);
			}
			else
			{
				List<Entity> nearbyEntities = this.entityHost.getEntityWorld().getEntitiesInAABBexcluding(this.entityHost, this.entityHost.getEntityBoundingBox().grow(8.0F), null);
				double d0 = 0.0D;
				Entity chosenEntity = null;
				for(Entity entity : nearbyEntities)
				{
					if(checkTarget(entity) && (entity.getDistanceSq(this.entityHost) < d0 || d0 == 0.0D))
					{
						chosenEntity = entity;
					}
				}
				this.attackTarget = (EntityLivingBase) chosenEntity;
				if(this.attackTarget == null)
				{
					this.seekingVec = RandomPositionGenerator.findRandomTarget(this.entityHost, 10, 7);
					if(this.seekingVec != null)
						this.entityHost.getNavigator().tryMoveToXYZ(this.seekingVec.x, this.seekingVec.y, this.seekingVec.z, this.hostSpeed());
				}
			}
		}
		else if(this.failedCharge)
		{
			RayTraceResult result = LookHelper.checkClosestLineOfSight(this.entityHost, this.entityHost.getEntityWorld(), 16, 16);
			if(result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && checkTarget(result.entityHit))
			{
				this.attackTarget = (EntityLivingBase) result.entityHit;
			}
			if(this.attackTarget != null)
			{
				seekAndAttack(false);
			}
		}
		this.attackTick = Math.max(this.attackTick - 1, 0);
		this.chargeCounter++;
		super.updateTask();
	}

	private void seekAndAttack(boolean unBlocksome) 
	{
		double chargeSpeed = this.hostSpeed();
		this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, chargeSpeed + .1D);
		double d0 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
		if(VillagerAIAttack.checkAndPerformAttack((EntityVillager) this.entityHost, this.attackTarget, this.attackTick, d0, unBlocksome))
		{
			this.goodCharge = true;
			this.attackTick = (int) (((float)VillagerAIAttack.getCooldown((EntityVillager) this.entityHost)) * .7F);
			ridModifiers();
		}
	}

	private boolean checkTarget(Entity entity)
	{
		if(!(entity instanceof EntityLivingBase))
			return false;
		for(Class<? extends EntityLivingBase> clazz : CommonProxy.TARGETS)
		{
			if(clazz.isAssignableFrom(entity.getClass())) 
			{
				return true;
			}
		}
		for(Predicate<? super EntityLivingBase> filter : this.filters)
		{
			if(filter != null && filter.apply((EntityLivingBase) entity))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean checkHurdle() 
	{
//		this.prevRotation = this.rotation;
//		this.rotation = this.entityHost.rotationYaw;
		int earlierFails = this.earlierPathFindFails;
		this.earlierPathFindFails = this.pathfindingFails;
		return this.idleTicks > 40 || /*MathHelper.wrapDegrees(this.rotation - this.prevRotation) > 50F ||*/ this.pathfindingFails > earlierFails || (this.chargeCounter > 100 && this.entityHost.getRNG().nextInt(30) == 0);
	}
	
	@Override
	public void arrivedAtObjective() 
	{
		if(!this.goodCharge)
		{
			this.resetTask();
		}
	}
	
	@Override
	public BlockPos getObjectiveBlock()
	{
		return VilMethods.getCommBlockPos((EntityVillager) this.entityHost);
	}

	@Override
	protected double hostSpeed() 
	{ 
		return this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
	}
	
	@Override
	protected void resetObjective() 
	{
		VilMethods.setCommBlockPos((EntityVillager) this.entityHost, null);
	}
	
	private void ridModifiers() 
	{
		IAttributeInstance attribute = this.entityHost.getAttributeMap().getAttributeInstance(VilAttributes.VIL_DAMAGE);
		attribute.removeModifier(CHARGE_BOOST_I);
		attribute.removeModifier(CHARGE_BOOST_II);
		attribute.removeModifier(CHARGE_BOOST_III);
		//attribute.removeModifier(FAILED_CHARGE_LAG);
	}	

	@Override
	public void resetTask()
	{
		ridModifiers();
		VilMethods.setTroopFaring((EntityVillager) this.entityHost, TroopCommands.NONE);
		VilMethods.setCommBlockPos((EntityVillager) this.entityHost, null);
		this.entityHost.setSprinting(false);
		this.order = null;
		this.attackTarget = null;
		this.seekingVec = null;
		this.attackTick = 0;
		this.chargeCounter = 0;
		this.failedCharge = false;
		this.goodCharge = false;
	}
}
