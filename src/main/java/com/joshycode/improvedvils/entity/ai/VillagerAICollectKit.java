package com.joshycode.improvedvils.entity.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.PathUtil;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHealth;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VillagerAICollectKit extends EntityAIBase {

	private EntityVillager villager;
	private InventoryHands villagerKit;
	private Map<Item, EntityEquipmentSlot> kitToCollect;
	private Map<Item, Integer> ammoToCollect;
	private int idleTicks;
	private int pathfindingFails;
	private final int mostPathfindingFails;
	private float distanceToObj;
	private boolean refilled;
	BlockPos kitStore;
	BlockPos prevPos;
	private Path path;

	public VillagerAICollectKit(EntityVillager villager, int mostFails)
	{
		super();
		this.villager = villager;
		this.mostPathfindingFails = mostFails;
		this.pathfindingFails = 0;
		this.villagerKit = new InventoryHands(villager, "Hands", false);
		this.ammoToCollect = new HashMap<>();
		this.kitToCollect = new HashMap<>();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getKitStorePos(this.villager) == null) || this.isDoingSomethingMoreImportant() || (this.villager.getRNG().nextInt(5) != 0))
			return false;
		
		this.kitStore = VilMethods.getKitStorePos(this.villager);	
		IInventory inv = getTileInventory();
		if(this.kitStore == null || inv == null) return false;
		
		if(VilMethods.getGuardBlockPos(this.villager) != null)
		{
			double dist = VilMethods.getGuardBlockPos(this.villager).getDistance(this.kitStore.getX(), this.kitStore.getY(), this.kitStore.getZ());
			double distSq = dist * dist;

			if(distSq > CommonProxy.GUARD_IGNORE_LIMIT)
				return false;
		}
		boolean collectKit = this.canCollectKit(inv);
		boolean collectAmmo = this.canCollectAmmo(inv);
		boolean collectPotions = this.canCollectPotions(inv);
		
		if(collectKit || collectAmmo || collectPotions)
			Log.info("So true! %s", this.villager.getUniqueID());
		return collectKit || collectAmmo || collectPotions;
	}

	private boolean canCollectPotions(IInventory inv) 
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			for(PotionEffect effect : PotionUtils.getEffectsFromStack(stack))
			{
				if(effect.getPotion() instanceof PotionHealth)
				{
					this.ammoToCollect.put(stack.getItem(), 1);
					return true;
				}
			}
		}
		return false;
	}

	private boolean canCollectAmmo(IInventory inv) 
	{
		if(this.villager.getHeldItemMainhand().isEmpty())
			return false;
		
		String s = this.villager.getHeldItemMainhand().getUnlocalizedName();
		boolean flag = false;
		RangeAttackEntry ammunitionHeld = null;
		WeaponBrooksData heldWeapon = null;
		for(WeaponBrooksData weapon : ConfigHandler.configuredGuns.keySet())
		{
			if(s.equals(weapon.itemUnlocalizedName))
			{
				heldWeapon = weapon;
				ammunitionHeld = searchInvForAmmoEntry(heldWeapon, this.villager.getVillagerInventory());
				
				if(ammunitionHeld != null)
				{
					flag = true;
					break;
				}
			}
		}
		
		if(flag)
		{
			Map<Item, Integer> multiplied = multiplyConsumables(ammunitionHeld);
			if(InventoryUtil.getItemStacksInInventory(this.villager.getVillagerInventory(), multiplied) != null)
				return false;
			if(InventoryUtil.getItemStacksInInventory(inv, ammunitionHeld.getConsumables()) != null)
			{
				this.ammoToCollect.putAll(multiplied);
				return true;
			}
			else
			{
				return false;
			}
		}
		
		ammunitionHeld = searchInvForAmmoEntry(heldWeapon, inv);
		if(ammunitionHeld != null)
		{
			//try to collect 32 rounds of whatever ammo is needed
			Map<Item, Integer> multiplied = multiplyConsumables(ammunitionHeld);
			this.ammoToCollect.putAll(multiplied);
			return true;
		}
		return false;
	}

	private Map<Item, Integer> multiplyConsumables(RangeAttackEntry ammunitionHeld) 
	{
		Map<Item, Integer> consumables = new HashMap<Item, Integer>(ammunitionHeld.getConsumables());
		for(Item item : consumables.keySet())
		{
			int val = consumables.get(item);
			val *= 32;
			consumables.put(item, val);
		}
		return consumables;
	}

	private RangeAttackEntry searchInvForAmmoEntry(WeaponBrooksData weapons, IInventory inv) 
	{
		if(ConfigHandler.configuredGuns.get(weapons) == null)
			return null;
		for(RangeAttackEntry entry : ConfigHandler.configuredGuns.get(weapons))
		{
			if(InventoryUtil.getItemStacksInInventory(inv, entry.getConsumables()) != null)
			{
				return entry;
			}
		}
		return null;
	}

	private boolean canCollectKit(IInventory inv) 
	{
		ItemStack hasMainhand = null;
		ItemStack hasOffhand = null;
		ItemStack hasHead = null;
		ItemStack hasChest = null;
		ItemStack hasLegs = null;
		ItemStack hasFeet = null;
		boolean flag = false;
		
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.isEmpty())
				continue;
			
			if(stack.getItem().getItemUseAction(stack) == EnumAction.BLOCK)
			{
				hasOffhand = hasOffhand == null ? stack : hasOffhand;
				continue;
			}
			if(stack.getItem().getItemUseAction(stack) == EnumAction.BOW)
			{
				hasMainhand = hasMainhand == null ? stack : hasMainhand;
				continue;
			}
			String s = stack.getUnlocalizedName();
			for(WeaponBrooksData g : ConfigHandler.configuredGuns.keySet())
			{
				if(s.equals(g.itemUnlocalizedName))
				{
					hasMainhand = hasMainhand == null ? stack : hasMainhand;
					continue;
				}
			}
			
			Iterator<AttributeModifier> attribute = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator();
			boolean isMainHandWorthy = false;
			if(attribute.hasNext())
				isMainHandWorthy = attribute.next().getAmount() > 0D;
				
			int flag1 = EntityLiving.getSlotForItemStack(stack).getSlotIndex();
			
			hasMainhand = (isMainHandWorthy && hasMainhand == null) ? stack : hasMainhand;
			hasOffhand = (flag1 == 5 && hasOffhand == null) ? stack : hasOffhand;
			hasHead = (flag1 == 4 && hasHead == null) ? stack : hasHead;
			hasChest = (flag1 == 3 && hasChest == null) ? stack : hasChest;
			hasLegs = (flag1 == 2 && hasLegs == null) ? stack : hasLegs;
			hasFeet = (flag1 == 1 && hasFeet == null) ? stack : hasFeet;
		}
		
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.MAINHAND).isEmpty() && hasMainhand != null)
		{
			this.kitToCollect.put(hasMainhand.getItem(), EntityEquipmentSlot.MAINHAND);
			flag = true;
		}
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.OFFHAND).isEmpty() && hasOffhand != null)
		{
			this.kitToCollect.put(hasOffhand.getItem(), EntityEquipmentSlot.OFFHAND);
			flag = true;
		}
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.HEAD).isEmpty() && hasHead != null)
		{
			this.kitToCollect.put(hasHead.getItem(), EntityEquipmentSlot.HEAD);
			flag = true;
		}
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.CHEST).isEmpty() && hasChest != null)
		{
			this.kitToCollect.put(hasChest.getItem(), EntityEquipmentSlot.CHEST);
			flag = true;
		}
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.LEGS).isEmpty() && hasLegs != null)
		{
			this.kitToCollect.put(hasLegs.getItem(), EntityEquipmentSlot.LEGS);
			flag = true;
		}
		if(this.villagerKit.getStackInSlot(EntityEquipmentSlot.FEET).isEmpty() && hasFeet != null)
		{
			this.kitToCollect.put(hasFeet.getItem(), EntityEquipmentSlot.FEET);
			flag = true;
		}		
		return flag;
	}

	@Override
	public void startExecuting()
	{
		this.refilled = false;
		this.idleTicks = 0;
		VilMethods.setRefilling(this.villager, true);
		this.kitStore = VilMethods.getKitStorePos(this.villager);
		if(generatePath())
		{
			PathPoint pp = this.path.getFinalPathPoint();
			if(pp != null)
			{
				this.distanceToObj = pp.distanceTo(new PathPoint(this.kitStore.getX(), this.kitStore.getY(), this.kitStore.getZ()));
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
		if(this.villager.getAttackTarget() != null && !VilMethods.outOfAmmo(this.villager))
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
		if(this.villager.getDistanceSq(this.kitStore) < 4.0D && !this.refilled)
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
		this.ammoToCollect.clear();
		this.kitToCollect.clear();
		this.pathfindingFails = 0;
		VilMethods.setRefilling(this.villager, false);
	}

	private void refillInventory()
	{
		IInventory storeInv = getTileInventory();
		if(storeInv != null)
		{
			VillagerPlayerDealMethods.updateArmourWeaponsAndFood(this.villager);
			
			for(int i = 0; i < storeInv.getSizeInventory(); i++)
			{
				ItemStack stack = storeInv.getStackInSlot(i);
				
				EntityEquipmentSlot slot = this.kitToCollect.get(stack.getItem());
				if(slot != null && this.villagerKit.getStackInSlot(slot).isEmpty())
				{
					this.villagerKit.setEquipmentSlot(slot, stack.splitStack(1));
					continue;
				}
				if(this.ammoToCollect.containsKey(stack.getItem()))
				{
					int val = this.ammoToCollect.get(stack.getItem());
					ItemStack intoInv = stack.splitStack(val);
					val -= intoInv.getCount();
					if(val <= 0)
						this.ammoToCollect.remove(stack.getItem());
					else
						this.ammoToCollect.put(stack.getItem(), val);
					stack.grow(this.villager.getVillagerInventory().addItem(intoInv).getCount());
					VilMethods.setOutOfAmmo(this.villager, false);
					continue;
				}
			}		
			VillagerPlayerDealMethods.checkArmourWeaponsAndFood(this.villager, VilMethods.getPlayerId(this.villager));
			this.refilled = true;
		}
		else
		{
			if(this.villager.world.isAreaLoaded(this.kitStore, 1))
				VilMethods.setKitStore(this.villager, null);
		}
	}

	@Nullable
	IInventory getTileInventory()
	{
		IInventory iinventory = null;
		World world = this.villager.getEntityWorld();
		IBlockState state = world.getBlockState(kitStore);
		Block block = state.getBlock();
		if(!block.hasTileEntity(state))
			return null;

        TileEntity tileentity = world.getTileEntity(kitStore);

        if (tileentity instanceof IInventory)
        {
            iinventory = (IInventory)tileentity;

            if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
            {
                iinventory = ((BlockChest)block).getContainer(world, kitStore, true);
            }
        }

        int x = this.kitStore.getX();
        int y = this.kitStore.getY();
        int z = this.kitStore.getZ();

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
				float newDistanceToObj = pp.distanceTo(new PathPoint(this.kitStore.getX(), this.kitStore.getY(), this.kitStore.getZ()));
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
		if(this.villager.getDistanceSq(this.kitStore) > CommonProxy.GUARD_MAX_PATH_SQ)
		{
			Vec3d pos1 = PathUtil.findNavigableBlockInDirection(this.villager.getPosition(), this.kitStore, this.villager);
			if(pos1 != null)
			{
				pos = pos1;
			}
			else
			{
				pos = RandomPositionGenerator.findRandomTargetBlockTowards(this.villager, 10, 7, new Vec3d(this.kitStore));
			}
		}
		else
		{
			pos = new Vec3d(this.kitStore);
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
