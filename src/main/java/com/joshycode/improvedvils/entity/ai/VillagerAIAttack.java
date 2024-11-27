package com.joshycode.improvedvils.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jline.utils.Log;

import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.VilAttributes;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

public abstract class VillagerAIAttack extends EntityAIBase {

	EntityVillager attacker;
	
	public VillagerAIAttack() 
	{
		super();
	}

	private static boolean attackEntityAsVillager(EntityVillager attacker, EntityLivingBase entityIn, boolean unBlocksome) 
	{
		float f = (float)attacker.getEntityAttribute(VilAttributes.VIL_DAMAGE).getAttributeValue();
	    int i = 0;	
	    ItemStack itemstack = attacker.getHeldItemMainhand();
	
	    List<AttributeModifier> l = new ArrayList<AttributeModifier>(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
	    
	    if(!l.isEmpty())
	    {
	        float f1 = 0.0F;
	        for(AttributeModifier mod : l)
	        	f1 += (float) mod.getAmount();
	
	    	if(f1 >= 1.5F)
	    		f1 *= ConfigHandler.villagerDeBuffMelee;
	
	    	f += f1;
	    }
	    
	    f += EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), entityIn.getCreatureAttribute());
	    i += EnchantmentHelper.getKnockbackModifier(attacker);
	    
	    DamageSource attack = unBlocksome ? DamageSource.causeMobDamage(attacker).setDamageBypassesArmor() : DamageSource.causeMobDamage(attacker);
	    
	    boolean flag = entityIn.attackEntityFrom(attack, f);
	
	    if(!itemstack.isEmpty())
	    {
	    	EntityPlayer playerEnt = null;
	    	UUID player = VilMethods.getPlayerId(attacker);
	
			if(player != null)
			{
				playerEnt = attacker.world.getPlayerEntityByUUID(player);
			}
	    	if(playerEnt != null)
	    	{
	    		itemstack.hitEntity(entityIn, playerEnt);
	    	}
	    	else
	    	{
	    		itemstack.getItem().hitEntity(itemstack, entityIn, attacker);
	    	}
	    }
	    if (flag)
	    {
	        if (i > 0 && entityIn instanceof EntityLivingBase)
	        {
	            entityIn.knockBack(attacker, i * 0.5F, MathHelper.sin(attacker.rotationYaw * 0.017453292F), (-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
	            attacker.motionX *= 0.6D;
	            attacker.motionZ *= 0.6D;
	        }
	
	        int j = EnchantmentHelper.getFireAspectModifier(attacker);
	
	        if (j > 0)
	        {
	            entityIn.setFire(j * 4);
	        }
	
	        if (entityIn instanceof EntityPlayer)
	        {
	            EntityPlayer entityplayer = (EntityPlayer)entityIn;
	            ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;
	
	            if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, attacker) && itemstack1.getItem().isShield(itemstack1, entityplayer))
	            {
	                float f2 = 0.25F + EnchantmentHelper.getEfficiencyModifier(attacker) * 0.05F;
	                if (attacker.getRNG().nextFloat() < f2)
	                {
	                    entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
	                    attacker.world.setEntityState(entityplayer, (byte)30);
	                }
	            }
	        }
	        EnchantmentHelper.applyThornEnchantments(entityIn, attacker);
	    }
	
	    return flag;
	}
	
	protected static int getCooldown(EntityVillager attacker)
	{
		int eAttr = (int) (attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
		return (int) (eAttr * ConfigHandler.meleeAttackCooldown);
	}

	protected static boolean checkAndPerformAttack(EntityVillager attacker, EntityLivingBase attackTarget, int attackTick, double p_190102_2_, boolean passesArmour) 
	{
	    double d0 = getAttackReachSqr(attacker, attackTarget);
	
	    if (p_190102_2_ <= d0 && attackTick <= 0 && !VilMethods.isDrinking(attacker))
	    {
	        attacker.setActiveHand(EnumHand.MAIN_HAND);
	        return attackEntityAsVillager(attacker, attackTarget, passesArmour);
	    }
	    return false;
	}

	protected static double getAttackReachSqr(EntityVillager attacker, EntityLivingBase attackTarget) 
	{
	    return (attacker.width + ConfigHandler.attackReach) * (attacker.width + ConfigHandler.attackReach) + attackTarget.width;
	}

}