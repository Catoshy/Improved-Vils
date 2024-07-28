package com.joshycode.improvedvils.entity.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class VillagerAIAttackNearestTarget<T extends EntityLivingBase> extends VillagerAITarget<T> {

	protected final Class<T> targetClass;
    protected final EntityAINearestAttackableTarget.Sorter sorter;
    protected final Predicate <? super T > targetEntitySelector;
    protected final int targetDistance;
    protected T targetEntity;

    public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, int targetDistance)
    {
        this(creature, classTarget, checkSight, targetDistance, (Predicate<T>)null);
    }

    public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, int targetDistance, @Nullable final Predicate <? super T > targetSelector)
    {
        super(creature, checkSight,  false);
        this.targetClass = classTarget;
        this.targetDistance = targetDistance;
        this.sorter = new EntityAINearestAttackableTarget.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
            @Override
			public boolean apply(@Nullable T p_apply_1_)
            {
                if (p_apply_1_ == null)
                {
                    return false;
                }
                else if (targetSelector != null && !targetSelector.apply(p_apply_1_))
                {
                    return false;
                }
                else
                {													
                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : EntityAITarget.isSuitableTarget(creature, p_apply_1_, false, checkSight);
                }
            }
        };
    }

	@Override
	public boolean shouldExecute()
	{
		if(!super.shouldExecute())
			return false;
		if(this.taskOwner.getRevengeTarget() != null && this.taskOwner.ticksExisted - this.taskOwner.getRevengeTimer() < 100 && !this.taskOwner.getRevengeTarget().isDead)
			return false;
		
		List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.targetDistance), this.targetEntitySelector);
        T targetEntity;

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            Collections.sort(list, this.sorter);
            targetEntity = list.get(0);
        }
		if(this.taskOwner.getHeldItemMainhand().isEmpty())
		{
			return false;
		}

		if(isVillagerInside())
		{
			PathNavigateGround pathNav = ((PathNavigateGround) this.taskOwner.getNavigator());
			pathNav.setBreakDoors(false);

			if(pathNav.getPathToEntityLiving(targetEntity) != null)
			{
				pathNav.setBreakDoors(true);
				this.targetEntity = targetEntity;
				return true;
			}
		}
		else
		{
			this.targetEntity = targetEntity;
			return true;
		}
		return false;
	}

    private boolean isVillagerInside() {
		if(!VilMethods.getDuty((EntityVillager) this.taskOwner))
			if((!this.taskOwner.world.isDaytime() || (this.taskOwner.world.isRaining() && !this.taskOwner.world.getBiome(new BlockPos(this.taskOwner)).canRain())))
					if(this.taskOwner.getNavigator().noPath())
						return true;
		return false;
	}

	@Override
	public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

	private AxisAlignedBB getTargetableArea(double targetDistance)
	{
		return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if(!super.shouldExecute())
			return false;

		return super.shouldContinueExecuting();
	}

	@Override
	public void updateTask()
	{
		EntityLivingBase target = this.taskOwner.getAttackTarget();

		if(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner) != null && target != null)
		{
			double dist = target.getDistanceSq(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
			if(dist > CommonProxy.MAX_GUARD_DIST - 31)
			{
    			List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
    			Collections.sort(list, new VillagerAIAttackNearestTarget.Sorter(taskOwner, VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner)));
    			T targetEntity = list.size() > 0 ? list.get(0) : null;

    			if(targetEntity != null && !targetEntity.equals(this.target))
    			{
    				this.target = targetEntity;
    				this.taskOwner.setAttackTarget(this.target);
    			}
    		}
    	}
	}
	
	public static class Sorter implements Comparator<Entity> {
        private final Entity entity;
        private final BlockPos pos;

        public Sorter(Entity entityIn, BlockPos gPos)
        {
            this.entity = entityIn;
            this.pos = gPos;
        }

        @Override
		public int compare(Entity p_compare_1_, Entity p_compare_2_)
        {
        	boolean flag1 = p_compare_1_.getDistanceSq(this.pos) > CommonProxy.MAX_GUARD_DIST - 31;
        	boolean flag2 = p_compare_2_.getDistanceSq(this.pos) > CommonProxy.MAX_GUARD_DIST - 31;

        	if(flag1 != flag2) {
        		if(flag1) {
        			return 1;
        		} else {
        			return -1;
        		}
        	}

            double d0 = this.entity.getDistanceSq(p_compare_1_);
            double d1 = this.entity.getDistanceSq(p_compare_2_);

            if (d0 < d1)
            {
                return -1;
            }
            else
            {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}
