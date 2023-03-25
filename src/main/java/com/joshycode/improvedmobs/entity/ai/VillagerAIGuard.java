package com.joshycode.improvedmobs.entity.ai;

import java.util.UUID;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class VillagerAIGuard extends EntityAIBase{

	EntityVillager entityHost;
	BlockPos ppos;
	private float distToCenter;
	private int pathFails;
	private final int maxDistanceSq;
	private final int minDistSq;
	private final int maxPathFails;
	private Path path;
	private boolean setFailed;
	private int tickCounter;
	
	public VillagerAIGuard(EntityVillager entityHost, int maxDistSq, int minDistSq, int maxPathFails) {
		super();
		this.entityHost = entityHost;
		this.maxDistanceSq = maxDistSq;
		this.minDistSq = minDistSq;
		this.distToCenter = 0;
		this.pathFails = 0;
		this.tickCounter = -1;
		this.maxPathFails = maxPathFails;
		this.setFailed = false;
	}
	
	@Override
	public boolean shouldExecute() {
		if(getGuardPos() == null) 
		return false;
		if(this.entityHost.getDistanceSq(getGuardPos()) > this.maxDistanceSq && 
				(this.entityHost.ticksExisted - this.entityHost.getLastAttackedEntityTime()) > 40) {
			if(this.generatePath()) {
				System.out.println("should execute");
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldContinueExecuting() {
		if(getGuardPos() == null)
			return false;
		if(this.entityHost.getDistanceSq(getGuardPos()) < this.minDistSq) {
			this.returnState();
			return false;
		} else if (this.tickCounter > 40) {
			System.out.println("this.tickCounter > 40");
			this.fail();
			return false;
		}
		return true;
	}
	
	@Override
	public void startExecuting() {
		this.setReturning(true);
		this.entityHost.getNavigator().setPath(this.path, .7d);
		this.ppos = this.entityHost.getPosition();
	}
	
	private boolean generatePath() {
		Vec3d pos;
		if(this.entityHost.getDistanceSq(getGuardPos()) > CommonProxy.GUARD_IGNORE_LIMIT) {
			pos = RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 16, 7,
					guardPosAsVec());
		} else {
			pos = guardPosAsVec();
		}
		this.path = this.entityHost.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z);
		if(this.path != null) {
			System.out.println("generatePath -- path");
			return true;
		}
		return false;
	}

	@Override
	public void updateTask() {
		if(this.setFailed || this.entityHost.isAirBorne)
			return;
		if(isHungry()) {
			this.fail();
		}
		if(this.path == null) {
			if(this.pathFails < this.maxPathFails) {
				if(!generatePath()) {
					System.out.println("Path failed");
					this.pathFails++;
				}
			} else {
				this.fail();
			}
			return;
		}
		if(this.path.isFinished()) {
			if(this.entityHost.getDistanceSq(getGuardPos()) <= this.minDistSq) {
				this.returnState();
			} else {
				if(this.pathFails < this.maxPathFails) {
					float distanceCenter = 9999f;
					if(this.path.getFinalPathPoint() != null)
						distanceCenter = this.path.getFinalPathPoint().distanceToSquared(guardPosAsPP());
					if(distanceCenter > this.distToCenter) {
						this.pathFails++;
					}
					this.generatePath();
				} else {
					this.fail();
				}
			}
		}
		if(this.entityHost.getPosition().equals(ppos)) {
			this.tickCounter++;
		} else {
			this.tickCounter = 0;
		}
		this.ppos = this.entityHost.getPosition();
	}
	
	private boolean getHungry() {
		try {
			return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException e) {}
		return true;
	}
	
	private void setReturning(boolean isReturning) {
		try {
			System.out.println("Setting return ... " + isReturning);
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setReturning(isReturning);
		} catch (NullPointerException e) {}
	}
	
	private boolean isHungry() {
		try {
			return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException e) {}
		return false;
	}
	
	private BlockPos getGuardPos() {
		try {
			return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getBlockPos();
		} catch (NullPointerException e) {}
		return null;
	}
	
	private PathPoint guardPosAsPP() {
		try {
			BlockPos pos = getGuardPos();
			return new PathPoint(pos.getX(), pos.getY(), pos.getZ());
		} catch (NullPointerException e) {}
		return null;
	}
	
	private Vec3d guardPosAsVec() {
		try {
			BlockPos pos = getGuardPos();
			return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		} catch (NullPointerException e) {}
		return null;
	}
	
	public void returnState() {
		System.out.println("RETURNED STATE");
		this.setFailed = false;
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		setReturning(false);
	}
	
	public void fail() {
		System.out.println("FAILED");
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		setReturning(false);
		this.setFailed = true;
		try {
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(null);
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setReturning(false);
		} catch (NullPointerException e) {}		
	}
}
