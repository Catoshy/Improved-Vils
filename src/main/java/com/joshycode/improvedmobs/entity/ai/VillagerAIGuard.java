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
		this.path = null;
		this.setFailed = false;
	}
	
	@Override
	public boolean shouldExecute() {
		if(CapabilityHandler.getGuardBlockPos(this.entityHost) == null || this.setFailed) 
			return false;
		if(CapabilityHandler.getHungry(this.entityHost)) {
			this.fail();
			return false;
		}
		if(this.entityHost.getDistanceSq(CapabilityHandler.getGuardBlockPos(this.entityHost)) > this.maxDistanceSq && 
				(this.entityHost.ticksExisted - this.entityHost.getLastAttackedEntityTime()) > 40) {
			if(this.generatePath()) {
				System.out.println("should execute");
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldContinueExecuting() {
		if(CapabilityHandler.getGuardBlockPos(this.entityHost) == null || this.setFailed)
			return false;
		if(this.pathFails > this.maxPathFails) {
			this.fail();
			return false;
		}
		if(this.entityHost.getDistanceSq(CapabilityHandler.getGuardBlockPos(this.entityHost)) < this.minDistSq) {
			this.returnState();
			return false;
		}
		return true;
	}
	
	@Override
	public void startExecuting() {
		CapabilityHandler.setReturning(this.entityHost, true);
		this.entityHost.getNavigator().setPath(this.path, .7d);
		this.ppos = this.entityHost.getPosition();
	}
	
	private boolean generatePath() {
		Vec3d pos;
		if(this.entityHost.getDistanceSq(CapabilityHandler.getGuardBlockPos(this.entityHost)) > CommonProxy.GUARD_MAX_PATH) {
			pos = RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 16, 8,
					CapabilityHandler.guardBlockAsVec(this.entityHost));
		} else {
			pos = CapabilityHandler.guardBlockAsVec(this.entityHost);
		}
		if(pos == null) {
			this.pathFails++;
			return false;
		}
		this.path = this.entityHost.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z);
		if(this.path != null) {
			System.out.println("generatePath -- path");
			System.out.flush();
			return true;
		}
		return false;
	}

	@Override
	public void updateTask() {
		if(this.path == null) {
			if(generatePath()) {
				System.out.println("Path failed");
				this.pathFails++;
			} else {
				this.fail();
			}
			return;
		}
		if (this.tickCounter > 20) {
			System.out.println("this.tickCounter > 40");
			this.tickCounter = 0;
			this.generatePath();
			this.pathFails++;
			return;
		}
		if(this.path.isFinished()) {
			float distanceCenter = 9999f;
			if(this.path.getFinalPathPoint() != null)
				distanceCenter = this.path.getFinalPathPoint().distanceToSquared(
						CapabilityHandler.guardBlockAsPP(this.entityHost)
						);
			if(distanceCenter > this.distToCenter) {
				this.pathFails++;
			}
			this.generatePath();
		}
		if(this.entityHost.getPosition().equals(ppos)) {
			this.tickCounter++;
		} else {
			this.tickCounter = 0;
		}
		this.ppos = this.entityHost.getPosition();
	}
	
	public void returnState() {
		System.out.println("RETURNED STATE");
		this.setFailed = false;
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		CapabilityHandler.setReturning(this.entityHost, false);
	}
	
	public void fail() {
		System.out.println("FAILED");
		this.entityHost.getNavigator().clearPath();
		this.tickCounter = -1;
		this.path = null;
		this.pathFails = 0;
		CapabilityHandler.setReturning(this.entityHost, false);
		this.setFailed = true;
		try {
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(null);
			this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setReturning(false);
		} catch (NullPointerException e) {}		
	}
}
