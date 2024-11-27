package com.joshycode.improvedvils.entity.ai;

import java.util.concurrent.ThreadLocalRandom;

import org.jline.utils.Log;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;

public class VillagerAIGuard extends EntityAIGoFar{

	EntityVillager villager;
	private final int maxDistanceSq;
	private final int minDistanceSq;
	private boolean setFailed;
	private int randomTick;

	public VillagerAIGuard(EntityVillager entityHost, int maxDistSq, int minDistSq, int maxPathFails)
	{
		super(entityHost, minDistSq, maxPathFails);
		this.villager = entityHost;
		this.maxDistanceSq = maxDistSq;
		this.minDistanceSq = minDistSq;
		this.setFailed = false;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getGuardBlockPos(this.villager) == null || this.setFailed || VilMethods.isRefillingFood(this.villager) || this.villager.isMating())
			return false;
		if(VilMethods.getHungry(this.villager) || !VilMethods.getDuty(this.villager))
		{
			this.fail();
			return false;
		}
		if(this.villager.getAttackTarget() == null && this.randomTick-- == 0)
		{
			this.randomTick = ThreadLocalRandom.current().nextInt(40, 80 + 1);
			return true;
		}
		if(this.villager.getAttackTarget() != null)
		{
			if(this.villager.getDistanceSq(VilMethods.getGuardBlockPos(this.villager)) > this.maxDistanceSq)
			{
				return true;
			}
		}
		else
		{
			if(this.villager.getDistanceSq(VilMethods.getGuardBlockPos(this.villager)) > this.minDistanceSq &&
					(this.villager.ticksExisted - this.villager.getLastAttackedEntityTime()) > 40)
			{
				if(this.theDebugVar)
					Log.info("Guard too far away, must come closer");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(VilMethods.getGuardBlockPos(this.villager) == null || this.setFailed || this.finished)
			return false;
		if(!super.shouldContinueExecuting())
		{
			
				Log.info("failing out guard %s", VilMethods.getLastDoor(this.villager));
			this.fail();
			return false;
		}
		return true;
	}

	@Override
	public void startExecuting()
	{
		super.startExecuting();
		if(this.theDebugVar)
			Log.info("Guard startExecuting!");
		VilMethods.setReturning(this.villager, true);
	}

	public void fail()
	{
		if(this.theDebugVar)
			Log.info("fail state!");
		this.villager.getNavigator().clearPath();
		this.setFailed = true;
		VilMethods.setReturning(this.villager, false);
		VilMethods.setGuardBlock(this.villager, null);
		VilMethods.setLastDoor(this.villager, null);
	}
	
	public void returnState()
	{
		this.setFailed = false;
	}
	
	protected BlockPos getObjectiveBlock() 
	{ 
		return VilMethods.getGuardBlockPos(this.villager); 
	}
	
	protected boolean breakDoors() { return true; }
	
	protected void arrivedAtObjective() 
	{
		this.setFailed = false;
		VilMethods.setReturning(this.villager, false);
		VilMethods.setLastDoor(this.villager, null);
	}
}
