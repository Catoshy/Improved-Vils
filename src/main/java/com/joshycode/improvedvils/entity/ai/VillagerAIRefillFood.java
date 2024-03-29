package com.joshycode.improvedvils.entity.ai;

import java.util.List;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
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

public class VillagerAIRefillFood extends EntityAIBase {

	private EntityVillager villager;
	private int idleTicks;
	private int waitTicks;
	private boolean refilled;
	BlockPos foodStore;
	BlockPos prevPos;

	public VillagerAIRefillFood(EntityVillager villager)
	{
		super();
		this.villager = villager;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getFoodStorePos(villager) == null) || this.isDoingSomethingMoreImportant() || (this.villager.getRNG().nextInt(5) != 0) || this.waitTicks-- > 0)
			return false;
		if(VilMethods.getGuardBlockPos(villager) != null)
		{
			BlockPos foodStorePos = VilMethods.getFoodStorePos(villager);
			if(foodStorePos != null)
			{
				double dist = VilMethods.getGuardBlockPos(villager).getDistance(foodStorePos.getX(), foodStorePos.getY(), foodStorePos.getZ());
				double distSq = dist * dist;

				if(distSq > CommonProxy.GUARD_IGNORE_LIMIT)
					return false;
			}
			else
			{
				return false;
			}
		}

		float totalFoodSaturation = 0;
		for(ItemStack foodItem : InventoryUtil.getStacksByItem(this.villager.getVillagerInventory(), ItemFood.class))
		{
			totalFoodSaturation += ((ItemFood) foodItem.getItem()).getSaturationModifier(foodItem) * foodItem.getCount();
		}
		if(totalFoodSaturation < (ConfigHandler.collectFoodThreshold * .6f))
		{
			return true;
		}

		return false;
	}

	@Override
	public void startExecuting()
	{
		this.refilled = false;
		this.idleTicks = 0;
		VilMethods.setRefilling(this.villager, true);
		this.foodStore = VilMethods.getFoodStorePos(villager);
		this.villager.getNavigator().tryMoveToXYZ(foodStore.getX(), foodStore.getY(), foodStore.getZ(), this.villager.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
	}

	private boolean isDoingSomethingMoreImportant()
	{
		if((VilMethods.getCommBlockPos(this.villager) != null) || VilMethods.isOutsideHomeDist(this.villager) || VilMethods.isReturning(this.villager) || VilMethods.getMovingIndoors(this.villager))
			return true;
		if(this.villager.isMating())
    		return true;
		if(VilMethods.getFollowing(this.villager))
			return true;
		if(this.villager.getAttackTarget() != null)
			return true;
		return false;
	}

	@Override
	public void updateTask()
	{
		if(this.villager.getDistanceSq(this.foodStore) < 2.0D && !this.refilled)
		{
			this.refilled = true;
			refillInventory();

			Vec3d vec = getPosition();
			if(vec != null)
				this.villager.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, this.villager.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}
		if(this.villager.getPosition().equals(this.prevPos))
		{
			this.idleTicks++;
		}
		else
		{
			idleTicks = 0;
		}
		this.prevPos = this.villager.getPosition();
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		int revengeTime = this.villager.ticksExisted - this.villager.getRevengeTimer();
		if((revengeTime < 20 && revengeTime >= 0) || this.idleTicks > 40)
			return false;
		if((VilMethods.getGuardBlockPos(villager) != null && this.refilled) || (this.villager.getNavigator().noPath() && this.refilled))
			return false;
		return true;
	}

	@Override
	public void resetTask()
	{
		super.resetTask();
		VilMethods.setRefilling(this.villager, false);
	}

	private void refillInventory()
	{
		IInventory storeInv = getTileInventory();
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
			float collectedSaturation = ConfigHandler.collectFoodThreshold * .6f - saturation;
			
			if(collectedSaturation == 0)
				this.waitTicks = 500;
			
			this.changePlayerReputation(collectedSaturation);
				
		}
		else
		{
			VilMethods.setFoodStore(this.villager, null);
		}
	}

	private void changePlayerReputation(float collectedSaturation) 
	{
		IImprovedVilCapability vilCap = this.villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		if(vilCap.getPlayerId() != null)
			VillagerPlayerDealMethods.changePlayerReputation(this.villager, vilCap.getPlayerId(), collectedSaturation);
	}

	@Nullable
	IInventory getTileInventory()
	{
		IInventory iinventory = null;
		World world = this.villager.getEntityWorld();
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

        int x = this.foodStore.getX();
        int y = this.foodStore.getY();
        int z = this.foodStore.getZ();

        if (iinventory == null)
        {
            List<Entity> list = world.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

            if (!list.isEmpty())
            {
                iinventory = (IInventory)list.get(world.rand.nextInt(list.size()));
            }
        }
        return iinventory;
	}

	@Nullable
	protected Vec3d getPosition()
    {
        return RandomPositionGenerator.findRandomTarget(this.villager, 8, 6);
    }

}
