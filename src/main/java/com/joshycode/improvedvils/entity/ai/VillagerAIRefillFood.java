package com.joshycode.improvedvils.entity.ai;

import java.util.List;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VillagerAIRefillFood extends EntityAIGoFar {

	private static final String refillCooldownInfo = ImprovedVils.MODID + ":refill";
	private int refillCooldown;

	public VillagerAIRefillFood(EntityVillager villager, int mostFails)
	{
		super(villager, 4, mostFails, true);
		this.refillCooldown = villager.getEntityData().getInteger(refillCooldownInfo);
		if(this.refillCooldown <= 0)
			this.refillCooldown = 1;
	}

	@Override
	public boolean shouldExecute()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		BlockPos foodStore = getObjectiveBlock();
		
		if(foodStore == null || this.isDoingSomethingMoreImportant() || (villager.getRNG().nextInt(5) != 0))
			return false;
		
		IInventory inv = getTileInventory();
		if(inv == null) return false;
		
		BlockPos herePos = VilMethods.getGuardBlockPos(villager) != null ? VilMethods.getGuardBlockPos(villager) : villager.getPosition();
		double dist = herePos.getDistance(foodStore.getX(), foodStore.getY(), foodStore.getZ());
		double distSq = dist * dist;
	
		if(VilMethods.getGuardBlockPos(villager) != null && distSq > CommonProxy.GUARD_IGNORE_LIMIT)
			return false;
		if(distSq > CommonProxy.GUARD_IGNORE_LIMIT * 2.25D)
			return false;

		float totalFoodSaturation = 0;
		float collectThreshold = ConfigHandler.collectFoodThreshold * .6F;
		for(ItemStack foodItem : InventoryUtil.getStacksByItemClass(villager.getVillagerInventory(), ItemFood.class))
		{
			totalFoodSaturation += ((ItemFood) foodItem.getItem()).getSaturationModifier(foodItem) * foodItem.getCount();
		}
		if(totalFoodSaturation < collectThreshold)
		{
			return this.canCollectFood(inv);
		}

		return false;
	}
	
	private boolean isDoingSomethingMoreImportant()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		if((VilMethods.getCommBlockPos(villager) != null) || VilMethods.isOutsideHomeDist(villager) || VilMethods.isReturning(villager) || VilMethods.getMovingIndoors(villager))
			return true;
		if(villager.isMating())
    		return true;
		if(VilMethods.getFollowing(villager))
			return true;
		if(villager.getAttackTarget() != null)
			return true;
		return false;
	}

	private boolean canCollectFood(IInventory inv) 
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(inv.getStackInSlot(i).getItem() instanceof ItemFood)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void startExecuting()
	{
		super.startExecuting();
		VilMethods.setRefilling((EntityVillager) this.entityHost, true);
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		EntityVillager villager = (EntityVillager) this.entityHost;
		int revengeTime = villager.ticksExisted - villager.getRevengeTimer();
		if(revengeTime < 20 && revengeTime >= 0)
			return false;
		if((VilMethods.getGuardBlockPos(villager) != null && this.finished) || (villager.getNavigator().noPath() && this.finished))
			return false;
		return super.shouldContinueExecuting();
	}
	
	private void refillInventory()
	{
		this.finished = false;
		IInventory storeInv = getTileInventory();
		EntityVillager villager = (EntityVillager) this.entityHost;
		float saturation = ConfigHandler.collectFoodThreshold * .6f;
		if(storeInv != null)
		{

			for(int i = 0; i < storeInv.getSizeInventory(); i++)
			{
				ItemStack stack = storeInv.getStackInSlot(i);
				if(stack.getItem() instanceof ItemFood && saturation > 0)
				{
					float itemSaturation = ((ItemFood) stack.getItem()).getSaturationModifier(stack);
					int refillCount;

					if(saturation - (stack.getCount() * itemSaturation) < 0)
					{
						refillCount = (int) (saturation / itemSaturation);
					}
					else
					{
						refillCount = stack.getCount();
					}
					stack.grow(villager.getVillagerInventory().addItem(stack.splitStack(refillCount)).getCount());
					saturation -= itemSaturation * refillCount;
				}
			}			
			VillagerPlayerDealMethods.checkArmourWeaponsAndFood(villager, VilMethods.getPlayerId(villager));
			this.finished = true;
		}
		else
		{
			VilMethods.setFoodStore(villager, null);
		}
	}

	@Nullable
	IInventory getTileInventory()
	{
		IInventory iinventory = null;
		World world = this.entityHost.getEntityWorld();
		BlockPos foodStore = getObjectiveBlock();
		IBlockState state = world.getBlockState(foodStore);
		Block block = state.getBlock();
		if(!block.hasTileEntity(state))
			return null;

        TileEntity tileentity = world.getTileEntity(foodStore);

        if (tileentity instanceof IInventory)
        {
            iinventory = (IInventory)tileentity;

            if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
            {
                iinventory = ((BlockChest)block).getContainer(world, foodStore, true);
            }
        }

        int x = foodStore.getX();
        int y = foodStore.getY();
        int z = foodStore.getZ();

        if (iinventory == null)
        {
            List<Entity> list = world.getEntitiesInAABBexcluding((Entity) this.entityHost, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

            if (!list.isEmpty())
            {
                iinventory = (IInventory)list.get(world.rand.nextInt(list.size()));
            }
        }
        return iinventory;
	}

	@Override
	public void resetObjective()
	{
		this.refillCooldown = this.entityHost.getRNG().nextInt(50) + 75;
		VilMethods.setRefilling((EntityVillager) this.entityHost, false);
	}
	
	@Override
	protected BlockPos getObjectiveBlock() 
	{ 
		return VilMethods.getFoodStorePos((EntityVillager) this.entityHost); 
	}
	
	@Override
	protected boolean breakDoors() { return true; }
	
	@Override
	protected void arrivedAtObjective() 
	{
		refillInventory();
		
		Vec3d vec1 = getRandomPosition();
		if(vec1 != null)
			this.entityHost.getNavigator().tryMoveToXYZ(vec1.x, vec1.y, vec1.z, this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
	}
}
