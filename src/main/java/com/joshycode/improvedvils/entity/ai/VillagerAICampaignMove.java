package com.joshycode.improvedvils.entity.ai;

import org.jline.utils.Log;

import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;

import net.minecraft.block.BlockLilyPad;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VillagerAICampaignMove extends EntityAIGoFar {
	
	private TroopCommands order;

	public VillagerAICampaignMove(EntityVillager entityHost, int pathFailMax)
	{
		super(entityHost, 4, pathFailMax, true);
		this.order = TroopCommands.NONE;
	}

	@Override
	public boolean shouldExecute()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		this.order = VilMethods.getTroopFaring(villager);
		if(!isRightCommands(villager) || 
				(VilMethods.getGuardBlockPos(villager) != null) || 
				villager.isMating() || 
				VilMethods.getFollowing(villager) ||
				VilMethods.getHungry(villager) ||
				!VilMethods.getDuty(villager))
		{
			return false;
		}
		return true;
	}

	private boolean isRightCommands(EntityVillager villager) 
	{
		return VilMethods.getCommBlockPos(villager) != null 
				&& 
				(this.order == TroopCommands.FORWARD || this.order == TroopCommands.FORWARD_ATTACK);
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
		if(this.order == TroopCommands.FORWARD_ATTACK && this.entityHost.getAttackTarget() != null)
		{
			return false;
		}
		EntityVillager villager = (EntityVillager) this.entityHost;
		if(VilMethods.getGuardBlockPos(villager) == null && VilMethods.getCommBlockPos(villager) != null)
		{
			return super.shouldContinueExecuting();
		}
		if(this.theDebugVar)
			Log.info("we failed the test");
		return false;
	}
    
	@Override
    protected BlockPos getObjectiveBlock() { return VilMethods.getCommBlockPos((EntityVillager) this.entityHost); }
	
	@Override
	protected void resetObjective() 
	{
		VilMethods.setCommBlockPos((EntityVillager) this.entityHost, null);
		VilMethods.setTroopFaring((EntityVillager) this.entityHost, TroopCommands.NONE);
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
