package com.joshycode.improvedmobs.entity.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.VilCapabilityMethods;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class VillagerAIAttackNearestTarget<T extends EntityLivingBase> extends VillagerAITarget<T> {
	
	protected final Class<T> targetClass;
    private final int targetChance;
    protected final EntityAINearestAttackableTarget.Sorter sorter;
    protected final Predicate <? super T > targetEntitySelector;
    protected T targetEntity;

    public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight)
    {
        this(creature, classTarget, checkSight, false);
    }

    public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby)
    {
        this(creature, classTarget, 4, checkSight, onlyNearby, (Predicate)null);
    }

    public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super T > targetSelector)
    {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.sorter = new EntityAINearestAttackableTarget.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
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
                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : VillagerAIAttackNearestTarget.this.isSuitableTarget(p_apply_1_, false);
                }
            }
        };
    }

	@Override
	public boolean shouldExecute() {
		if(!super.shouldExecute())
			return false;
		List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
        T targetEntity;
        
        if (list.isEmpty()) {
            return false;
            
        } else {
            Collections.sort(list, this.sorter);
            targetEntity = list.get(0);
        }
		if(this.taskOwner.getHeldItemMainhand().getItem() == Items.AIR) {
			return false;
		}
		boolean flag2 = InventoryUtil.doesInventoryHaveItem(((EntityVillager) this.taskOwner).getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) > 0;
		
		//TODO player teams attack targeting
		
		if(!flag2 && ((!this.taskOwner.world.isDaytime() || this.taskOwner.world.isRaining() && !this.taskOwner.world.getBiome(new BlockPos(this.taskOwner)).canRain()) && this.taskOwner.world.provider.hasSkyLight()) && this.taskOwner.getNavigator().noPath()) {
			PathNavigateGround pathNav = ((PathNavigateGround) this.taskOwner.getNavigator());
			pathNav.setBreakDoors(false);
			
			if(pathNav.getPathToEntityLiving(targetEntity) != null) {
				pathNav.setBreakDoors(true);
				this.targetEntity = targetEntity;
				return true;
				
			}	
		} else {
			this.targetEntity = targetEntity;
			return true;
		}
		return false;
	}
	
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }
	
	private AxisAlignedBB getTargetableArea(double targetDistance) {
		return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
	}

	@Override
	public boolean shouldContinueExecuting() {
		if(!super.shouldExecute())
			return false;
		EntityLivingBase target = this.taskOwner.getAttackTarget();
		if(VilCapabilityMethods.getGuardBlockPos((EntityVillager) this.taskOwner) != null && target != null) {
			double dist = target.getDistanceSq(VilCapabilityMethods.getGuardBlockPos((EntityVillager) this.taskOwner));
			if(dist > CommonProxy.MAX_GUARD_DIST - 31) {
    			List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
    			Collections.sort(list, new VillagerAIAttackNearestTarget.Sorter(taskOwner, VilCapabilityMethods.getGuardBlockPos((EntityVillager) this.taskOwner)));
    			T targetEntity = list.size() > 0 ? list.get(0) : null;
    			if(targetEntity != null && !targetEntity.equals(this.target)) {
    				this.target = targetEntity;
    				this.taskOwner.setAttackTarget(this.target);
    			}
    		}
    	}
		return super.shouldContinueExecuting();
	}

	public static class Sorter implements Comparator<Entity>
    {
        private final Entity entity;
        private final BlockPos pos;

        public Sorter(Entity entityIn, BlockPos gPos)
        {
            this.entity = entityIn;
            this.pos = gPos;
        }

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
