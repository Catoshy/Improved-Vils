package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.handler.ConfigHandlerVil;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class VillagerAIMate extends EntityAIBase
{
    private final EntityVillager villager;
    private EntityVillager mate;
    private final World world;
    private int matingTimeout;
    Village village;

    public VillagerAIMate(EntityVillager villagerIn)
    {
        this.villager = villagerIn;
        this.world = villagerIn.world;
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
        if (this.villager.getGrowingAge() != 0)
        {
            return false;
        }
        else if (this.villager.getRNG().nextInt(500) != 0)
        {
            return false;
        } 
        else if (InventoryUtil.doesInventoryHaveItem(this.villager.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0) 
        {
        	return false;
        }
        else
        {
            this.village = this.world.getVillageCollection().getNearestVillage(new BlockPos(this.villager), 0);

            if (this.village == null)
            {
                return false;
            }
            else if (this.checkSufficientDoorsPresentForNewVillager() && this.villager.getIsWillingToMate(true) && this.hasEnoughSaturationToBreed())
            {
                Entity entity = this.world.findNearestEntityWithinAABB(EntityVillager.class, this.villager.getEntityBoundingBox().grow(16.0D, 3.0D, 16.0D), this.villager);

                if (entity == null)
                {
                    return false;
                }
                else
                {
                    this.mate = (EntityVillager)entity;
                    return this.mate.getGrowingAge() == 0 && this.mate.getIsWillingToMate(true);
                }
            }
            else
            {
                return false;
            }
        }
    }

    public void startExecuting()
    {
        this.matingTimeout = 300;
        this.villager.setMating(true);
    }

    public void resetTask()
    {
        this.village = null;
        this.mate = null;
        this.villager.setMating(false);
    }

    public boolean shouldContinueExecuting()
    {
        return this.matingTimeout >= 0 && this.checkSufficientDoorsPresentForNewVillager() && this.villager.getGrowingAge() == 0 && this.villager.getIsWillingToMate(false);
    }

    public void updateTask()
    {
        --this.matingTimeout;
        this.villager.getLookHelper().setLookPositionWithEntity(this.mate, 10.0F, 30.0F);

        if (this.villager.getDistanceSq(this.mate) > 2.25D)
        {
            this.villager.getNavigator().tryMoveToEntityLiving(this.mate, 0.25D);
        }
        else if (this.matingTimeout == 0 && this.mate.isMating())
        {
            this.giveBirth();
        }

        if (this.villager.getRNG().nextInt(35) == 0)
        {
            this.world.setEntityState(this.villager, (byte)12);
        }
    }
    
    private boolean hasEnoughSaturationToBreed()
    {
    	return InventoryUtil.getFoodSaturation(this.villager.getVillagerInventory()) > 4.8f;
    }
    
    private boolean consumeBreedingMaterials()
    {
    	float runningTotal = 4.8f;
    	
    	for(int i = 0; i < this.villager.getVillagerInventory().getSizeInventory(); i++) 
    	{
    		ItemStack stack = this.villager.getVillagerInventory().getStackInSlot(i);
    		
    		if(!(stack.getItem() instanceof ItemFood))
    			continue;
    		
    		if(runningTotal > 0) 
    		{
    			float saturation = ((ItemFood)stack.getItem()).getSaturationModifier(stack);
    			if(stack.getCount() > Math.ceil(runningTotal / saturation)) 
    			{
    				int neededCount = (int) Math.ceil(runningTotal / saturation);
    				this.villager.getVillagerInventory().decrStackSize(i, neededCount);
    				runningTotal -= neededCount * saturation;
    			}
    			else 
    			{
    				runningTotal -= stack.getCount() * saturation;
    				this.villager.getVillagerInventory().decrStackSize(i, stack.getCount());
    			}
    		}
    	}
    	return !(runningTotal > 0);
    }

    private boolean checkSufficientDoorsPresentForNewVillager()
    {
        if (!this.village.isMatingSeason())
        {
            return false;
        }
        else
        {
            int i = (int)((double)((float)this.village.getNumVillageDoors()) * ConfigHandlerVil.villagersPerDoor);
            return this.village.getNumVillagers() < i;
        }
    }

    private void giveBirth()
    {
        net.minecraft.entity.EntityAgeable entityvillager = this.villager.createChild(this.mate);
        this.mate.setGrowingAge(2000);
        this.villager.setGrowingAge(2000);
        this.mate.setIsWillingToMate(false);
        this.villager.setIsWillingToMate(false);

        final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(villager, mate, entityvillager);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event) || event.getChild() == null || !consumeBreedingMaterials()) { return; }
        entityvillager = event.getChild();
        entityvillager.setGrowingAge(-6000);
        entityvillager.setLocationAndAngles(this.villager.posX, this.villager.posY, this.villager.posZ, 0.0F, 0.0F);
        this.world.spawnEntity(entityvillager);
        this.world.setEntityState(entityvillager, (byte)12);
    }
}
