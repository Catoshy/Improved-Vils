package com.joshycode.improvedvils.entity.ai;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.Vec3d;

public class VillagerAIFollow extends EntityAIBase {
	private final EntityVillager villager;
    private final Predicate<EntityPlayer> followPredicate;
    private EntityPlayer followingPlayer;
    private final double speedModifier;
    private final PathNavigate navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final double areaSize;

    public VillagerAIFollow(final EntityVillager entityIn, double speedIn, float stopDist)
    {
        this.villager = entityIn;
        this.followPredicate = new VilFollowPredicate(entityIn);
        this.speedModifier = speedIn;
        this.navigation = entityIn.getNavigator();
        this.stopDistance = stopDist;
        this.areaSize = entityIn.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
    	if(VilMethods.getHungry(this.villager))
    		return false;
    	if(!VilMethods.getFollowing(this.villager))
    		return false;
        List<EntityPlayer> list = this.villager.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.villager.getEntityBoundingBox().grow((double)this.areaSize), this.followPredicate);

        if (!list.isEmpty())
        {
            for (EntityPlayer entityliving : list)
            {
                if (!entityliving.isInvisible())
                {
                    this.followingPlayer = entityliving;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldContinueExecuting()
    {
        return this.followingPlayer != null && !this.navigation.noPath() && this.villager.getDistanceSq(this.followingPlayer) > (double)(this.stopDistance * this.stopDistance) && !VilMethods.getHungry(this.villager);
    }

    public void startExecuting()
    {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.villager.getPathPriority(PathNodeType.WATER);
        this.villager.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    public void resetTask()
    {
        this.followingPlayer = null;
        this.navigation.clearPath();
        this.villager.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    public void updateTask()
    {
        if (this.followingPlayer != null && !this.villager.getLeashed())
        {
            this.villager.getLookHelper().setLookPositionWithEntity(this.followingPlayer, 10.0F, (float)this.villager.getVerticalFaceSpeed());

            if (--this.timeToRecalcPath <= 0)
            {
                this.timeToRecalcPath = 10;
                double d0 = this.villager.posX - this.followingPlayer.posX;
                double d1 = this.villager.posY - this.followingPlayer.posY;
                double d2 = this.villager.posZ - this.followingPlayer.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > (double)(this.stopDistance * this.stopDistance))
                {
                    this.navigation.tryMoveToEntityLiving(this.followingPlayer, this.speedModifier);
                }
                else
                {
                    this.navigation.clearPath();
                    Vec3d lookVector = this.followingPlayer.getLookVec();

                    if (d3 <= (double)this.stopDistance || lookVector.x == this.villager.posX && lookVector.y == this.villager.posY && lookVector.z == this.villager.posZ)
                    {
                        double d4 = this.followingPlayer.posX - this.villager.posX;
                        double d5 = this.followingPlayer.posZ - this.villager.posZ;
                        this.navigation.tryMoveToXYZ(this.villager.posX - d4, this.villager.posY, this.villager.posZ - d5, this.speedModifier);
                    }
                }
            }
        }
    }
    
    protected class VilFollowPredicate<T extends EntityPlayer> implements Predicate<T> {

    	EntityVillager villager;
    	
    	protected VilFollowPredicate(EntityVillager villager) {
    		this.villager = villager;
    	}
    	
		@Override
		public boolean apply(T input) {
			UUID id = UUID.randomUUID();
			try {
				id = this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
			} catch (NullPointerException e) {}
			if(input.getUniqueID().equals(id)) {
				return true;
			}
			return false;
		}
    	
    }
}
