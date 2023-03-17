package com.joshycode.improvedmobs.entity.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;

import com.joshycode.improvedmobs.capabilities.entity.IVilPlayerIdCapability;
import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import com.joshycode.improvedmobs.handler.VillagerCapabilityHandler;
import com.joshycode.improvedmobs.util.VilAttributes;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

public class VillagerAIAttackMelee extends EntityAIAttackMelee {

	Random rand;
	
	public VillagerAIAttackMelee(EntityCreature creature, double speedIn, boolean useLongMemory) {
		super(creature, speedIn, useLongMemory);
		this.rand = new Random();
	}
	
	public void setPlayer(UUID playerId) {
		if(this.attacker != null) {
			this.attacker.getCapability(VillagerCapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerId(playerId);; 
		} else {
			throw new IllegalArgumentException("attacker is null");
		}
	}
	
	public UUID getPlayer() {
		IVilPlayerIdCapability cap = this.attacker.getCapability(VillagerCapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(cap == null)
			throw new IllegalArgumentException("no entity capability for villager - IVilPlayerIdCapability is null");
		return cap.getPlayerId();
	}
	
	@Override
	public boolean shouldExecute() {
		String s = this.attacker.getHeldItemMainhand().getUnlocalizedName();
		for(String g : ConfigHandlerVil.configuredGuns.keySet()) {
			if(s.equals(g)) {
				return false;
			}
		}
		return super.shouldExecute();
    }
	
	@Override
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

	private boolean attackEntityAsVillager(EntityLivingBase entityIn) {
		float f = (float)this.attacker.getEntityAttribute(VilAttributes.VIL_DAMAGE).getAttributeValue();
        int i = 0;
        
        ItemStack itemstack = this.attacker.getHeldItemMainhand();
        
        List<AttributeModifier> l = new ArrayList(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
        System.out.println(l.toString());
        try {
            float f1 = (float) l.get(0).getAmount();
        	System.out.println(f1);
        	if(f1 >= 1.5F)	//TODO make config var instead of .75
        		f1 *= .75;
        	f += f1;
        } catch (Throwable t) {
        	System.err.println("Warning! no damage attribute for Item; " + itemstack.getDisplayName());
        }
        f += EnchantmentHelper.getModifierForCreature(this.attacker.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
        i += EnchantmentHelper.getKnockbackModifier(this.attacker);
        
        System.out.println(f);
        
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this.attacker), f);

        if(!itemstack.isEmpty()) {
        	EntityPlayer player = entityIn.world.getPlayerEntityByUUID(getPlayer());
        	if(player != null) {
        		System.out.println("VillagerAIAttackMelee player is; " + player.getName());
        		itemstack.hitEntity(entityIn, player);
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
}
