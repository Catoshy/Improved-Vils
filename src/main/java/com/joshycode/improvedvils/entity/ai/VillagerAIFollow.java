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
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.Vec3d;

public class VillagerAIFollow extends EntityAIBase {
	private final EntityVillager villager;
    private final Predicate<EntityPlayer> followPredicate;
    private EntityPlayer followingPlayer;
    private final double speedModifier;
    private final PathNavigate navigator;
    private int timeToRecalcPath;
    private final float stopDistance;
	private final float startDistance;
    
    private float oldWaterCost;
    private final double areaSize;

	public VillagerAIFollow(final EntityVillager entityIn, double speedIn, float stopDist, float startDist)
    {
        this.villager = entityIn;
        this.followPredicate = new VilFollowPredicate<>(entityIn);
        this.speedModifier = speedIn;
        this.navigator = entityIn.getNavigator();
        this.stopDistance = stopDist;
		this.startDistance = startDist;
        this.areaSize = entityIn.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
        this.setMutexBits(3);
    }

    @Override
	public boolean shouldExecute()
    {
    	if(VilMethods.getHungry(this.villager) || !VilMethods.getFollowing(this.villager) || !VilMethods.getDuty(this.villager))
    		return false;
        List<EntityPlayer> list = this.villager.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.villager.getEntityBoundingBox().grow(this.areaSize), this.followPredicate);

        if (!list.isEmpty())
        {
            for (EntityPlayer entityliving : list)
            {
                if (!entityliving.isInvisible() && !weaponsAndDistance(entityliving))
                {
                    this.followingPlayer = entityliving;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean weaponsAndDistance(EntityPlayer player) 
    {
		return !this.villager.getHeldItemMainhand().isEmpty() && this.villager.getDistanceSq(player) < this.startDistance * this.startDistance && this.villager.getAttackTarget() != null;
	}

	@Override
	public boolean shouldContinueExecuting()
    {
        return this.followingPlayer != null && !this.weaponsAndDistance(this.followingPlayer) && !this.navigator.noPath() && this.villager.getDistanceSq(this.followingPlayer) > this.stopDistance * this.stopDistance && !VilMethods.getHungry(this.villager);
    }

    @Override
	public void startExecuting()
    {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.villager.getPathPriority(PathNodeType.WATER);
        ((PathNavigateGround) this.navigator).setBreakDoors(true);
        this.villager.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
	public void resetTask()
    {
        this.followingPlayer = null;
        this.navigator.clearPath();
        ((PathNavigateGround) this.navigator).setBreakDoors(false);
        this.villager.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    @Override
	public void updateTask()
    {
        if (this.followingPlayer != null && !this.villager.getLeashed())
        {
            this.villager.getLookHelper().setLookPositionWithEntity(this.followingPlayer, 10.0F, this.villager.getVerticalFaceSpeed());

            if (--this.timeToRecalcPath <= 0)
            {
                this.timeToRecalcPath = 10;
                double d0 = this.villager.posX - this.followingPlayer.posX;
                double d1 = this.villager.posY - this.followingPlayer.posY;
                double d2 = this.villager.posZ - this.followingPlayer.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > this.stopDistance * this.stopDistance)
                {
                    this.navigator.tryMoveToEntityLiving(this.followingPlayer, this.speedModifier);
                }
                else
                {
                    this.navigator.clearPath();
                    Vec3d lookVector = this.followingPlayer.getLookVec();

                    if (d3 <= this.stopDistance || lookVector.x == this.villager.posX && lookVector.y == this.villager.posY && lookVector.z == this.villager.posZ)
                    {
                        double d4 = this.followingPlayer.posX - this.villager.posX;
                        double d5 = this.followingPlayer.posZ - this.villager.posZ;
                        this.navigator.tryMoveToXYZ(this.villager.posX - d4, this.villager.posY, this.villager.posZ - d5, this.speedModifier);
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
