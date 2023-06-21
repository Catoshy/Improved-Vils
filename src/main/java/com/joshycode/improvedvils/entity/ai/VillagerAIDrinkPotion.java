package com.joshycode.improvedvils.entity.ai;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHealth;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;

public class VillagerAIDrinkPotion extends EntityAIBase {

	EntityVillager villager;
	ItemStack potionStack;
	ItemStack returnItem;
	private int slot;
	private int tickCounter;
	private int potionUseTimer;

	protected static final UUID MODIFIER_UUID = UUID.fromString("bca14a1e-cfcf-11ed-afa1-0242ac120002");
	private static final AttributeModifier MODIFIER = (new AttributeModifier(MODIFIER_UUID, "Drinking speed penalty", -0.25D, 0)).setSaved(false);

	public VillagerAIDrinkPotion(EntityVillager villager)
	{
		super();
		this.villager = villager;
		this.tickCounter = ThreadLocalRandom.current().nextInt(40, 80 + 1);
	}

	@Override
	public boolean shouldExecute()
	{
		if(!isDrinking() && this.villager.getLastAttackedEntityTime() - this.villager.ticksExisted <= 40)
		{
			if(this.tickCounter % 5 != 0)
			{
				this.tickCounter--;
				return false;
			}
		}
		else if(this.tickCounter > 0)
		{
			this.tickCounter--;
			return false;
		}
		if(isDrinking() && this.villager.getHeldItemMainhand().getItem().equals(Items.POTIONITEM))
		{
			return true;
		}
		else if((this.villager.getHealth() / this.villager.getMaxHealth()) < .33f && !this.villager.getVillagerInventory().isEmpty())
		{
			for(int i = 0; i < this.villager.getVillagerInventory().getSizeInventory(); i++)
			{
				ItemStack stack = this.villager.getVillagerInventory().getStackInSlot(i);
				for(PotionEffect effect : PotionUtils.getEffectsFromStack(stack))
				{
					if(effect.getPotion() instanceof PotionHealth)
					{
						this.potionStack = stack;
						this.slot = i;
						return true;
					}
				}
			}
		}
		this.tickCounter = ThreadLocalRandom.current().nextInt(40, 80 + 1);
		return false;
	}

	@Override
	public void startExecuting()
	{
		setDrinking(true);
		if(!this.villager.getHeldItemMainhand().getItem().equals(Items.POTIONITEM))
		{
			this.returnItem =  this.villager.getHeldItemMainhand();
			this.villager.setHeldItem(EnumHand.MAIN_HAND, this.potionStack);
			this.potionUseTimer = this.villager.getHeldItemMainhand().getMaxItemUseDuration();
			this.villager.getVillagerInventory().setInventorySlotContents(this.slot, this.returnItem);
		}
		IAttributeInstance iattributeinstance = this.villager.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        iattributeinstance.removeModifier(MODIFIER);
        iattributeinstance.applyModifier(MODIFIER);
		this.villager.world.playSound((EntityPlayer)null, this.villager.posX, this.villager.posY, this.villager.posZ, SoundEvents.ENTITY_WITCH_DRINK, this.villager.getSoundCategory(), 1.0F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F);
	}

	@Override
	public void updateTask()
	{
		this.potionUseTimer--;
		if(this.potionUseTimer > 0)
		{
			return;
		}
		List<PotionEffect> list = PotionUtils.getEffectsFromStack(this.potionStack);
		if (list != null)
		{
			for (PotionEffect potioneffect : list)
			{
            	 this.villager.addPotionEffect(new PotionEffect(potioneffect));
            }
		}
		this.potionStack = new ItemStack(Items.GLASS_BOTTLE);
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return !this.villager.isDead && this.villager.getHeldItemMainhand().equals(potionStack) && this.potionUseTimer > 0;
	}

	@Override
	public void resetTask()
	{
		this.tickCounter = ThreadLocalRandom.current().nextInt(40, 80 + 1);
		this.villager.setHeldItem(EnumHand.MAIN_HAND, this.returnItem);
		this.villager.getVillagerInventory().setInventorySlotContents(slot, this.potionStack);
		this.potionUseTimer = 0;
		this.potionStack = null;
		this.returnItem = null;
		setDrinking(false);
		this.villager.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER);
	}

	private void setDrinking(boolean b)
	{
		try
		{
	        this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setDrinking(b);
		} catch(NullPointerException e) {}
	}

	private boolean isDrinking()
	{
		try
		{
	       return this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isDrinking();
		} catch(NullPointerException e) {}
		return false;
	}
}
