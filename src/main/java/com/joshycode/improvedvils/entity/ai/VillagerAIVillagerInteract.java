package com.joshycode.improvedvils.entity.ai;

import com.joshycode.improvedvils.capabilities.VilMethods;

import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class VillagerAIVillagerInteract extends EntityAIWatchClosest2
{
    private int interactionDelay;
    private final EntityVillager villager;

    public VillagerAIVillagerInteract(EntityVillager villagerIn)
    {
        super(villagerIn, EntityVillager.class, 3.0F, 0.02F);
        this.villager = villagerIn;
    }

    @Override
	public void startExecuting()
    {
        super.startExecuting();

        if (this.villager.canAbondonItems() && this.closestEntity instanceof EntityVillager && ((EntityVillager)this.closestEntity).wantsMoreFood() &&
        		!VilMethods.getDuty(this.villager))
        {
            this.interactionDelay = 10;
        }
        else
        {
            this.interactionDelay = 0;
        }
    }

    @Override
	public void updateTask()
    {
        super.updateTask();

        if (this.interactionDelay > 0)
        {
            --this.interactionDelay;

            if (this.interactionDelay == 0)
            {
                InventoryBasic inventorybasic = this.villager.getVillagerInventory();

                for (int i = 0; i < inventorybasic.getSizeInventory(); ++i)
                {
                    ItemStack itemstack = inventorybasic.getStackInSlot(i);
                    ItemStack itemstack1 = ItemStack.EMPTY;

                    if (!itemstack.isEmpty())
                    {
                        Item item = itemstack.getItem();

                        if ((item == Items.BREAD || item == Items.POTATO || item == Items.CARROT || item == Items.BEETROOT) && itemstack.getCount() > 3)
                        {
                            int l = itemstack.getCount() / 2;
                            itemstack.shrink(l);
                            itemstack1 = new ItemStack(item, l, itemstack.getMetadata());
                        }
                        else if (item == Items.WHEAT && itemstack.getCount() > 5)
                        {
                            int j = itemstack.getCount() / 2 / 3 * 3;
                            int k = j / 3;
                            itemstack.shrink(j);
                            itemstack1 = new ItemStack(Items.BREAD, k, 0);
                        }

                        if (itemstack.isEmpty())
                        {
                            inventorybasic.setInventorySlotContents(i, ItemStack.EMPTY);
                        }
                    }

                    if (!itemstack1.isEmpty())
                    {
                        double d0 = this.villager.posY - 0.30000001192092896D + this.villager.getEyeHeight();
                        EntityItem entityitem = new EntityItem(this.villager.world, this.villager.posX, d0, this.villager.posZ, itemstack1);
                        float f1 = this.villager.rotationYawHead;
                        float f2 = this.villager.rotationPitch;
                        entityitem.motionX = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * 0.3F;
                        entityitem.motionZ = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * 0.3F;
                        entityitem.motionY = -MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F;
                        entityitem.setDefaultPickupDelay();
                        this.villager.world.spawnEntity(entityitem);
                        break;
                    }
                }
            }
        }
    }
}