package com.joshycode.improvedmobs.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.util.PositionUtil;
import com.joshycode.improvedmobs.util.VilAttributes;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class VillagerAIAttackMelee extends EntityAIAttackMelee {

	Random rand;
	Path path;
	double speedToTarget;
	
	public VillagerAIAttackMelee(EntityCreature creature, double speedIn, boolean useLongMemory) {
		super(creature, speedIn, useLongMemory);
		this.rand = new Random();
		this.speedToTarget = speedIn;
	}
	
	@Override
	public boolean shouldExecute() {
		if(CapabilityHandler.getCommBlockPos((EntityVillager) this.attacker) != null || PositionUtil.isOutsideHomeDist((EntityVillager) this.attacker)
				|| CapabilityHandler.isReturning((EntityVillager) this.attacker) || CapabilityHandler.getMovingIndoors((EntityVillager) this.attacker)) {
    		return false;
    	}
		String s = this.attacker.getHeldItemMainhand().getUnlocalizedName();
		for(String g : ConfigHandlerVil.configuredGuns.keySet()) {
			if(s.equals(g)) {
				return false;
			}
		}
    	return super.shouldExecute();
    }
	
	@Override
	public void updateTask()
    {
       super.updateTask();
	       this.path = this.attacker.getNavigator().getPath();
	       if(this.path != null && CapabilityHandler.getGuardBlockPos((EntityVillager) this.attacker) != null && 
	    		   this.path.getFinalPathPoint().distanceToSquared(CapabilityHandler.guardBlockAsPP((EntityVillager) this.attacker)) > CommonProxy.MAX_GUARD_DIST - 31) {
	    	   this.truncatePath(this.path, CapabilityHandler.getGuardBlockPos((EntityVillager) this.attacker));
	       }
	       this.attacker.getNavigator().setPath(this.path, this.speedToTarget);
    }

	@Override
	public boolean shouldContinueExecuting() {
		if(CapabilityHandler.getCommBlockPos((EntityVillager) this.attacker) != null || PositionUtil.isOutsideHomeDist((EntityVillager) this.attacker)
				|| CapabilityHandler.isReturning((EntityVillager) this.attacker) || CapabilityHandler.getMovingIndoors((EntityVillager) this.attacker)) {
    		return false;
    	}
		return super.shouldContinueExecuting();
	}
	
	private boolean attackEntityAsVillager(EntityLivingBase entityIn) {
		float f = (float)this.attacker.getEntityAttribute(VilAttributes.VIL_DAMAGE).getAttributeValue();
        int i = 0;
        
        ItemStack itemstack = this.attacker.getHeldItemMainhand();
        
        List<AttributeModifier> l = new ArrayList(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
        System.out.println(l.toString());
        try {
            float f1 = (float) l.get(0).getAmount();
        	System.out.println(f1);
        	if(f1 >= 1.5F)	
        		f1 *= ConfigHandlerVil.villagerDeBuffMelee;
        	f += f1;
        } catch (Throwable t) {
        	System.err.println("Warning! no damage attribute for Item; " + itemstack.getDisplayName());
        }
        f += EnchantmentHelper.getModifierForCreature(this.attacker.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
        i += EnchantmentHelper.getKnockbackModifier(this.attacker);
        
        System.out.println(f);
        
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this.attacker), f);

        if(!itemstack.isEmpty()) {
        	EntityPlayer playerEnt = null;
        	UUID player = getPlayerId();
    		if(player != null) {
    			playerEnt = this.attacker.world.getPlayerEntityByUUID(player);
    		}
        	if(playerEnt != null) {
        		System.out.println("VillagerAIAttackMelee player is; " + playerEnt.getName());
        		itemstack.hitEntity(entityIn, playerEnt);
        	} else {
        		itemstack.getItem().hitEntity(itemstack, entityIn, this.attacker);
        	}
        }
        if (flag)
        {
            if (i > 0 && entityIn instanceof EntityLivingBase)
            {
                ((EntityLivingBase)entityIn).knockBack(this.attacker, (float)i * 0.5F, (double)MathHelper.sin(this.attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.attacker.rotationYaw * 0.017453292F)));
                this.attacker.motionX *= 0.6D;
                this.attacker.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this.attacker);

            if (j > 0)
            {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, this.attacker) && itemstack1.getItem().isShield(itemstack1, entityplayer))
                {
                    float f2 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this.attacker) * 0.05F;

                    if (this.rand.nextFloat() < f2)
                    {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        this.attacker.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }

            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) entityIn, this.attacker);
        }

        return flag;
	}
	
	protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_)
    {
        double d0 = this.getAttackReachSqr(p_190102_1_);

        if (p_190102_2_ <= d0 && this.attackTick <= 0)
        {
            this.attackTick = 20;
            this.attacker.swingArm(EnumHand.MAIN_HAND);
            attackEntityAsVillager(p_190102_1_);
        }
    }

	private void truncatePath(Path p, BlockPos pos) {
		for(int i = 0; i < p.getCurrentPathLength(); i++) {
			if(p.getPathPointFromIndex(i).distanceToSquared(new PathPoint(pos.getX(), pos.getY(), pos.getZ())) > 
					CommonProxy.MAX_GUARD_DIST - 31 /* 2 x 2*/) {
				p.setCurrentPathLength(i);
			}
		}
	}
	
	@Nullable
	private UUID getPlayerId() {
		try {
			return this.attacker.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
		} catch (NullPointerException e) {}
		return null;
	}

	@Override
	public boolean isInterruptible() {
		return true;
	}
	
	@Override
	public void resetTask() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative()))
        {
            this.attacker.setAttackTarget((EntityLivingBase)null);
        }

        this.attacker.getNavigator().clearPath();
    }
}
