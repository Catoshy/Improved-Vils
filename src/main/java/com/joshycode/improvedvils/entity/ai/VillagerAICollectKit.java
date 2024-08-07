package com.joshycode.improvedvils.entity.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.entity.ai.RangeAttackEntry.WeaponBrooksData;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

public class VillagerAICollectKit extends EntityAIGoFar {

	private EntityVillager villager;
	private InventoryHands villagerKit;
	private Map<Item, EntityEquipmentSlot> kitToCollect;
	private Map<Item, Integer> ammoToCollect;
	private Set<Item> leftOversToPutAway;

	public VillagerAICollectKit(EntityVillager villager, int mostFails)
	{
		super(villager, 4, mostFails);
		this.villager = villager;
		this.villagerKit = new InventoryHands(villager, "Hands", false);
		this.ammoToCollect = new HashMap<>();
		this.kitToCollect = new HashMap<>();
		this.leftOversToPutAway = new HashSet<>();
	}

	@Override
	public boolean shouldExecute()
	{
		if((VilMethods.getKitStorePos(this.villager) == null) || this.isDoingSomethingMoreImportant() || (this.villager.getRNG().nextInt(5) != 0))
			return false;
		
		BlockPos kitStore = getObjectiveBlock();	
		IInventory inv = getTileInventory();
		if(kitStore == null || inv == null) return false;
		
		if(VilMethods.getGuardBlockPos(this.villager) != null)
		{
			double dist = VilMethods.getGuardBlockPos(this.villager).getDistance(kitStore.getX(), kitStore.getY(), kitStore.getZ());
			double distSq = dist * dist;

			if(distSq > CommonProxy.GUARD_IGNORE_LIMIT)
				return false;
		}
		boolean collectKit = this.canCollectKit(inv);
		boolean collectAmmo = this.canCollectAmmo(inv);
		boolean collectPotions = this.canCollectPotions(inv);
		
		return collectKit || collectAmmo || collectPotions;
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
		//Search for weapon-ammo combo already used by villager. If one is found, flag = true
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
				setAmmoToCollect(multiplied);
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
			setAmmoToCollect(multiplyConsumables(ammunitionHeld));
			return true;
		}
		return false;
	}

	private void setAmmoToCollect(Map<Item, Integer> multiplied) 
	{
		for(Map.Entry<Item, Integer> entry : multiplied.entrySet())
		{
			if(entry.getValue() < 0 && InventoryUtil.doesInventoryHaveItem(this.villager.getVillagerInventory(), entry.getKey()) > 0)
				this.leftOversToPutAway.add(entry.getKey());
			else if(entry.getValue() == 0 && InventoryUtil.doesInventoryHaveItem(this.villager.getVillagerInventory(), entry.getKey()) == 0)
				entry.setValue(1);
		}
		this.ammoToCollect.putAll(multiplied);
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
		super.startExecuting();
		VilMethods.setRefilling(this.villager, true);
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		int revengeTime = this.villager.ticksExisted - this.villager.getRevengeTimer();
		if((revengeTime < 20 && revengeTime >= 0) || this.pathfindingFails > this.mostPathfindingFails)
			return false;
		if((VilMethods.getGuardBlockPos(villager) != null && this.finished) || (this.villager.getNavigator().noPath() && this.finished))
			return false;
		return true;
	}

	private void refillInventory()
	{
		this.finished = false;
		IInventory storeInv = getTileInventory();
		InventoryBasic vilInv = this.villager.getVillagerInventory();
		
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
					stack.grow(vilInv.addItem(intoInv).getCount());
					VilMethods.setOutOfAmmo(this.villager, false);
					continue;
				}
				for(Item itemToPutInChest : this.leftOversToPutAway)
				{
					ItemStack putStack = InventoryUtil.getGreatestStackByItem(vilInv, itemToPutInChest);
					boolean flag = false;
					if(putStack == null)
					{
						this.leftOversToPutAway.remove(itemToPutInChest);
						continue;
					}
					if(stack.isEmpty())
					{
						flag = true;
						storeInv.setInventorySlotContents(i, putStack.splitStack(Math.min(storeInv.getInventoryStackLimit(), putStack.getMaxStackSize())));
					}
					else if(ItemStack.areItemsEqual(stack, putStack) && stack.isStackable())
					{
						flag = true;
						int j = Math.min(stack.getCount() + putStack.getCount(), stack.getMaxStackSize());
						int growAmount = Math.min(j, storeInv.getInventoryStackLimit());
						stack.grow(putStack.splitStack(growAmount).getCount());
					}
					if(flag)
						this.leftOversToPutAway.remove(itemToPutInChest);
				}
			}		
			VillagerPlayerDealMethods.checkArmourWeaponsAndFood(this.villager, VilMethods.getPlayerId(this.villager));
			this.finished = true;
		}
		else
		{
			VilMethods.setKitStore(this.villager, null);
		}
	}

	@Nullable
	IInventory getTileInventory()
	{
		IInventory iinventory = null;
		World world = this.villager.getEntityWorld();
		BlockPos kitStore = getObjectiveBlock();
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

        int x = kitStore.getX();
        int y = kitStore.getY();
        int z = kitStore.getZ();

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
	
	@Override
	protected BlockPos getObjectiveBlock() 
	{ 
		return VilMethods.getKitStorePos(this.villager); 
	}
	
	@Override
	protected boolean breakDoors() 
	{ 
		return true; 
	}
	
	@Override
	protected double hostSpeed() { return .7D; }
	
	@Override
	protected void resetObjective() 
	{
		VilMethods.setRefilling(this.villager, false);
	}
	
	@Override
	protected void arrivedAtObjective() 
	{
		refillInventory();
		
		Vec3d vec1 = getRandomPosition();
		if(vec1 != null)
			this.entityHost.getNavigator().tryMoveToXYZ(vec1.x, vec1.y, vec1.z, this.entityHost.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
	}
}
