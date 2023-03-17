package com.joshycode.improvedmobs.entity.ai;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VillagerAIAttackNearestTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {

	public VillagerAIAttackNearestTarget(EntityCreature creature, Class<T> classTarget) {
		super(creature, classTarget, true);
		}

	@Override
	public boolean shouldExecute() {
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
		boolean flag = false;
		
		//TODO player teams attack targeting
		
		for(int i  = 0; i < ((EntityVillager) this.taskOwner).getVillagerInventory().getSizeInventory(); i++) {
			//TODO make writ-of-draft item
			flag |= ((EntityVillager) this.taskOwner).getVillagerInventory().getStackInSlot(i).getItem() == Items.STICK;
			
		}
		if(!flag && ((!this.taskOwner.world.isDaytime() || this.taskOwner.world.isRaining() && !this.taskOwner.world.getBiome(new BlockPos(this.taskOwner)).canRain()) && this.taskOwner.world.provider.hasSkyLight()) && this.taskOwner.getNavigator().noPath()) {
			Vec3d p = targetEntity.getPositionVector();
			PathNavigateGround pathNav = ((PathNavigateGround) this.taskOwner.getNavigator());
			pathNav.setBreakDoors(false);
			
			if(pathNav.tryMoveToXYZ(p.x, p.y, p.z, 1.0D)) {
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
}
