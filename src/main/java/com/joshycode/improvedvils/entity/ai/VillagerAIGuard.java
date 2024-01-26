package com.joshycode.improvedvils.entity.ai;

import java.util.concurrent.ThreadLocalRandom;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.PathUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VillagerAIGuard extends EntityAIBase{

	EntityVillager entityHost;
	BlockPos ppos;
	private float distToCenter;
	private int pathFails;
	private final int maxDistanceSq;
	private final int minDistanceSq;
	private final int maxPathFails;
	private Path path;
	private boolean setFailed;
	private int tickCounter;
	private int randomTick;

	public VillagerAIGuard(EntityVillager entityHost, int maxDistSq, int minDistSq, int maxPathFails)
	{
		super();
		this.entityHost = entityHost;
		this.maxDistanceSq = maxDistSq;
		this.minDistanceSq = minDistSq;
		this.distToCenter = 0;
		this.pathFails = 0;
		this.tickCounter = -1;
		this.maxPathFails = maxPathFails;
		this.path = null;
		this.setFailed = false;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if(VilMethods.getGuardBlockPos(this.entityHost) == null || this.setFailed || VilMethods.isRefillingFood(this.entityHost) || this.entityHost.isMating())
			return false;
		if(VilMethods.getHungry(this.entityHost) || !VilMethods.getDuty(this.entityHost))
		{
			this.fail();
			return false;
		}
		if(this.entityHost.getAttackTarget() == null && this.randomTick == 0)
		{
			this.randomTick = ThreadLocalRandom.current().nextInt(40, 80 + 1);
			return true;
		}
		else
		{
			this.randomTick--;
		}
		if(this.entityHost.getAttackTarget() != null)
		{
			if(this.entityHost.getDistanceSq(VilMethods.getGuardBlockPos(this.entityHost)) > this.maxDistanceSq)
			{
				if(this.generatePath(false))
				{
					return true;
				}
			}
		}
		else
		{
			if(this.entityHost.getDistanceSq(VilMethods.getGuardBlockPos(this.entityHost)) > this.minDistanceSq &&
					(this.entityHost.ticksExisted - this.entityHost.getLastAttackedEntityTime()) > 40)
			{
				if(this.generatePath(false))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(VilMethods.getGuardBlockPos(this.entityHost) == null || this.setFailed)
			return false;
		if(this.pathFails > this.maxPathFails)
		{
			this.fail();
			return false;
		}
		if(this.entityHost.getDistanceSq(VilMethods.getGuardBlockPos(this.entityHost)) < this.minDistanceSq)
		{
			this.returnState();
			return false;
		}
		return true;
	}

	@Override
	public void startExecuting()
	{
		VilMethods.setReturning(this.entityHost, true);
		this.entityHost.getNavigator().setPath(this.path, .7d);
		this.ppos = this.entityHost.getPosition();
	}

	private boolean generatePath(boolean awayFrom)
	{
		Vec3d pos;
		if(this.entityHost.getDistanceSq(VilMethods.getGuardBlockPos(this.entityHost)) > CommonProxy.GUARD_MAX_PATH_SQ)
		{
			if(!awayFrom)
				pos = PathUtil.findNavigableBlockInDirection(this.entityHost.getPosition(), VilMethods.getGuardBlockPos(this.entityHost), this.entityHost);
			else
				pos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entityHost, 7, 4, VilMethods.guardBlockAsVec(entityHost));
		}
		else
		{
			pos = VilMethods.guardBlockAsVec(this.entityHost);
		}
		if(pos == null)
		{
			this.pathFails++;
			return false;
		}
		this.path = this.entityHost.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z);
		if(this.path != null)
		{
			return true;
		}
		return false;
	}

	@Override
	public void updateTask()
	{
		if(this.path == null)
		{
			this.pathFails++;
			if(generatePath(false))
			{
				this.entityHost.getNavigator().setPath(this.path, .7d);
			}
			return;
		}
		if (this.tickCounter > 20)
		{
			this.tickCounter = 0;
			this.pathFails++;
			if(generatePath(true))
			{
				this.entityHost.getNavigator().setPath(this.path, .7d);
			}
			return;
		}
		if(this.path.isFinished())
		{
			float distanceCenter = 9999f;

			if(this.path.getFinalPathPoint() != null)
				distanceCenter = this.path.getFinalPathPoint().distanceToSquared(VilMethods.guardBlockAsPP(this.entityHost));

			if(distanceCenter > this.distToCenter)
			{
				this.pathFails++;
			}
			if(this.generatePath(false))
			{
				this.entityHost.getNavigator().setPath(this.path, .7d);
			}
		}
		if(this.entityHost.getPosition().equals(ppos))
		{
			this.tickCounter++;
		}
		else
		{
			this.tickCounter = 0;
		}
		this.ppos = this.entityHost.getPosition();
	}

	@Override
	public void resetTask()
	{
		this.pathFails = 0;
		this.entityHost.getNavigator().clearPath();
		this.path = null;
	}

	public void returnState()
	{
		this.setFailed = false;
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		VilMethods.setReturning(this.entityHost, false);
	}

	public void fail()
	{
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		this.setFailed = true;
		try
		{
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(null).setReturning(false);
		} catch (NullPointerException e) {}
	}
}
