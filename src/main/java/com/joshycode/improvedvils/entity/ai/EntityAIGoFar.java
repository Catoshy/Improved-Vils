package com.joshycode.improvedvils.entity.ai;

import javax.annotation.Nullable;

import org.jline.utils.Log;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.PathUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class EntityAIGoFar extends EntityAIBase {

	//TODO set from EC to Villager, will need remove redundant fields in child classes.
	protected final EntityCreature entityHost;
	protected int pathfindingFails;
	protected final int mostPathfindingFails;
	protected PathNavigate navigator;
	protected boolean finished;
	protected boolean theDebugVar;
	private boolean lastDoorSeen;
	private boolean passedBackThrough;
	private boolean pathingToLastDoor;
	private Path path;
	private float distanceToObj;
	private float prevPathDistance;
	private int idleTicks;
	private int ppathIndex;
	private int imHereDistanceSq;
	
	public EntityAIGoFar(EntityCreature entityHost, int imHereDistanceSq, int mostFails)
	{
		super();
		this.distanceToObj = 0;
		this.prevPathDistance = 1;
		this.pathfindingFails = 0;
		this.idleTicks = 0;
		this.ppathIndex = 0;
		this.imHereDistanceSq = imHereDistanceSq;
		this.mostPathfindingFails = mostFails;
		this.entityHost = entityHost;
		this.navigator = entityHost.getNavigator();
		this.lastDoorSeen = false;
		this.passedBackThrough = false;
		this.pathingToLastDoor = false;
		this.finished = false;
		this.theDebugVar = false;
		this.setMutexBits(3);
	}

	@Override
	public void startExecuting()
	{
		this.finished = false;
		BlockPos object = getObjectiveBlock();
		if(breakDoors())
			this.lastDoorSeen = VilMethods.getLastDoor((EntityVillager) this.entityHost) != null; //Cap (VilMethod) BlockPos var is for permanence across AIs
		((PathNavigateGround) this.navigator).setBreakDoors(this.breakDoors());
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null)
			{
				this.distanceToObj = pp.distanceTo(new PathPoint(object.getX(), object.getY(), object.getZ()));
			}
			this.navigator.setPath(this.path, this.hostSpeed());
			this.ppathIndex = this.path.getCurrentPathIndex();
		}
	}

	@Override
	public void updateTask()
	{
		if(!this.entityHost.world.getEntitiesInAABBexcluding(entityHost, this.entityHost.getEntityBoundingBox().grow(2D), 
				new Predicate<Entity>() {

					@Override
					public boolean apply(Entity arg0) {
						return arg0 instanceof EntityPlayer;
					}
			
		}).isEmpty())
			this.theDebugVar = true;
		else
			this.theDebugVar = false;
		BlockPos object = getObjectiveBlock();
		if(this.finished) return;
		if(object == null)
		{
			if(this.theDebugVar)
				Log.info("null object");
			this.resetTask();
			return;
		}
		if(!this.entityHost.isOnLadder() && this.idleTicks >  20
				|| this.idleTicks > 60)
		{
			this.idleTicks = 0;
			if(!this.tryToGetCloser(object)) return;
		}
		if(this.entityHost.getDistanceSq(object) < this.imHereDistanceSq)
		{
			this.navigator.clearPath();
			this.finished = true;
			arrivedAtObjective();
			return;
		}
		else if(this.path != null && this.path.isFinished())
		{
			if(this.theDebugVar)
				Log.info("path finished");
			if(!this.tryToGetCloser(object)) return;
		}
		else if(this.path == null)
		{
			this.pathfindingFails++;
			if(this.theDebugVar)
				Log.info("path == null or nav noPath");
			if(!this.tryToGetCloser(object)) return;
		}
		if(breakDoors())
			setDoorReference();
		checkIdle();
	}

	private void setDoorReference()
	{
		int i = this.path.getCurrentPathIndex();
		PathPoint point = this.path.getPathPointFromIndex(i);
		//BlockPos pointPos = new BlockPos(point.x, point.y, point.z);
		BlockPos pointPos = this.entityHost.getPosition();
		PathNodeType type = this.entityHost.getNavigator().getNodeProcessor().getPathNodeType(this.entityHost.getEntityWorld(), point.x, point.y, point.z);
		if(type == PathNodeType.DOOR_OPEN || type == PathNodeType.DOOR_WOOD_CLOSED)
		{
			BlockPos savedPos = VilMethods.getLastDoor((EntityVillager) this.entityHost);
			if(this.theDebugVar)
				Log.info("point, pointPos, type, savedPos, villager name; ", point, " ", pointPos, " ", type, " ", savedPos, " ", this.entityHost.getName());
			if(savedPos != null && !pointPos.equals(savedPos) || //villager treads through door: here checks if either he has a previous saved door pos and sets to pointPos if not same,
					savedPos == null && !this.passedBackThrough) //or if the door position is intentionally null because he's going back through, hence passedBackThrough would be true.
			{
				this.lastDoorSeen = true;
				VilMethods.setLastDoor((EntityVillager) this.entityHost, pointPos);
			}
			else if(!this.lastDoorSeen && !this.passedBackThrough) //if lastDoorSeen is true, then villager is still going through the door so he would not actually be turning around
			{													   //to pass through. if passedBackThrough is true, then the following has already been done.
				this.passedBackThrough = true;
				this.pathingToLastDoor = false;
				Log.info("setting villager; %s door pos back to null", this.entityHost.getName());
				VilMethods.setLastDoor((EntityVillager) this.entityHost, null);
			}
		}
		else
		{
			this.lastDoorSeen = false;
			this.passedBackThrough = false;
		}
	}

	public void checkIdle() 
	{
		int i = this.path.getCurrentPathIndex();
		if(i + 1 >= this.path.getCurrentPathLength())
		{
			this.idleTicks++;
			return;
		}
		PathPoint nextPoint = this.path.getPathPointFromIndex(i + 1);
		BlockPos nextPointPos = new BlockPos(nextPoint.x, nextPoint.y, nextPoint.z);
		if(this.prevPathDistance <= this.entityHost.getDistanceSq(nextPointPos) && this.ppathIndex == i)
		{
			this.idleTicks++;
		}
		else
		{
			this.idleTicks = 0;
		}
		this.prevPathDistance = (float) (this.prevPathDistance <= this.entityHost.getDistanceSq(nextPointPos) ? this.prevPathDistance : this.entityHost.getDistanceSq(nextPointPos));
		this.ppathIndex = this.path.getCurrentPathIndex();
	}

	@Override
	public void resetTask()
	{
		if(this.theDebugVar)
			Log.info("reset task");
		resetObjective();
		this.path = null;
		this.navigator.setPath(null, 0);
		this.idleTicks = 0;
		this.distanceToObj = 0;
		this.pathfindingFails = 0;
	}

	private boolean generatePath()
	{
		Vec3d pos;
		BlockPos objective = getObjectiveBlock();
		boolean flipFacing = false;
		int offsetAngle = 0;
		if(this.pathfindingFails > 8 || this.pathingToLastDoor)
		{
			if(VilMethods.getLastDoor((EntityVillager) this.entityHost) != null)
			{
				if(this.theDebugVar)
					Log.info("pathing to \"last door\"");
				objective = VilMethods.getLastDoor((EntityVillager) this.entityHost);
				this.pathingToLastDoor = true;
			}
			else 
			{
				this.pathingToLastDoor = false;
				if(this.entityHost.getRNG().nextInt(2) == 0)
					flipFacing = true;
				else
					offsetAngle = 10;
			}
		}
		if(this.entityHost.getDistanceSq(objective) > CommonProxy.GUARD_MAX_PATH_SQ)
		{
				
			Vec3d pos1 = PathUtil.findNavigableBlockInDirection(this.entityHost.getPosition(), objective, this.entityHost, offsetAngle, flipFacing);
			if(pos1 != null)
			{
				pos = pos1;
			}
			else
			{
				pos = RandomPositionGenerator.findRandomTargetBlockTowards(entityHost, 10, 7, VilMethods.asVec3D(objective));
			}
		}
		else
		{
			pos = VilMethods.asVec3D(objective);
		}
		if(pos == null)
		{
			return false;
		}
		this.path = this.navigator.getPathToXYZ(pos.x, pos.y, pos.z);
		this.ppathIndex = 0;
		if(this.path != null)
		{
			if(ConfigHandler.debug && this.theDebugVar)
				Log.info("new Villager path is so long; ", path.getCurrentPathLength());
			this.removePathFloatingEnds();
			if(ConfigHandler.debug && this.theDebugVar)
				Log.info("after truncating, villager path is so long; ", path.getCurrentPathLength());
			return true;
		}
		return false;
	}

	private void removePathFloatingEnds() 
	{
		for(int i = this.path.getCurrentPathLength() - 1; i > 0; i--)
		{
			PathPoint point = this.path.getPathPointFromIndex(i);
			BlockPos pointPos = new BlockPos(point.x, point.y, point.z);
			PathPoint beforePoint = this.path.getPathPointFromIndex(i - 1);
			BlockPos beforePointPos = new BlockPos(beforePoint.x, beforePoint.y, beforePoint.z);
			if(!pointPos.down().equals(beforePointPos)) return;
			
			IBlockState block = this.entityHost.getEntityWorld().getBlockState(beforePointPos);
			if(block.getMaterial() == Material.AIR)
				this.path.setCurrentPathLength(i - 1);
			else
				return;
		} 
	}

	private boolean tryToGetCloser(BlockPos object)
	{
		if(this.theDebugVar)
			Log.info("path fails for vill;", this.pathfindingFails);
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
				else
				{
					this.pathfindingFails = 0;
				}
				this.distanceToObj = newDistanceToObj;
			}
			this.navigator.setPath(this.path, .7D);
			return true;
		}
		this.navigator.setPath(null, 0);
		return false;
	}
	
	@Nullable
	protected Vec3d getRandomPosition()
    {
        return RandomPositionGenerator.findRandomTarget(this.entityHost, 8, 6);
    }
    
	protected BlockPos getObjectiveBlock() { return null; }
	
	protected boolean breakDoors() { return false; }
	
	protected double hostSpeed() { return .7D; }
	
	protected void resetObjective() {}
	
	protected void arrivedAtObjective() {}
}
