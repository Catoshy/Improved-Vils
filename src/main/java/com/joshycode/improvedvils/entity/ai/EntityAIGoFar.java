package com.joshycode.improvedvils.entity.ai;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.PathUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
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
	protected final boolean countFails;
	protected final boolean canSoftReset;
	protected PathNavigate navigator;
	protected boolean finished;
	protected boolean theDebugVar;
	protected int idleTicks;
	private BlockPos earlierObj;
	private boolean lastDoorSeen;
	private boolean passedBackThrough;
	private boolean pathingToLastDoor;
	protected Path path;
	private float distanceToObj;
	private float prevPathDistance;
	private int ppathIndex;
	private int imHereDistanceSq;
	
	public EntityAIGoFar(EntityCreature entityHost, int imHereDistanceSq, int mostFails, boolean canSoftReset)
	{
		super();
		this.distanceToObj = 0;
		this.prevPathDistance = 1;
		this.pathfindingFails = 0;
		this.idleTicks = 0;
		this.ppathIndex = 0;
		this.imHereDistanceSq = imHereDistanceSq;
		this.countFails = mostFails >= 0;
		this.mostPathfindingFails = mostFails;
		this.entityHost = entityHost;
		this.navigator = entityHost.getNavigator();
		this.lastDoorSeen = false;
		this.passedBackThrough = false;
		this.pathingToLastDoor = false;
		this.finished = false;
		this.theDebugVar = false;
		this.canSoftReset = canSoftReset;
		this.setMutexBits(3);
	}

	public EntityAIGoFar(EntityCreature villager, int imHereDistanceSq) 
	{
		this(villager, imHereDistanceSq, -1, false);
	}

	@Override
	public void startExecuting()
	{
		this.finished = false;
		BlockPos object = getObjectiveBlock();
		if(breakDoors())
			this.lastDoorSeen = VilMethods.getLastDoor((EntityVillager) this.entityHost) != null; //Cap (VilMethod) BlockPos var is for permanence across AIs
		((PathNavigateGround) this.navigator).setBreakDoors(this.breakDoors());
		if(generatePath())//TODO
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
		if(!this.entityHost.world.getEntitiesInAABBexcluding(entityHost, this.entityHost.getEntityBoundingBox().grow(4D), 
				new Predicate<Entity>() {

					@Override
					public boolean apply(Entity arg0) {
						return arg0 instanceof EntityPlayer;
					}
			
		}).isEmpty() && ConfigHandler.debug)
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
		else if(!object.equals(this.earlierObj))
		{
			this.earlierObj = object;
			if(!this.tryToGetCloser(object)) return;
		}
		if(!this.entityHost.isOnLadder() && this.idleTicks >  20
				|| this.idleTicks > 60)
		{
			this.idleTicks = 0;
			if(this.canSoftReset) this.resetTask();
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

	public boolean shouldContinueExecuting()
	{
		return this.pathfindingFails < this.mostPathfindingFails;
	}

	private void setDoorReference()
	{
		int i = this.path.getCurrentPathIndex();
		PathPoint point = this.path.getPathPointFromIndex(i);
		BlockPos pointPos = new BlockPos(point.x, point.y, point.z);
		PathNodeType type = this.navigator.getNodeProcessor().getPathNodeType(this.entityHost.getEntityWorld(), point.x, point.y, point.z);
		if(type == PathNodeType.DOOR_OPEN || type == PathNodeType.DOOR_WOOD_CLOSED /*|| type == PathNodeType.TRAPDOOR TODO*/)
		{
			BlockPos savedPos = VilMethods.getLastDoor((EntityVillager) this.entityHost);
			if(this.theDebugVar)
				Log.info("point, pointPos, type, savedPos, villager name; " + point + " " + pointPos + " " + type + " " + savedPos + " " + this.entityHost.getName());
			if(savedPos != null && !pointPos.equals(savedPos) || //villager treads through door: here checks if either he has a previous saved door pos and sets to entityPos if not same,
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
			if(ConfigHandler.debug && this.theDebugVar)
				Log.info("generate path fail.");
			return false;
		}
		this.path = this.navigator.getPathToXYZ(pos.x, pos.y, pos.z);
		this.ppathIndex = 0;
		if(this.path != null)
		{
			if(ConfigHandler.debug && this.theDebugVar)
				Log.info("new Villager path is so long; %s", path.getCurrentPathLength());
			this.removePathFloatingEnds();
			if(ConfigHandler.debug && this.theDebugVar)
				Log.info("after truncating, villager path is so long; %s", path.getCurrentPathLength());
			return true;
		}
		if(ConfigHandler.debug && this.theDebugVar)
			Log.info("generate path fail.");
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
		if(ConfigHandler.debug && this.theDebugVar)
			Log.info("path fails for vill; %s", this.pathfindingFails);
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
			this.tweakPath(this.path);
			this.navigator.setPath(this.path, this.hostSpeed());
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
	
	protected void tweakPath(Path path) {}
	
	protected BlockPos getObjectiveBlock() { return null; }
	
	protected boolean breakDoors() { return false; }
	
	@Override
	public boolean isInterruptible() { return this.canSoftReset; }
	
	protected double hostSpeed() 
	{ 
		return this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() + .1D;
	}
	
	protected void resetObjective() {}
	
	protected void arrivedAtObjective() {}
	

	@Override
	public void resetTask()
	{
		if(this.canSoftReset && this.pathfindingFails < this.mostPathfindingFails && this.getObjectiveBlock() != null && !this.finished)	
		{
			if(this.theDebugVar)
				Log.info("soft reset task");
			this.path = null;
			this.navigator.clearPath();
			this.idleTicks = 0;
			this.distanceToObj = 0;
		}
		else
		{
			if(this.theDebugVar)
				Log.info("hard reset task");
			resetObjective();
			this.path = null;
			this.navigator.clearPath();
			this.idleTicks = 0;
			this.distanceToObj = 0;
			this.pathfindingFails = 0;
		}
	}
}
