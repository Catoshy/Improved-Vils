package com.joshycode.improvedmobs.entity.ai;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;

public class VillagerAICampaignMove extends EntityAIBase {
	
	private final EntityVillager entityHost;
	private float distanceToObj;
	private int idleTicks;
	private int pathfindingFails;
	private final int pathfindingFailMax;
	private PathNavigate navigator;
	private Path path;
	private BlockPos ppos;

	public VillagerAICampaignMove(EntityVillager entityHost, int pathFailMax) {
		super();
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
		this.idleTicks = 0;
		this.pathfindingFailMax = pathFailMax;
		this.entityHost = entityHost;
		this.navigator = entityHost.getNavigator();
		this.setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute() {
		if(CapabilityHandler.getCommBlockPos(this.entityHost) == null || CapabilityHandler.getGuardBlockPos(this.entityHost) != null) {
			return false;
		}
		if(InventoryUtil.doesInventoryHaveItem(this.entityHost.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 && !CapabilityHandler.getHungry(this.entityHost)) {
			System.out.println("should execute - campaign move " + CapabilityHandler.getCommBlockPos(this.entityHost).toString());
			return true;
		}
		return false;
	}

	public void startExecuting() {
		BlockPos object = CapabilityHandler.getCommBlockPos(this.entityHost);
		if(generatePath()) {
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null) {
				this.distanceToObj = pp.distanceTo(new PathPoint(object.getX(), object.getY(), object.getZ()));
				System.out.println("distance to target = " + this.distanceToObj);
			}
			this.entityHost.getNavigator().setPath(this.path, .7D);
		}
		this.ppos = this.entityHost.getPosition();
	}
	
	@Override
	public void updateTask() {
		BlockPos object = CapabilityHandler.getCommBlockPos(this.entityHost);
		if(object != null) {
			//System.out.println("update task");
			if(this.idleTicks > 20) {
				System.out.println("this.tickCounter > 40");
				this.idleTicks = 0;
				this.tryToGetCloser(object);
				this.pathfindingFails++;
				return;
			} else if(this.entityHost.getDistanceSq(object) < 4D) {
				System.out.println("updateTask() wntity in range, path finished");
				this.resetTask();
				Vec3d vec = getPosition();
				this.navigator.clearPath();
				if(vec != null)
					this.navigator.tryMoveToXYZ(vec.x, vec.y, vec.z, .6D);
			} else if(this.path != null && this.path.isFinished()) {
				System.out.println("updateTask() path not null, path finished");
				this.tryToGetCloser(object);
			} else if(this.path == null) {
				System.out.println("updateTask() path is null");
				this.pathfindingFails++;
				this.generatePath();
			}
			if(this.entityHost.getPosition().equals(ppos)) {
				this.idleTicks++;
			} else {
				this.idleTicks = 0;
			}
			this.ppos = this.entityHost.getPosition();
		} else {
			System.out.println("object is null");
			this.resetTask();
		}
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		if(CapabilityHandler.getGuardBlockPos(this.entityHost) == null && CapabilityHandler.getCommBlockPos(this.entityHost) != null) {
			if(this.pathfindingFails < this.pathfindingFailMax) {
				return true;
			} else {
				System.out.println("manual reset");
				this.resetTask();
			}
		}
		return false;
	}
	
	@Override
	public void resetTask() {
		System.out.println("resetting task");
		CapabilityHandler.setCommBlockPos(this.entityHost, null);
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
	}
	
	private boolean generatePath() {
		Vec3d pos;
		//TODO
		if(this.entityHost.getDistanceSq(CapabilityHandler.getCommBlockPos(this.entityHost)) > CommonProxy.GUARD_MAX_PATH) {
			Vec3d pos1 = VillagerAICampaignMove.findBlockInDirection(this.entityHost.getPosition(), CapabilityHandler.getCommBlockPos(this.entityHost));
			if(pos1 != null) {
				System.out.println("generatePath -- pos1 not null, = " + pos1.toString());
				pos = pos1;/*RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 16, 8, pos1);*/
			} else {
				pos = RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 16, 8, CapabilityHandler.commPosAsVec(this.entityHost));
			}
		} else {
			pos = CapabilityHandler.commPosAsVec(this.entityHost);
		}
		if(pos == null) {
			System.out.println("generatePath -- pos == null");
			this.pathfindingFails++;
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

	private void tryToGetCloser(BlockPos object) {
		if(object != null) {
			if(generatePath()) {
				PathPoint pp = this.path.getFinalPathPoint();
				if(pp != null) {
					float newDistanceToObj = pp.distanceTo(new PathPoint(object.getX(), object.getY(), object.getZ()));
					if(newDistanceToObj >= this.distanceToObj) {
						this.pathfindingFails++;
						this.distanceToObj = newDistanceToObj;
					}
					System.out.println("distance to target = " + this.distanceToObj);
				}
				this.entityHost.getNavigator().setPath(this.path, .7D);
			}
		}
	}
	
    protected Vec3d getPosition() {
        return RandomPositionGenerator.findRandomTarget(this.entityHost, 8, 6);
    }
    
   // protected Vec3d randomBlockTowards() {
    //	return RandomPositionGenerator.findRandomTargetBlockTowards(this.entityHost, 14, 3, new Vec3d(this.object));
    //}
   
    public void giveObjectiveChunkPos(int x, int z) {
    	Chunk c = this.entityHost.getWorld().getChunkFromBlockCoords(new BlockPos(this.entityHost));
    	BlockPos entPos = this.entityHost.getPos();
    	int entX = entPos.getX();
    	int entY = entPos.getY();
    	int entZ = entPos.getZ();
    	
    	int dX = x - c.x;
    	int dZ = z - c.z;
    	
    	if(Math.abs(dX) > Math.abs(dZ)) {
    		double slope = dZ / Math.abs(dX);
    		double runZ = entZ;
    		int runX = entX;
    		int step = 1;
    		if(dX < 0)
    			step = -1;
    		
    		for(int i = 0; i < 200; i++) {
    			runZ += slope;
    			runX += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(this.entityHost.getWorld().getChunkFromBlockCoords(pos).equals(c)) {
    				CapabilityHandler.setCommBlockPos(this.entityHost, pos);
    				return;
    			}
    		}
    		CapabilityHandler.setCommBlockPos(this.entityHost, new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
    				this.entityHost.posY, (c.z << 4) + this.entityHost.getRNG().nextInt(15)));
    	} else {
    		double slope = dX / Math.abs(dZ);
    		double runX = entX;
    		int runZ = entZ;
    		int step = 1;
    		if(dZ < 0)
    			step = -1;
    		
    		for(int i = 0; i < 200; i++) {
    			runX += slope;
    			runZ += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(this.entityHost.getWorld().getChunkFromBlockCoords(pos).equals(c)) {
    				CapabilityHandler.setCommBlockPos(this.entityHost, pos);
    				return;
    			}
    		}
    		CapabilityHandler.setCommBlockPos(this.entityHost, new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
    				this.entityHost.posY, (c.z << 4) + this.entityHost.getRNG().nextInt(15)));
    	}
    }
    
    @Nullable
	private static Vec3d findBlockInDirection(BlockPos start, BlockPos dest) {
    	int entX = start.getX();
    	int entY = start.getY();
    	int entZ = start.getZ();
    	
    	int dX = dest.getX() - entX;
    	int dZ = dest.getZ() - entZ;
    	
    	if(Math.abs(dX) > Math.abs(dZ)) {
    		double slope = dZ / Math.abs(dX);
    		double runZ = entZ;
    		int runX = entX;
    		int step = 1;
    		if(dX < 0)
    			step = -1;
    		
    		for(int i = 0; i < 20; i++) {
    			runZ += slope;
    			runX += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(pos.getDistance(entX, entY, entZ) >= 16) {
    				return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    			}
    		}
    	} else {
    		double slope = dX / Math.abs(dZ);
    		double runX = entX;
    		int runZ = entZ;
    		int step = 1;
    		if(dZ < 0)
    			step = -1;
    		
    		for(int i = 0; i < 200; i++) {
    			runX += slope;
    			runZ += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(pos.getDistance(entX, entY, entZ) >= 16) {
    				return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    			}
    		}
    	}
    	return null;
	}
}
