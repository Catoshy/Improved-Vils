package com.joshycode.improvedmobs.entity.ai;

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
	private boolean hasCompleted;
	private float distanceToObj;
	private int pathfindingFails;
	private final int pathfindingFailMax;
	private PathNavigate navigator;
	private Vec3i object;

	public VillagerAICampaignMove(EntityVillager entityHost, int pathFailMax) {
		super();
		this.hasCompleted = false;
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
		this.pathfindingFailMax = pathFailMax;
		this.entityHost = entityHost;
		this.navigator = entityHost.getNavigator();
		this.setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute() {
		if(this.entityHost.world.isRemote) {
			return false;
		}
		if(this.hasCompleted || this.object == null)
			return false;
		boolean b = getHungry();
		if(InventoryUtil.doesInventoryHaveItem(this.entityHost.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 && !b)
			return true;
		return false;
	}
	
	private boolean getHungry() {
		try {
			return this.entityHost.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException e) {}
		return true;
	}

	public void startExecuting() {
		if(this.object != null) {
			boolean b = this.navigator.tryMoveToXYZ(this.object.getX() + .5d, this.object.getY(), this.object.getZ() + .5d, .6d);
			Path p = this.navigator.getPath();
			if(p != null) {
				PathPoint pp = p.getFinalPathPoint();
				if(b && pp != null) {
					this.distanceToObj = pp.distanceTo(new PathPoint(this.object.getX(), this.object.getY(), this.object.getZ()));
					System.out.println("distance to target = " + this.distanceToObj);
				}
			}
		}
	}
	
	@Override
	public void updateTask() {
		if(!this.hasCompleted) {
			if(this.object != null) {
				Path p = this.navigator.getPath();
				if(this.entityHost.getDistanceSq(new BlockPos(this.object)) < 4D) {
					System.out.println("updateTask() wntity in range, path finished");
					this.hasCompleted = true;
					this.object = null;
					Vec3d vec = getPosition();
					this.navigator.clearPath();
					if(vec != null)
						this.navigator.tryMoveToXYZ(vec.x, vec.y, vec.z, .6D);
				} else if(p != null && p.isFinished()) {
					System.out.println("updateTask() path not null, path finished");
					this.tryToGetCloser();
				} else if(p == null) {
					this.resetTask();
				}
			}
		}
	}
	
	public void resetTask() {
		this.hasCompleted = true;
		this.distanceToObj = 0;
		this.object = null;
	}
	
	private void tryToGetCloser() {
		if(this.pathfindingFails < this.pathfindingFailMax) {
			if(this.object != null) {
				boolean b = this.navigator.tryMoveToXYZ(this.object.getX() + .5d, this.object.getY(), this.object.getZ() + .5d, .6d);
				Path p = this.navigator.getPath();
				if(p != null) {
					PathPoint pp = p.getFinalPathPoint();
					if(b && pp != null) {
						float newDistanceToObj = pp.distanceTo(new PathPoint(this.object.getX(), this.object.getY(), this.object.getZ()));
						if(newDistanceToObj >= this.distanceToObj) {
							this.pathfindingFails++;
							this.distanceToObj = newDistanceToObj;
						}
						System.out.println("distance to target = " + this.distanceToObj);
					}
				}
			}
		} else {
			this.resetTask();
		}
	}
	
    protected Vec3d getPosition() {
        return RandomPositionGenerator.findRandomTarget(this.entityHost, 8, 6);
    }
    
   // protected Vec3d randomBlockTowards() {
    //	return RandomPositionGenerator.findRandomTargetBlockTowards(this.entityHost, 14, 3, new Vec3d(this.object));
    //}
    
    public void giveObjectiveBlockPos(BlockPos pos) {
    	this.hasCompleted = false;
    	this.object = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }
    
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
    				this.giveObjectiveBlockPos(pos);
    				return;
    			}
    		}
    		this.giveObjectiveBlockPos(new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
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
    				this.giveObjectiveBlockPos(pos);
    				return;
    			}
    		}
    		this.giveObjectiveBlockPos(new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
    				this.entityHost.posY, (c.z << 4) + this.entityHost.getRNG().nextInt(15)));
    	}
    }
}
