package com.joshycode.improvedmobs.handler;

import java.lang.reflect.Field;
import java.util.UUID;

import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;
import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.entity.EntityNoMan;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackNearestTarget;
import com.joshycode.improvedmobs.entity.ai.VillagerAIShootRanged;
import com.joshycode.improvedmobs.event.ChildGrowEvent;
import com.joshycode.improvedmobs.util.VilAttributes;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandlerVilAI {

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
	
	public void addAiTasks(EntityVillager entity) {
		if(this.targetClass == null)
			this.targetClass = ObfuscationReflectionHelper.findField(EntityAIAvoidEntity.class, "field_181064_i");
		entity.tasks.taskEntries.forEach(t -> {
			if(t.action instanceof EntityAIAvoidEntity) {
				ReflectionUtils.setFieldValue(this.targetClass, t.action, EntityNoMan.class);
			}
		});
		entity.tasks.addTask(4, new VillagerAIAttackMelee(entity, 1.0, false));
		entity.tasks.addTask(4, new VillagerAIShootRanged(entity, 10, 16, .5F));
		entity.targetTasks.addTask(1, new VillagerAIAttackNearestTarget(entity, EntityPlayer.class));
	}
}
