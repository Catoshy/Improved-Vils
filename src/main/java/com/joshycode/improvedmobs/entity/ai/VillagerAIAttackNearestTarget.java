package com.joshycode.improvedmobs.entity.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;
import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.InventoryUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;

public class VillagerAIAttackNearestTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
	
	public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget, double speed) {
		super(creature, classTarget, 10, true, false, (Predicate)null);
	}

	@Override
	public boolean shouldExecute() {
		BlockPos pos = getGuardPos();
		
    	if(pos != null) {
    		double hostDist = this.taskOwner.getDistanceSq(getGuardPos());
    		if(isReturning()) {
    			return false;
    		}
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) {
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		}
    	}
    	
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
	
	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase target = this.taskOwner.getAttackTarget();
		if(getGuardPos() != null) {
    		if(isReturning()) {
    			return false;
    		}
    		double hostDist = this.taskOwner.getDistanceSq(getGuardPos());
    		double dist = target.getDistanceSq(getGuardPos());
    		if(hostDist > CommonProxy.MAX_GUARD_DIST - 1) {
    			this.taskOwner.setAttackTarget(null);
    			return false;
    		} else if(dist > CommonProxy.MAX_GUARD_DIST - 31) {
    			List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
    			Collections.sort(list, new VillagerAIAttackNearestTarget.Sorter(taskOwner, getGuardPos()));
    			T targetEntity = list.get(0);
    			if(!targetEntity.equals(this.targetEntity)) {
    				this.targetEntity = targetEntity;
    				this.taskOwner.setAttackTarget(this.targetEntity);
    			}
    		}
    	}
		return super.shouldContinueExecuting();
	}

	private BlockPos getGuardPos() {
		try {
			return this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getBlockPos();
		} catch (NullPointerException e) {}
		return null;
	}
	
	private boolean isReturning() {
		try {
			return this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isReturning();
		} catch (NullPointerException e) {}
		return false;
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
