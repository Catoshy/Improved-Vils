package com.joshycode.improvedvils.entity.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class VillagerAIAttackNearestTarget extends VillagerAITarget {

	protected final Set<Targeter> targeters;
    private final Comparator<Entity> sorterOther;
    private final VillagerAIAttackNearestTarget.Sorter sorterGuard;
    protected EntityLivingBase targetEntity;

    public VillagerAIAttackNearestTarget(EntityVillager villager, Map<Class<? extends EntityLivingBase>, Predicate<? super EntityLivingBase>> targetsAndFilters, boolean checkSight, int targetDistance)
    {
        super(villager, checkSight,  false, targetDistance);
        this.targeters = new HashSet<Targeter>();
		for(Entry<Class<? extends EntityLivingBase>, Predicate<? super EntityLivingBase>> targetAndFilter : targetsAndFilters.entrySet())
		{
			this.targeters.add(new Targeter(villager, targetAndFilter.getKey(), checkSight, targetAndFilter.getValue()));
		}
        this.sorterOther = new EntityAINearestAttackableTarget.Sorter(this.taskOwner);
        this.sorterGuard = new VillagerAIAttackNearestTarget.Sorter(this.taskOwner, VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
        this.setMutexBits(1);
    }

	@Override
	public boolean shouldExecute()
	{
		if(!super.shouldExecute() || this.taskOwner.getHeldItemMainhand().isEmpty())
			return false;
		if(this.taskOwner.getRevengeTarget() != null && this.taskOwner.ticksExisted - this.taskOwner.getRevengeTimer() < 100 && !this.taskOwner.getRevengeTarget().isDead)
			return false;
		
		List<EntityLivingBase> list = Lists.newArrayList();
		for(Targeter t : this.targeters)
				list.addAll(this.taskOwner.world.<EntityLivingBase>getEntitiesWithinAABB(t.targetClass, this.getTargetableArea(this.targetDistance), t.targetEntitySelector));
        
		Comparator<Entity> sorter;
		
        if(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner) != null)
        	sorter = this.sorterGuard.setPos(VilMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
        else
        	sorter = this.sorterOther;
		
		EntityLivingBase targetEntity;

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            Collections.sort(list, sorter);
            targetEntity = list.get(0);
        }
        
        if(targetEntity == null)
        	return false;
        
		if(isVillagerInside())
		{
			PathNavigateGround pathNav = ((PathNavigateGround) this.taskOwner.getNavigator());
			pathNav.setBreakDoors(false);

			if(pathNav.getPathToEntityLiving(targetEntity) != null)
			{
				this.targetEntity = targetEntity;
				pathNav.setBreakDoors(true);
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
		if(this.taskOwner.getRNG().nextInt(3) != 0) return;
				
		EntityLivingBase targetEntity = this.taskOwner.getAttackTarget();
		this.shouldExecute();
		
        if(this.targetEntity != targetEntity && this.targetEntity != null)
        	this.taskOwner.setAttackTarget(this.targetEntity);
	}

	private AxisAlignedBB getTargetableArea(double targetDistance)
	{
		return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
	}

	public static class Targeter {
		protected final Class<? extends EntityLivingBase> targetClass; //This field is a "producer" in that it is furnished through methods as a variable to be checked.
		protected final Predicate <? super EntityLivingBase> targetEntitySelector; //This field is a "consumer" in that it is passed through until it gets to the Chunk class where it is fed entities of type T.
		
		public Targeter(EntityVillager villager, Class<? extends EntityLivingBase> class1, boolean checkSight, @Nullable final Predicate<? super EntityLivingBase> predicate)
		{
			this.targetClass = class1;
			this.targetEntitySelector = new Predicate<EntityLivingBase>()
			{
			    @Override
				public boolean apply(@Nullable EntityLivingBase p_apply_1_)
			    {
			        if (p_apply_1_ == null)
			        {
			            return false;
			        }
			        else if (predicate != null && !predicate.apply(p_apply_1_))
			        {
			            return false;
			        }
			        else
			        {													
			            return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : EntityAITarget.isSuitableTarget(villager, p_apply_1_, false, checkSight);
			        }
			    }
			};
		}
	}
	
	public static class Sorter implements Comparator<Entity> {
        private final Entity entity;
        private BlockPos pos;

        public Sorter(Entity entityIn, BlockPos gPos)
        {
            this.entity = entityIn;
            this.pos = gPos;
        }
        
        public Sorter setPos(BlockPos pos) 
        { 
        	this.pos = pos; 
        	return this;
        }

        @Override
		public int compare(Entity p_compare_1_, Entity p_compare_2_)
        {
        	if(p_compare_1_ == null && p_compare_2_ == null)
        	return 0;
        	else if(p_compare_1_ == null)
        	return 1;
        	else if(p_compare_2_ == null)
        	return -1;
        	
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
	
	@Override
	public void resetTask()
	{
		super.resetTask();
		((PathNavigateGround) this.taskOwner.getNavigator()).setBreakDoors(true);
	}
}