package com.joshycode.improvedmobs.entity.ai;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.handler.CapabilityHandler;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class VillagerAIHurtByTarget<T extends EntityLivingBase> extends VillagerAITarget<T> {

	private final boolean callsForHelp;
	private int revengeTimerOld;

	public VillagerAIHurtByTarget(EntityCreature creature, boolean callsForHelp) {
		super(creature, false, false);
		this.callsForHelp = callsForHelp;
		this.revengeTimerOld = 0;
	}

	@Override
	public boolean shouldExecute() {
		if(!super.shouldExecute())
			return false;
		int i = this.taskOwner.getRevengeTimer();
        EntityLivingBase entitylivingbase = this.taskOwner.getRevengeTarget();
        return i != this.revengeTimerOld && entitylivingbase != null && this.isSuitableTarget(entitylivingbase, false);
	}
	
	 public void startExecuting()
	    {
	        this.taskOwner.setAttackTarget(this.taskOwner.getRevengeTarget());
	        this.target = this.taskOwner.getAttackTarget();
	        this.revengeTimerOld = this.taskOwner.getRevengeTimer();
	        this.unseenMemoryTicks = 300;

	        if (this.callsForHelp)
	        {
	            this.alertOthers();
	        }

	        super.startExecuting();
	    }

	protected void alertOthers() {
		double d0 = this.getTargetDistance();

        for (EntityCreature entitycreature : this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).grow(d0, 10.0D, d0)))
        {
            if (this.taskOwner != entitycreature && entitycreature.getAttackTarget() == null && (!(this.taskOwner instanceof EntityTameable) || ((EntityTameable)this.taskOwner).getOwner() == ((EntityTameable)entitycreature).getOwner()) && !entitycreature.isOnSameTeam(this.taskOwner.getRevengeTarget()))
            {
                boolean flag = true;

                if(entitycreature instanceof EntityVillager) {
                	flag = false;
                	if(CapabilityHandler.getGuardBlockPos((EntityVillager) entitycreature) != null && entitycreature.getDistanceSq(CapabilityHandler.getGuardBlockPos((EntityVillager) entitycreature)) > CommonProxy.MAX_GUARD_DIST) {
                		 flag = true;
                	}
                }	
                if(!flag) {
                    this.setEntityAttackTarget(entitycreature, this.taskOwner.getRevengeTarget());
                }
            }
        }
    }
	
	protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
		System.out.println("isSuitableTarget, CommonProxy.TARGETS = " + CommonProxy.TARGETS.getSuitable_targets().toString());
		if(!super.isSuitableTarget(target, includeInvincibles))
			return false;
		if(target != null) {
			for(Class c : CommonProxy.TARGETS.getSuitable_targets()) {
				if(c.isInstance(target)) {
					System.out.println("isSuitableTarget, return = " + true);
					return true;
				}
			}
		}
		return false;
	}
	
	private void setEntityAttackTarget(EntityCreature entitycreature, EntityLivingBase revengeTarget) {
		entitycreature.setAttackTarget(revengeTarget);
	}
}
