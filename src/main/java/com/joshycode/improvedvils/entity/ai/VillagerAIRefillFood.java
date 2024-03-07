package com.joshycode.improvedvils.entity.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.PathUtil;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

//TODO combine exactsame methods between this class AIRefillKit, and AICampaignMove into one parent class for readability
public class VillagerAIRefillFood extends EntityAIBase {

	private static final String refillCooldownInfo = ImprovedVils.MODID + ":refill";
	private EntityVillager villager;
	private int idleTicks;
	private int pathfindingFails;
	private int refillCooldown;
	private final int mostPathfindingFails;
	private boolean refilled;
	BlockPos foodStore;
	BlockPos prevPos;
	Map<Item, Integer> foodToCollect;//TODO
	private Path path;
	private float distanceToObj;

	public VillagerAIRefillFood(EntityVillager villager, int mostFails)
	{
		super();
		this.mostPathfindingFails = mostFails;
		this.villager = villager;
		this.refillCooldown = villager.getEntityData().getInteger(refillCooldownInfo);
		if(this.refillCooldown <= 0)
			this.refillCooldown = 1;
		this.foodToCollect = new HashMap<Item, Integer>(); //TODO
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getFoodStorePos(this.villager) == null) || this.isDoingSomethingMoreImportant() || (this.villager.getRNG().nextInt(5) != 0))
			return false;
		
		this.foodStore = VilMethods.getFoodStorePos(this.villager);
		IInventory inv = getTileInventory();
		if(inv == null || this.foodStore == null) return false;
	
		if(VilMethods.getGuardBlockPos(this.villager) != null)
		{
			double dist = VilMethods.getGuardBlockPos(this.villager).getDistance(this.foodStore.getX(), this.foodStore.getY(), this.foodStore.getZ());
			double distSq = dist * dist;
							
			if(distSq > CommonProxy.GUARD_IGNORE_LIMIT)
				return false;
		}

		float totalFoodSaturation = 0;
		float collectThreshold = ConfigHandler.collectFoodThreshold * .6F;
		for(ItemStack foodItem : InventoryUtil.getStacksByItem(this.villager.getVillagerInventory(), ItemFood.class))
		{
			totalFoodSaturation += ((ItemFood) foodItem.getItem()).getSaturationModifier(foodItem) * foodItem.getCount();
		}
		if(totalFoodSaturation < collectThreshold)
		{
			return this.canCollectFood(inv);
		}

		return false;
	}

	private boolean canCollectFood(IInventory inv) 
	{
		this.foodToCollect.clear();//TODO
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
		this.refilled = false;
		this.idleTicks = 0;
		VilMethods.setRefilling(this.villager, true);
		this.foodStore = VilMethods.getFoodStorePos(this.villager);
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null)
			{
				this.distanceToObj = pp.distanceTo(new PathPoint(this.foodStore.getX(), this.foodStore.getY(), this.foodStore.getZ()));
			}
			this.villager.getNavigator().setPath(this.path, this.villager.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}
		this.prevPos = this.villager.getPosition();
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
		if(this.idleTicks > 20)
		{
			this.idleTicks = 0;
			this.tryToGetCloser();
			this.pathfindingFails++;
		}
		if(this.villager.getDistanceSq(this.foodStore) < 4.0D && !this.refilled)
		{
			refillInventory();

			Vec3d vec1 = getRandomPosition();
			if(vec1 != null)
				this.villager.getNavigator().tryMoveToXYZ(vec1.x, vec1.y, vec1.z, this.villager.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}
		if(this.villager.getNavigator().getPath() != null && this.villager.getNavigator().getPath().isFinished())
		{
			this.tryToGetCloser();
		}
		else if(this.villager.getNavigator().getPath() == null)
		{
			this.pathfindingFails++;
			this.tryToGetCloser();
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
		if((revengeTime < 20 && revengeTime >= 0) || this.pathfindingFails > this.mostPathfindingFails)
			return false;
		if((VilMethods.getGuardBlockPos(villager) != null && this.refilled) || (this.villager.getNavigator().noPath() && this.refilled))
			return false;
		return true;
	}

	@Override
	public void resetTask()
	{
		super.resetTask();
		this.path = null;
		this.pathfindingFails = 0;
		this.refillCooldown = this.villager.getRNG().nextInt(50) + 75;
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
			
			this.changePlayerReputation(collectedSaturation);
			this.refilled = true;
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
		IBlockState state = world.getBlockState(this.foodStore);
		Block block = state.getBlock();
		if(!block.hasTileEntity(state))
			return null;

        TileEntity tileentity = world.getTileEntity(this.foodStore);

        if (tileentity instanceof IInventory)
        {
            iinventory = (IInventory)tileentity;

            if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
            {
                iinventory = ((BlockChest)block).getContainer(world, this.foodStore, true);
            }
        }

        int x = this.foodStore.getX();
        int y = this.foodStore.getY();
        int z = this.foodStore.getZ();

        if (iinventory == null)
        {
            List<Entity> list = world.getEntitiesInAABBexcluding((Entity) this.villager, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

            if (!list.isEmpty())
            {
                iinventory = (IInventory)list.get(world.rand.nextInt(list.size()));
            }
        }
        return iinventory;
	}

	@Nullable
	protected Vec3d getRandomPosition()
    {
        return RandomPositionGenerator.findRandomTarget(this.villager, 8, 6);
    }
	
	private void tryToGetCloser() 
	{
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null)
			{
				float newDistanceToObj = pp.distanceTo(new PathPoint(this.foodStore.getX(), this.foodStore.getY(), this.foodStore.getZ()));
				if(newDistanceToObj >= this.distanceToObj)
				{
					this.pathfindingFails++;
				}
				this.distanceToObj = newDistanceToObj;
			}
			this.villager.getNavigator().setPath(this.path, this.villager.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}	
	}
	
	private boolean generatePath()
	{
		Vec3d pos;
		if(this.villager.getDistanceSq(this.foodStore) > CommonProxy.GUARD_MAX_PATH_SQ)
		{
			Vec3d pos1 = PathUtil.findNavigableBlockInDirection(this.villager.getPosition(), this.foodStore, this.villager);
			if(pos1 != null)
				pos = pos1;
			else
				pos = RandomPositionGenerator.findRandomTargetBlockTowards(this.villager, 10, 7, new Vec3d(this.foodStore));
		}
		else
		{
			pos = new Vec3d(this.foodStore);
		}
		if(pos == null)
		{
			this.pathfindingFails++;
			return false;
		}
		this.path = this.villager.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z);
		if(this.path != null)
		{
			return true;
		}
		return false;
	}
}
