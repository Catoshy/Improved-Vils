package com.joshycode.improvedvils.entity.ai;

import javax.annotation.Nullable;

import org.jline.utils.Log;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.block.BlockLilyPad;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VillagerAICampaignMove extends EntityAIGoFar {

	public VillagerAICampaignMove(EntityVillager entityHost, int pathFailMax)
	{
		super(entityHost, 4, pathFailMax);
	}

	@Override
	public boolean shouldExecute()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		if((VilMethods.getCommBlockPos(villager) == null) || (VilMethods.getGuardBlockPos(villager) != null) || villager.isMating() || VilMethods.getFollowing(villager))
			return false;
		if(!VilMethods.getHungry(villager) && VilMethods.getDuty(villager)) {
			return true;
		}
		return false;
	}

	@Override
	public void updateTask()
	{
		super.updateTask();
		BlockPos front = this.entityHost.getPosition().offset(this.entityHost.getHorizontalFacing());
		if(this.entityHost.getEntityWorld().getBlockState(front).getBlock() instanceof BlockLilyPad)
			this.entityHost.getEntityWorld().destroyBlock(front, true);
		if(this.entityHost.getEntityWorld().getBlockState(front.up()).getBlock() instanceof BlockLilyPad)
			this.entityHost.getEntityWorld().destroyBlock(front.up(), true);
		if(this.entityHost.getEntityWorld().getBlockState(this.entityHost.getPosition()).getBlock() instanceof BlockLilyPad)
			this.entityHost.posY -= 0.05D;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(this.finished && (this.navigator.noPath() || this.navigator.getPath().isFinished()))
			return false;
		EntityVillager villager = (EntityVillager) this.entityHost;
		if(VilMethods.getGuardBlockPos(villager) == null && VilMethods.getCommBlockPos(villager) != null)
		{
			if(this.pathfindingFails < this.mostPathfindingFails)
			{
				return true;
			}
		}
		if(this.theDebugVar)
			Log.info("we failed the test");
		return false;
	}
    
	@Override
    protected BlockPos getObjectiveBlock() { return VilMethods.getCommBlockPos((EntityVillager) this.entityHost); }
	
	@Override
	protected boolean breakDoors() { return false; }
	
	@Override
	protected double hostSpeed() { return .7D; }
	
	@Override
	protected void resetObjective() 
	{
		VilMethods.setCommBlockPos((EntityVillager) this.entityHost, null);
	}
	
	@Override
	protected void arrivedAtObjective() 
	{
		if(this.theDebugVar)
			Log.info("we think we have arrived");
		VilMethods.setLastDoor((EntityVillager) this.entityHost, null);
		Vec3d vec = this.getRandomPosition();
		if(vec != null)
			this.navigator.tryMoveToXYZ(vec.x, vec.y, vec.z, hostSpeed());
	}
}
