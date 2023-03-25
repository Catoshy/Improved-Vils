package com.joshycode.improvedmobs.handler;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.entity.InventoryHands;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackNearestTarget;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAvoidEntity;
import com.joshycode.improvedmobs.entity.ai.VillagerAICampaignEat;
import com.joshycode.improvedmobs.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedmobs.entity.ai.VillagerAIGuard;
import com.joshycode.improvedmobs.entity.ai.VillagerAIMoveIndoors;
import com.joshycode.improvedmobs.entity.ai.VillagerAIShootRanged;
import com.joshycode.improvedmobs.entity.ai.VillagerAIWanderAvoidWater;
import com.joshycode.improvedmobs.event.ChildGrowEvent;
import com.joshycode.improvedmobs.util.VilAttributes;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class EventHandlerVil {

	private Field targetClass;
	
	@SubscribeEvent
	public void onVillagerConstruct(EntityConstructing e) {
		if(e.getEntity() instanceof EntityVillager) {
			VilAttributes.applyConstr((EntityVillager) e.getEntity());
		}
	}
	
	@SubscribeEvent
	public void onVillagerSpawn(EntityJoinWorldEvent e) {
		if(!(e.getEntity() instanceof EntityVillager))
			return;
		EntityVillager entity = (EntityVillager) e.getEntity();
		if(entity.isChild())
		{
			return;
		}
		VilAttributes.apply((EntityVillager) e.getEntity());
		addAiTasks(entity);
	}
	
	@SubscribeEvent
	public void onVillagerGrow(ChildGrowEvent e) {
		if(e.getEntity() instanceof EntityVillager) {
			addAiTasks((EntityVillager) e.getEntity());
			VilAttributes.apply((EntityVillager) e.getEntity());
		}
	}
	
	@SubscribeEvent
	public void onAgeableUpdate(LivingUpdateEvent e) {
		if(e.getEntity().getEntityWorld().isRemote)
			return;
		if(e.getEntity() instanceof EntityAgeable) {
			EntityAgeable ageable = (EntityAgeable) e.getEntity();
			if(ageable.getGrowingAge() == -1) {
				MinecraftForge.EVENT_BUS.post(new ChildGrowEvent(ageable));
			}
		}
	}
	
	@SubscribeEvent
	public void onVillagerDies(LivingDeathEvent e) {
		if(e.getEntity() instanceof EntityVillager) {
			InventoryHands inv = new InventoryHands((EntityVillager) e.getEntity(), "Hands", false);
			for(int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if(stack != ItemStack.EMPTY) {
					if(!e.getEntity().getEntityWorld().isRemote)
						e.getEntity().entityDropItem(stack, 0);
				}
			}
			for(int i = 0; i < ((EntityVillager) e.getEntity()).getVillagerInventory().getSizeInventory(); i++) {
				ItemStack stack = ((EntityVillager) e.getEntity()).getVillagerInventory().getStackInSlot(i);
				if(stack != ItemStack.EMPTY) {
					if(!e.getEntity().getEntityWorld().isRemote)
						e.getEntity().entityDropItem(stack, 0);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onVillagerItemsDrop(LivingDropsEvent e) {
		if(e.getEntity() instanceof EntityVillager) {
			if(!e.getDrops().isEmpty()) {
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void openVillagerInv(EntityInteractSpecific event) {
		if(!event.getWorld().isRemote && event.getTarget() instanceof EntityVillager && event.getEntityPlayer().isSneaking())
		{
			try {
				CommonProxy.openVillagerGUI(event.getEntityPlayer(), event.getWorld(), (EntityVillager) event.getTarget());
			} catch(java.lang.Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addAiTasks(EntityVillager entity) {
		if(this.targetClass == null)
			this.targetClass = ObfuscationReflectionHelper.findField(EntityAIAvoidEntity.class, "field_181064_i");
		Collection<EntityAITaskEntry> toRem = new HashSet();
		for(EntityAITaskEntry ai : entity.tasks.taskEntries) {
			if(ai.action instanceof EntityAIAvoidEntity) {
				toRem.add(ai);
			}else if(ai.action instanceof EntityAIMoveIndoors) {
				toRem.add(ai);
			}else if(ai.action instanceof EntityAIWander) {
				toRem.add(ai);
			}
		}
		entity.tasks.taskEntries.removeAll(toRem);
		entity.tasks.addTask(9, new VillagerAIWanderAvoidWater(entity, .6D));
		entity.tasks.addTask(4, new VillagerAIAttackMelee(entity, .67D, false));
		entity.tasks.addTask(4, new VillagerAIShootRanged(entity, 10, 16, .5F));
		entity.tasks.addTask(1, new VillagerAICampaignEat(entity));
		entity.tasks.addTask(3, new VillagerAICampaignMove(entity, 1));
		entity.tasks.addTask(1, new VillagerAIGuard(entity, CommonProxy.MAX_GUARD_DIST, 6, 45));
		entity.tasks.addTask(3, new VillagerAIMoveIndoors(entity));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityEvoker.class, 12.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityVindicator.class, 8.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity(entity, EntityVex.class, 8.0F, 0.6D, 0.6D));
		if(ConfigHandlerVil.whiteListMobs) {
			for(String s : ConfigHandlerVil.attackableMobs) {
				entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, 
						EntityList.getClass(new ResourceLocation(s)), .67D));
			}
		} else  {
			entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntityMob.class, .67D));
			entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntitySlime.class, .67D));
			entity.targetTasks.addTask(3, new VillagerAIAttackNearestTarget(entity, EntityPlayer.class, .67D));
		}

	}
}
