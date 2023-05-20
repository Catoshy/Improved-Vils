package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.PathUtil;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
	private boolean finished;

	public VillagerAICampaignMove(EntityVillager entityHost, int pathFailMax) 
	{
		super();
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
		this.idleTicks = 0;
		this.pathfindingFailMax = pathFailMax;
		this.entityHost = entityHost;
		this.navigator = entityHost.getNavigator();
		this.finished = false;
		this.setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute() 
	{
		if(VilMethods.getCommBlockPos((EntityVillager) this.entityHost) == null)
			return false;
		if(VilMethods.getGuardBlockPos((EntityVillager) this.entityHost) != null)
			return false;
		if(((EntityVillager) this.entityHost).isMating())
    		return false;
		if(VilMethods.getFollowing((EntityVillager) this.entityHost))
			return false;
		if(InventoryUtil.doesInventoryHaveItem(this.entityHost.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0 && !VilMethods.getHungry(this.entityHost)) {
			return true;
		}
		return false;
	}

	public void startExecuting() 
	{
		this.finished = false;
		BlockPos object = VilMethods.getCommBlockPos(this.entityHost);
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null) 
			{
				this.distanceToObj = pp.distanceTo(new PathPoint(object.getX(), object.getY(), object.getZ()));
			}
			this.entityHost.getNavigator().setPath(this.path, .7D);
		}
		this.ppos = this.entityHost.getPosition();
	}
	
	@Override
	public void updateTask() 
	{
		BlockPos object = VilMethods.getCommBlockPos(this.entityHost);
		if(object != null) 
		{
			if(this.idleTicks > 20) 
			{
				this.idleTicks = 0;
				this.tryToGetCloser(object);
				this.pathfindingFails++;
				return;
			} 
			else if(this.entityHost.getDistanceSq(object) < 4D)
			{
				this.resetTask();
				Vec3d vec = getPosition();
				this.navigator.clearPath();
				this.finished = true;
				
				if(vec != null)
					this.navigator.tryMoveToXYZ(vec.x, vec.y, vec.z, .6D);
				
			} 
			else if(this.path != null && this.path.isFinished())
			{
				if(!this.finished)
					this.tryToGetCloser(object);
				else
					this.resetTask();
			} 
			else if(this.path == null)
			{
				this.pathfindingFails++;
				this.generatePath();
			}
			
			if(this.entityHost.getPosition().equals(ppos)) 
			{
				this.idleTicks++;
			} 
			else 
			{
				this.idleTicks = 0;
			}
			this.ppos = this.entityHost.getPosition();
		} 
		else 
		{
			this.resetTask();
		}
	}
	
	@Override
	public boolean shouldContinueExecuting() 
	{
		if(VilMethods.getGuardBlockPos(this.entityHost) == null && VilMethods.getCommBlockPos(this.entityHost) != null)
		{
			if(this.pathfindingFails < this.pathfindingFailMax)
			{
				return true;
			} 
		}
		return false;
	}
	
	@Override
	public void resetTask() 
	{
		VilMethods.setCommBlockPos(this.entityHost, null);
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
	}
	
	private boolean generatePath() 
	{
		Vec3d pos;
		if(this.entityHost.getDistanceSq(VilMethods.getCommBlockPos(this.entityHost)) > CommonProxy.GUARD_MAX_PATH)
		{
			Vec3d pos1 = PathUtil.findBlockInDirection(this.entityHost.getPosition(), VilMethods.getCommBlockPos(this.entityHost));
			if(pos1 != null) 
			{
				pos = pos1;
			}
			else 
			{
				pos = RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 16, 7, VilMethods.commPosAsVec(this.entityHost));
			}
		}
		else
		{
			pos = VilMethods.commPosAsVec(this.entityHost);
		}
		if(pos == null) 
		{
			this.pathfindingFails++;
			return false;
		}
		this.path = this.entityHost.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z);
		if(this.path != null) 
		{
			return true;
		}
		return false;
	}

	private void tryToGetCloser(BlockPos object)
	{
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null) 
			{
				float newDistanceToObj = pp.distanceTo(new PathPoint(object.getX(), object.getY(), object.getZ()));
				if(newDistanceToObj >= this.distanceToObj) 
				{
					this.pathfindingFails++;
				}
				this.distanceToObj = newDistanceToObj;
			}
			this.entityHost.getNavigator().setPath(this.path, .7D);
		}
	}
	
    protected Vec3d getPosition()
    {
        return RandomPositionGenerator.findRandomTarget(this.entityHost, 8, 6);
    }
   
    public void giveObjectiveChunkPos(int x, int z) 
    {
    	Chunk c = this.entityHost.getWorld().getChunkFromBlockCoords(new BlockPos(this.entityHost));
    	BlockPos entPos = this.entityHost.getPos();
    	int entX = entPos.getX();
    	int entY = entPos.getY();
    	int entZ = entPos.getZ();
    	
    	int dX = x - c.x;
    	int dZ = z - c.z;
    	
    	if(Math.abs(dX) > Math.abs(dZ))
    	{
    		double slope = dZ / Math.abs(dX);
    		double runZ = entZ;
    		int runX = entX;
    		int step = 1;
    		if(dX < 0)
    			step = -1;
    		
    		for(int i = 0; i < 200; i++) 
    		{
    			runZ += slope;
    			runX += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(this.entityHost.getWorld().getChunkFromBlockCoords(pos).equals(c)) 
    			{
    				VilMethods.setCommBlockPos(this.entityHost, pos);
    				return;
    			}
    		}
    		VilMethods.setCommBlockPos(this.entityHost, new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
    				this.entityHost.posY, (c.z << 4) + this.entityHost.getRNG().nextInt(15)));
    	} 
    	else 
    	{
    		double slope = dX / Math.abs(dZ);
    		double runX = entX;
    		int runZ = entZ;
    		int step = 1;
    		if(dZ < 0)
    			step = -1;
    		
    		for(int i = 0; i < 200; i++) 
    		{
    			runX += slope;
    			runZ += step;
    			BlockPos pos = new BlockPos(runX, entY, runZ);
    			if(this.entityHost.getWorld().getChunkFromBlockCoords(pos).equals(c)) 
    			{
    				VilMethods.setCommBlockPos(this.entityHost, pos);
    				return;
    			}
    		}
    		VilMethods.setCommBlockPos(this.entityHost, new BlockPos((c.x << 4) + this.entityHost.getRNG().nextInt(15),
    				this.entityHost.posY, (c.z << 4) + this.entityHost.getRNG().nextInt(15)));
    	}
    }
}
