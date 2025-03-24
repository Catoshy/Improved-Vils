package com.joshycode.improvedvils.patch;

import java.lang.reflect.Field;

import com.flemmli97.improvedmobs.entity.ai.NewWalkNodeProcessor;
import com.flemmli97.improvedmobs.handler.EventHandlerAI;
import com.flemmli97.tenshilib.common.events.PathFindInitEvent;
import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ImprovedMobsPatch {

	private Field entity, nodeProcessor;
	
	@SubscribeEvent
	public void modifyPathfinder(PathFindInitEvent event)
	{
		if(this.entity == null)
			this.entity = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_75515_a");
		if(this.nodeProcessor == null)
			this.nodeProcessor = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_179695_a");
		if(event.getNavigator() instanceof PathNavigateGround)
		{
			EntityLiving entity = ReflectionUtils.getFieldValue(this.entity, event.getNavigator());
			NewWalkNodeProcessor walkNode;
			if(entity instanceof EntityVillager) 
				walkNode = new WetWalkNodeProcessor();
			else
				walkNode = new NewWalkNodeProcessor();
			walkNode.setBreakBlocks(entity.getTags().contains(EventHandlerAI.breaker));
			walkNode.setCanEnterDoors(true);
			walkNode.setCanOpenDoors(true);
			ReflectionUtils.setFieldValue(this.nodeProcessor, event.getNavigator(), walkNode);
			event.setPathFinder(new PathFinder(walkNode));
		}
	}
}
