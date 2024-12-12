package com.joshycode.improvedvils.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.VilMethods;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.Provisions;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedvils.entity.ai.VillagerAIAttackNearestTarget;
import com.joshycode.improvedvils.entity.ai.VillagerAIAvoidEntity;
import com.joshycode.improvedvils.entity.ai.VillagerAIBayonetCharge;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignEat;
import com.joshycode.improvedvils.entity.ai.VillagerAICampaignMove;
import com.joshycode.improvedvils.entity.ai.VillagerAICollectKit;
import com.joshycode.improvedvils.entity.ai.VillagerAIDrinkPotion;
import com.joshycode.improvedvils.entity.ai.VillagerAIEatHeal;
import com.joshycode.improvedvils.entity.ai.VillagerAIFollow;
import com.joshycode.improvedvils.entity.ai.VillagerAIGuard;
import com.joshycode.improvedvils.entity.ai.VillagerAIHandlePlayers;
import com.joshycode.improvedvils.entity.ai.VillagerAIHurtByTarget;
import com.joshycode.improvedvils.entity.ai.VillagerAIMate;
import com.joshycode.improvedvils.entity.ai.VillagerAIMoveIndoors;
import com.joshycode.improvedvils.entity.ai.VillagerAIMoveTowardsRestriction;
import com.joshycode.improvedvils.entity.ai.VillagerAIRefillFood;
import com.joshycode.improvedvils.entity.ai.VillagerAIRestrictOpenDoor;
import com.joshycode.improvedvils.entity.ai.VillagerAIShootRanged;
import com.joshycode.improvedvils.entity.ai.VillagerAISwimming;
import com.joshycode.improvedvils.entity.ai.VillagerAIVillagerInteract;
import com.joshycode.improvedvils.entity.ai.VillagerAIWanderAvoidWater;
import com.joshycode.improvedvils.event.ChildGrowEvent;
import com.joshycode.improvedvils.gui.GuiVillagerArm;
import com.joshycode.improvedvils.handler.VillagerPredicate.EnemyPlayerAttackPredicate;
import com.joshycode.improvedvils.handler.VillagerPredicate.EnemyVillagerAttackPredicate;
import com.joshycode.improvedvils.handler.VillagerPredicate.FriendlyFireVillagerPredicate;
import com.joshycode.improvedvils.util.VilAttributes;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.Village;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
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
		if(!(e.getEntity() instanceof EntityVillager)) return;
		
		VilAttributes.applyConstr((EntityVillager) e.getEntity());
	}

	@SubscribeEvent
	public void onVillagerJoinWorld(EntityJoinWorldEvent e)
	{
		if(!(e.getEntity() instanceof EntityVillager)) return;

		EntityVillager entity = (EntityVillager) e.getEntity();
		
		if(entity.isChild()) return;
		
		VilAttributes.apply(entity);
		addAiTasks(entity);
		
		if(ConfigHandler.villagerHealth != 20f && !entity.getEntityData().getBoolean(modify_villager))
		{
			entity.setHealth(ConfigHandler.villagerHealth);
		}
		entity.getEntityData().setBoolean(modify_villager, true);
	}

	@SubscribeEvent
	public void onVillagerGrow(ChildGrowEvent e)
	{
		if(!(e.getEntity() instanceof EntityVillager)) return;
			
		addAiTasks((EntityVillager) e.getEntity());
		VilAttributes.apply((EntityVillager) e.getEntity());
		childGrown((EntityVillager) e.getEntity());
		if(ConfigHandler.villagerHealth != 20f)
		{
			((EntityVillager) e.getEntity()).setHealth(ConfigHandler.villagerHealth);
		}
		((EntityVillager) e.getEntity()).getEntityData().setBoolean(modify_villager, true);
	}
	
	private void childGrown(EntityVillager entity)
	{
		if(entity.getEntityWorld().isRemote) return;
		
		IImprovedVilCapability vilCap = entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
		Village village = entity.getEntityWorld().getVillageCollection().getNearestVillage(new BlockPos(entity), 0);
		if(vilCap.getHomeVillageID() != null || village == null) return;
		
		IVillageCapability villageCap =  village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
		vilCap.setHomeVillageID(villageCap.getUUID());
		if(/*vilCap.getTeam() != null &&*/ entity.getEntityWorld().getScoreboard().getTeam(villageCap.getTeam()) != null)
			VilMethods.setTeam(entity, villageCap.getTeam());
	}

	@SubscribeEvent
	public void onAgeableUpdate(LivingUpdateEvent e)
	{
		if(!(e.getEntity() instanceof EntityAgeable)) return;
			
		EntityAgeable ageable = (EntityAgeable) e.getEntity();
		if(ageable.getGrowingAge() == -1)
		{
			MinecraftForge.EVENT_BUS.post(new ChildGrowEvent(ageable));
		}
	}

	@SubscribeEvent
	public void onVillagerAttacked(LivingAttackEvent e)
	{
		if(e.getEntityLiving() instanceof EntityVillager)
		{
			if(e.getEntityLiving().getHeldItemOffhand().getItemUseAction() == EnumAction.BLOCK)
				if(e.getAmount() > e.getEntityLiving().getMaxHealth() * .75D || this.rand.nextFloat() + Float.MIN_VALUE < ConfigHandler.blockChance)
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
			{
				if(ConfigHandler.debug)
					Log.info("EntityMob, or Target type entity killed by player %s", e.getEntityLiving().getAttackingEntity());
				handleVillageGoodReputation(e.getEntityLiving(), ((EntityPlayer)e.getEntityLiving().getAttackingEntity()), 1);
			}
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
		if(ConfigHandler.debug)
			Log.info("handleVillageBadReputation");
		Village village = villager.getEntityWorld().getVillageCollection().getNearestVillage(villager.getPosition(), 0);
		if(village != null)
		{
			IVillageCapability villageCap = village.getCapability(CapabilityHandler.VILLAGE_CAPABILITY, null);
			IImprovedVilCapability vilCap = villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			if(ConfigHandler.debug)
				Log.info("handleVillageBadReputation: village not null \n" +
			"Villager Team: " + vilCap.getTeam() + "\n" +
			"Village Team: " + villageCap.getTeam());
			
			boolean inHomeVillage = VillagerPlayerDealMethods.isVillagerInHomeVillage(villageCap.getUUID(), vilCap.getHomeVillageID());
			if((villageCap.getTeam().isEmpty() && inHomeVillage) || vilCap.getTeam().equals(villageCap.getTeam())
					||	vilCap.getTeam().isEmpty() || villager.isChild())
			{
				if(ConfigHandler.debug)
					Log.info("unjustified attack: costing reputation:" + reputationCost);
				VillagerPlayerDealMethods.villageBadReputationChange(villager.getEntityWorld(), village, attackingPlayer);
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
			if(ConfigHandler.debug)
				Log.info("found village, will increase player reputation. Reputation is %s", village.getPlayerReputation(player.getUniqueID()));
			VillagerPlayerDealMethods.villageGoodReputationChange(entity.getEntityWorld(), village, player);
		}
	}

	@SubscribeEvent
	public void openVillagerInv(EntityInteractSpecific event)
	{
		if(event.getSide() == Side.SERVER && event.getTarget() instanceof EntityVillager && event.getEntityPlayer().isSneaking())
		{
			//World world = null;
			//world.init();
			//ClientProxy.openGuiForPlayerIfOK(event.getTarget().getEntityId());
			((IThreadListener) event.getWorld()).addScheduledTask(new VilPlayerDeal(event.getTarget().getEntityId(), (EntityPlayerMP) event.getEntityPlayer(), event.getWorld()));
			event.setCanceled(true);
			event.setCancellationResult(EnumActionResult.SUCCESS);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onVillagerInteract(GuiOpenEvent event)
	{
		if(Minecraft.getMinecraft().currentScreen instanceof GuiVillagerArm && !(event.getGui() instanceof GuiVillagerArm))
		{
			ImprovedVils.proxy.closeVillagerGUI(((GuiVillagerArm) Minecraft.getMinecraft().currentScreen).getVilId());
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGH, receiveCanceled=true)
	public void onKeyPressed(KeyInputEvent event)
	{
		//TODO off hand should also work
		ItemStack heldItem = Minecraft.getMinecraft().getRenderViewEntity().getHeldEquipment().iterator().next();
		if(!heldItem.getItem().equals(CommonProxy.ItemHolder.BATON)) return;
		
		KeyBinding[] keyBindings = ClientProxy.keyBindings;
		
		if(keyBindings[0].isPressed())
		{
			ImprovedVils.proxy.marshalKeyEvent(0);
		}
		else if(keyBindings[1].isPressed())
		{
			ImprovedVils.proxy.marshalKeyEvent(1);
		}
		else if(keyBindings[2].isPressed())
		{
			ImprovedVils.proxy.marshalKeyEvent(2);
		}
		else if(keyBindings[3].isPressed())
		{
			ImprovedVils.proxy.marshalKeyEvent(3);
		}
		else if(keyBindings[4].isPressed())
		{
			ImprovedVils.proxy.marshalKeyEvent(4);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void showBatonDoing(RenderGameOverlayEvent.Post e) 
	{
		if(e.isCancelable() || e.getType() != ElementType.TEXT || Minecraft.getMinecraft().gameSettings.chatVisibility == EntityPlayer.EnumChatVisibility.HIDDEN) return;
		
		float f = Minecraft.getMinecraft().gameSettings.chatOpacity * 0.9F + 0.1F;
		GlStateManager.pushMatrix();
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		
		ScaledResolution res = e.getResolution();
		int guiX = res.getScaledWidth() / 2 - 15;
		int guiY = res.getScaledHeight() - 50;
		
		TroopCommands command = ImprovedVils.proxy.getCommand();
		ItemStack heldItem = Minecraft.getMinecraft().getRenderViewEntity().getHeldEquipment().iterator().next();
		if(heldItem.getItem().equals(CommonProxy.ItemHolder.BATON))
		{
			if(ImprovedVils.proxy.getProvisioningUnit() != -1)
			{
				Provisions stuff = ImprovedVils.proxy.getStuff();
				String stuffWrit = TextFormatting.WHITE + "Select a \"" + stuff.name() + "\" chest.";
				int stuffWritWidth = font.getStringWidth(stuffWrit);
				font.drawString(stuffWrit, guiX - (stuffWritWidth / 2), guiY, 16777215);
	
			}
			if(command.getID() != TroopCommands.NONE.getID())
			{
				font.drawString(TextFormatting.WHITE + "Commanding a " + command.name() + "!", guiX, guiY, 16777215);
			}
		}
		else
		{
			ImprovedVils.proxy.setTroopCommand(TroopCommands.NONE);
		}
		
		int timeAgo = ImprovedVils.proxy.timeAgoSinceHudInfo();
		double alphaStrength = (double)timeAgo / 200.0D;
		alphaStrength = 1.0D - alphaStrength;
		alphaStrength = alphaStrength * 10.0D;
		alphaStrength = MathHelper.clamp(alphaStrength, 0D, 1D);
		alphaStrength = alphaStrength * alphaStrength;
		int alpha = (int)(255D * alphaStrength);
		alpha = (int)((float)alpha * f);
		
		if(alpha > 3)
		{
			int unit = ImprovedVils.proxy.getSelectedUnit();
			int platoon = unit % 10;
			int company = unit / 10;
			String platS = "Selected Platoon: " + String.format(java.util.Locale.US, "%d", platoon + 1);
			String compS = "Selected Company: " + String.format(java.util.Locale.US, "%d", company + 1);
			int platWidth = font.getStringWidth(platS);
			int compWidth = font.getStringWidth(compS);
            GlStateManager.enableBlend();
			font.drawStringWithShadow(compS, guiX + 12 - (compWidth / 2), guiY, 16777215 + (alpha << 24));
			font.drawStringWithShadow(platS, guiX + 12 - (platWidth / 2), guiY + 9, 16777215 + (alpha << 24));
			GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
		}
		GlStateManager.popMatrix();
	}

	public void addAiTasks(EntityVillager entity)
	{
		Collection<EntityAITaskEntry> toRem = new HashSet<EntityAITaskEntry>();
		for(EntityAITaskEntry ai : entity.tasks.taskEntries)
		{
			if(ai.action instanceof EntityAIAvoidEntity)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIMoveIndoors)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIRestrictOpenDoor)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIWander)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIMoveTowardsRestriction)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIVillagerMate)
				toRem.add(ai);
			else if(ai.action instanceof EntityAIVillagerInteract)
				toRem.add(ai);
			else if(ai.action instanceof EntityAISwimming)
				toRem.add(ai);
		}
		entity.tasks.taskEntries.removeAll(toRem);
		
		VillagerAICampaignEat aiCEat = new VillagerAICampaignEat(entity, ConfigHandler.dailyBread);
		entity.getVillagerInventory().addInventoryChangeListener(aiCEat);
		entity.tasks.addTask(9, new VillagerAIWanderAvoidWater(entity, .6D));
		entity.tasks.addTask(9, new VillagerAIVillagerInteract(entity));
		entity.tasks.addTask(6, new VillagerAIRefillFood(entity, 4));
		entity.tasks.addTask(3, new VillagerAICollectKit(entity, 4));
		entity.tasks.addTask(4, new VillagerAIMate(entity));
		entity.tasks.addTask(6, new VillagerAIMoveTowardsRestriction(entity, 0.6D));
		entity.tasks.addTask(6, new VillagerAIFollow(entity, .67D, 6.0F, ConfigHandler.followRangeNormal));
		entity.tasks.addTask(4, new VillagerAIMoveIndoors(entity));
		entity.tasks.addTask(4, new VillagerAIDrinkPotion(entity));
		entity.tasks.addTask(4, new VillagerAIAttackMelee(entity, /*.55D*/0D, true));
		entity.tasks.addTask(5, new VillagerAIShootRanged(entity, 10, 32, .5F, new FriendlyFireVillagerPredicate<Entity>(entity)));
		entity.tasks.addTask(0, new VillagerAISwimming(entity));
		entity.tasks.addTask(0, aiCEat);
		entity.tasks.addTask(0, new VillagerAIEatHeal(entity));
		entity.tasks.addTask(0, new VillagerAIHandlePlayers(entity));
		//entity.tasks.addTask(0, new VillagerAIClimbLadder(entity));
		entity.tasks.addTask(2, new VillagerAICampaignMove(entity, 64));
		entity.tasks.addTask(3, new VillagerAIRestrictOpenDoor(entity));
		entity.tasks.addTask(1, new VillagerAIGuard(entity, CommonProxy.MAX_GUARD_DIST, 4, 64));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity<EntityZombie>(entity, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity<EntityEvoker>(entity, EntityEvoker.class, 12.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity<EntityVindicator>(entity, EntityVindicator.class, 8.0F, 0.8D, 0.8D));
		entity.tasks.addTask(2, new VillagerAIAvoidEntity<EntityVex>(entity, EntityVex.class, 8.0F, 0.6D, 0.6D));
		
		Map<Class<? extends EntityLivingBase>, Predicate<? super EntityLivingBase>> targets = new HashMap<>();
		
		for(Class<? extends EntityLivingBase> c : CommonProxy.TARGETS)
				targets.put(c, null);
		
		targets.put(EntityVillager.class, new EnemyVillagerAttackPredicate<EntityLivingBase>(entity));
		targets.put(EntityPlayer.class, new EnemyPlayerAttackPredicate<EntityLivingBase>(entity));
		
		entity.tasks.addTask(1, new VillagerAIBayonetCharge(entity, new HashSet<Predicate<? super EntityLivingBase>>(targets.values()), 4, 32));
		entity.targetTasks.addTask(2, new VillagerAIAttackNearestTarget(entity, targets, true, ConfigHandler.targetDistance));
		entity.targetTasks.addTask(1, new VillagerAIHurtByTarget<EntityLivingBase>(entity));
	}
}
