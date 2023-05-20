package com.joshycode.improvedvils.handler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.ServerProxy;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedvils.entity.ai.VillagerAIAttackNearestTarget;
import com.joshycode.improvedvils.entity.ai.VillagerAIAvoidEntity;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignEat;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedvils.entity.ai.VillagerAIDrinkPotion;
import com.joshycode.improvedvils.entity.ai.VillagerAIEatHeal;
import com.joshycode.improvedvils.entity.ai.VillagerAIFollow;
import com.joshycode.improvedvils.entity.ai.VillagerAIGuard;
import com.joshycode.improvedvils.entity.ai.VillagerAIHurtByTarget;
import com.joshycode.improvedvils.entity.ai.VillagerAIMate;
import com.joshycode.improvedvils.entity.ai.VillagerAIMoveIndoors;
import com.joshycode.improvedvils.entity.ai.VillagerAIMoveTowardsRestriction;
import com.joshycode.improvedvils.entity.ai.VillagerAIRefillFood;
import com.joshycode.improvedvils.entity.ai.VillagerAIRestrictOpenDoor;
import com.joshycode.improvedvils.entity.ai.VillagerAIShootRanged;
import com.joshycode.improvedvils.entity.ai.VillagerAIVillagerInteract;
import com.joshycode.improvedvils.entity.ai.VillagerAIWanderAvoidWater;
import com.joshycode.improvedvils.event.ChildGrowEvent;
import com.joshycode.improvedvils.util.VilAttributes;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.Village;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class EventHandlerVil {

	private Random rand;
	private static final String modify_villager = ImprovedVils.MODID + ":initAttr";
	
	public EventHandlerVil() 
	{
		this.rand = new Random();
	}
	
	@SubscribeEvent
	public void onVillagerConstruct(EntityConstructing e)
	{
		if(e.getEntity() instanceof EntityVillager) 
		{
			VilAttributes.applyConstr((EntityVillager) e.getEntity());
		}
	}
	
	@SubscribeEvent
	public void onVillagerJoinWorld(EntityJoinWorldEvent e) 
	{
		if(!(e.getEntity() instanceof EntityVillager))
			return;
		
		EntityVillager entity = (EntityVillager) e.getEntity();
		if(entity.isChild())
		{
			return;
		}
		VilAttributes.apply((EntityVillager) e.getEntity());
		addAiTasks(entity);
		if(ConfigHandlerVil.villagerHealth != 20f && !entity.getEntityData().getBoolean(modify_villager))
		{
			entity.setHealth(ConfigHandlerVil.villagerHealth);
		}
		entity.getEntityData().setBoolean(modify_villager, true);
	}
	
	@SubscribeEvent
	public void onVillagerGrow(ChildGrowEvent e) 
	{
		if(e.getEntity() instanceof EntityVillager) 
		{
			addAiTasks((EntityVillager) e.getEntity());
			VilAttributes.apply((EntityVillager) e.getEntity());
			ServerProxy.childGrown((EntityVillager) e.getEntity());
			if(ConfigHandlerVil.villagerHealth != 20f) 
			{
				((EntityVillager) e.getEntity()).setHealth(ConfigHandlerVil.villagerHealth);
			}
			((EntityVillager) e.getEntity()).getEntityData().setBoolean(modify_villager, true);
		}
	}
	
	@SubscribeEvent
	public void onAgeableUpdate(LivingUpdateEvent e)
	{
		if(e.getEntity().getEntityWorld().isRemote)
			return;
		
		if(e.getEntity() instanceof EntityAgeable) 
		{
			EntityAgeable ageable = (EntityAgeable) e.getEntity();
			if(ageable.getGrowingAge() == -1) 
			{
				MinecraftForge.EVENT_BUS.post(new ChildGrowEvent(ageable));
			}
		}
	}
	
	@SubscribeEvent
	public void onVillagerAttacked(LivingAttackEvent e) 
	{
		if(e.getEntityLiving() instanceof EntityVillager)
		{
			if(e.getEntityLiving().getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK)
				if(e.getAmount() > e.getEntityLiving().getMaxHealth() * .75D || this.rand.nextFloat() + Float.MIN_VALUE < ConfigHandlerVil.blockChance)
					if(e.getEntityLiving().getActiveHand() != EnumHand.OFF_HAND) 
					{
							e.getEntityLiving().resetActiveHand();
							e.getEntityLiving().setActiveHand(EnumHand.OFF_HAND);
					}
			if(e.getEntityLiving().getAttackingEntity() instanceof EntityPlayer)
			{
				if(e.getEntityLiving().isChild())
					handleVillageBadReputation(((EntityVillager) e.getEntityLiving()), ((EntityPlayer)e.getEntityLiving().getAttackingEntity()), 3);
				else
					handleVillageBadReputation(((EntityVillager) e.getEntityLiving()), ((EntityPlayer)e.getEntityLiving().getAttackingEntity()), 1);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDies(LivingDeathEvent e) 
	{
		if(e.getEntity().getEntityWorld().isRemote) return;
		
		if(e.getEntityLiving() instanceof EntityVillager) 
		{
			if(e.getEntityLiving().getAttackingEntity() instanceof EntityPlayer)
				handleVillageBadReputation(((EntityVillager) e.getEntityLiving()), ((EntityPlayer)e.getEntityLiving().getAttackingEntity()), 2);
			e.getEntityLiving().captureDrops = true;
			InventoryHands inv = new InventoryHands((EntityVillager) e.getEntity(), "Hands", false);
			for(int i = 0; i < inv.getSizeInventory(); i++) 
			{
				ItemStack stack = inv.getStackInSlot(i);
				if(!stack.isEmpty()) 
				{
					e.getEntity().entityDropItem(stack, 0);
				}
			}
			for(int i = 0; i < ((EntityVillager) e.getEntity()).getVillagerInventory().getSizeInventory(); i++) 
			{
				ItemStack stack = ((EntityVillager) e.getEntity()).getVillagerInventory().getStackInSlot(i);
				if(stack != ItemStack.EMPTY) 
				{
					e.getEntity().entityDropItem(stack, 0);
				}
			}
			if(!net.minecraftforge.common.ForgeHooks.onLivingDrops(e.getEntityLiving(), e.getSource(), e.getEntityLiving().capturedDrops, 0, false)) 
			{
				for(EntityItem eitem : e.getEntityLiving().capturedDrops)
					e.getEntity().getEntityWorld().spawnEntity(eitem);
			}
			e.getEntityLiving().captureDrops = false;
		}
		else if(e.getEntityLiving() instanceof EntityMob || CommonProxy.TARGETS.contains(e.getEntityLiving().getClass()))
		{
			if(e.getEntityLiving().getAttackingEntity() instanceof EntityPlayer)
				handleVillageGoodReputation(e.getEntityLiving(), ((EntityPlayer)e.getEntityLiving().getAttackingEntity()), 1);
		}
		else if(e.getEntityLiving() instanceof EntityIronGolem)
		{
			EntityIronGolem golem = (EntityIronGolem) e.getEntityLiving();
			if(!golem.isPlayerCreated() && golem.getAttackingEntity() instanceof EntityPlayer && golem.getVillage() != null)
			{
				golem.getVillage().modifyPlayerReputation(golem.getAttackingEntity().getUniqueID(), 5);
			}
		}
	}
	
	private void handleVillageBadReputation(EntityVillager villager, EntityPlayer attackingPlayer, int reputationCost) 
	{
		Village village = villager.getEntityWorld().getVillageCollection().getNearestVillage(villager.getPosition(), 0);
		if(village != null)
		{
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			IImprovedVilCapability vilCap = villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			if((vilCap.getTeam() != null &&  vilCap.getTeam().equals(villageCap.getTeam()))
					||	vilCap.getTeam() == null || villager.isChild())
			{
				ServerProxy.villageBadReputationChange(villager.getEntityWorld(), village, attackingPlayer);
			}
			else
			{
				village.modifyPlayerReputation(attackingPlayer.getUniqueID(), reputationCost);
			}
		}
	}
	
	private void handleVillageGoodReputation(EntityLivingBase entity, EntityPlayer player, int reputation)
	{
		Village village = entity.getEntityWorld().getVillageCollection().getNearestVillage(entity.getPosition(), 0);
		if(village != null)
		{
			village.modifyPlayerReputation(player.getUniqueID(), reputation);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void openVillagerInv(EntityInteractSpecific event) 
	{
		if(!event.getWorld().isRemote && event.getTarget() instanceof EntityVillager && event.getEntityPlayer().isSneaking())
		{
			ClientProxy.openGuiForPlayerIfOK(event.getEntityLiving().getEntityId());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addAiTasks(EntityVillager entity) 
	{
		Collection<EntityAITaskEntry> toRem = new HashSet();
		for(EntityAITaskEntry ai : entity.tasks.taskEntries) 
		{
			if(ai.action instanceof EntityAIAvoidEntity) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIMoveIndoors) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIRestrictOpenDoor) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIWander) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIMoveTowardsRestriction) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIVillagerMate) 
			{
				toRem.add(ai);
			}
			else if(ai.action instanceof EntityAIVillagerInteract) 
			{
				toRem.add(ai);
			}
		}
		entity.tasks.taskEntries.removeAll(toRem);
		entity.tasks.addTask(9, new VillagerAIWanderAvoidWater(entity, .6D));
		entity.tasks.addTask(9, new VillagerAIVillagerInteract(entity));
		entity.tasks.addTask(5, new VillagerAIRefillFood(entity));
		entity.tasks.addTask(4, new VillagerAIMate(entity));
		entity.tasks.addTask(5, new VillagerAIMoveTowardsRestriction(entity, 0.6D));
		entity.tasks.addTask(5, new VillagerAIFollow(entity, .67D, 6.0F));
		entity.tasks.addTask(4, new VillagerAIMoveIndoors(entity));
		entity.tasks.addTask(4, new VillagerAIDrinkPotion(entity));
		entity.tasks.addTask(4, new VillagerAIAttackMelee(entity, .55D, false));
		entity.tasks.addTask(4, new VillagerAIShootRanged(entity, 10, 16, .5F));
		entity.tasks.addTask(1, new VillagerAICampaignEat(entity));
		entity.tasks.addTask(1, new VillagerAIEatHeal(entity));
		entity.tasks.addTask(3, new VillagerAICampaignMove(entity, 490));
		entity.tasks.addTask(3, new VillagerAIRestrictOpenDoor(entity));
		entity.tasks.addTask(1, new VillagerAIGuard(entity, CommonProxy.MAX_GUARD_DIST, 4, 490));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityEvoker.class, 12.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityVindicator.class, 8.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityVex.class, 8.0F, 0.6D, 0.6D));
		for(String s : ConfigHandlerVil.attackableMobs) 
		{
			entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, 
					EntityList.getClass(new ResourceLocation(s)), true));
		}
		entity.targetTasks.addTask(2, new VillagerAIHurtByTarget(entity, false));
		entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntityMob.class, true));
		entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntityVillager.class, true, false, new VillagerPredicate<EntityVillager>(entity) {
			 public boolean apply(@Nullable EntityVillager potentialEnemyVil)
	            {
				 IImprovedVilCapability taskOwnerCapability =  this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
				 IImprovedVilCapability predCapability = potentialEnemyVil.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
				 	return predCapability.getTeam() != null && taskOwnerCapability.getTeam() != null && !taskOwnerCapability.getTeam().equals(predCapability.getTeam());
	            }
		}));
		entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntityPlayer.class, true, false, new VillagerPredicate<EntityPlayer>(entity) {
			 public boolean apply(@Nullable EntityPlayer player)
	            {
				 IImprovedVilCapability taskOwnerCapability =  this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
				 	return taskOwnerCapability.getTeam() != null && player.getTeam() != null && !taskOwnerCapability.getTeam().equals(player.getTeam().getName());
	            }
		}));
	}
	
	private static abstract class VillagerPredicate<T extends EntityLivingBase> implements Predicate<T> {

		protected EntityVillager taskOwner;
		
		protected VillagerPredicate(EntityVillager taskOwner) {
			super();
			this.taskOwner = taskOwner;
		}
	}
}
