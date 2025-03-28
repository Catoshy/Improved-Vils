package com.joshycode.improvedvils.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.joshycode.improvedvils.CommonProxy.ItemHolder;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.gui.EnlisteeContainer;
import com.joshycode.improvedvils.gui.GuiBatonTroopSettings;
import com.joshycode.improvedvils.gui.GuiVillagerRollList;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.BlankNotePacket.WarnNoRoom;
import com.joshycode.improvedvils.util.BatonDealMethods;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class VillagerListPacket implements IMessage {

	public Map<Integer, UUID> villagerIds;
	
	public VillagerListPacket() {}

	public VillagerListPacket(Map<Integer, UUID> villagerIds) 
	{
		this.villagerIds = villagerIds;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.villagerIds.size());
		this.villagerIds.keySet().forEach(entityId -> {
			UUID ID = this.villagerIds.get(entityId);
			buf.writeLong(ID.getMostSignificantBits());
			buf.writeLong(ID.getLeastSignificantBits());
			buf.writeInt(entityId);
		});
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		Map<Integer, UUID> idSet = new HashMap<Integer, UUID>();
		int size = buf.readInt();
		for(int i = 0; i < size; i++)
		{
			long mostSig = buf.readLong();
			long leastSig = buf.readLong();
			int entityId = buf.readInt();
			UUID ID = new UUID(mostSig, leastSig);
			idSet.put(entityId, ID);
		}
		if(ConfigHandler.debug)
			Log.info("ID set; %s", idSet);
		this.villagerIds = idSet;
	}
	
	public static class BatonBefolkPacket extends VillagerListPacket {
		
		Map<Integer, Tuple<Boolean[], Integer>> villagerInfo;
		
		public BatonBefolkPacket() {}

		public BatonBefolkPacket(Map<Integer, UUID> villagerIds, Map<Integer, Tuple<Boolean[], Integer>> villagerInfo) 
		{	
			super(villagerIds);
			this.villagerInfo = villagerInfo;
		}
		
		@Override
		public void toBytes(ByteBuf buf)
		{
			super.toBytes(buf);
			buf.writeInt(this.villagerInfo.size());
			this.villagerInfo.keySet().forEach(entityId -> {
				Boolean[] info = this.villagerInfo.get(entityId).getFirst();
				buf.writeBoolean(info[0]);
				buf.writeBoolean(info[1]);
				buf.writeBoolean(info[2]);
				buf.writeBoolean(info[3]);
				buf.writeBoolean(info[4]);
				buf.writeInt(this.villagerInfo.get(entityId).getSecond());
				buf.writeInt(entityId);
			});
		}
		
		@Override
		public void fromBytes(ByteBuf buf) 
		{
			super.fromBytes(buf);
			Map<Integer, Tuple<Boolean[], Integer>> infoSet = new HashMap<Integer, Tuple<Boolean[], Integer>>();
			int size = buf.readInt();
			for(int i = 0; i < size; i++)
			{
				Boolean[] info = new Boolean[5];
				info[0] = buf.readBoolean();
				info[1] = buf.readBoolean();
				info[2] = buf.readBoolean();
				info[3] = buf.readBoolean();
				info[4] = buf.readBoolean();
				int hungerDays = buf.readInt();
				int entityId = buf.readInt();
				infoSet.put(entityId, new Tuple<Boolean[], Integer>(info, hungerDays));
			}
			this.villagerInfo = infoSet;
		}
	
		public static class Handler implements IMessageHandler<BatonBefolkPacket, IMessage> {

			@Override
			public IMessage onMessage(BatonBefolkPacket message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					if(Minecraft.getMinecraft().currentScreen instanceof GuiBatonTroopSettings)
					{
						GuiVillagerRollList guiList = ((GuiBatonTroopSettings) Minecraft.getMinecraft().currentScreen).getRollList();
						Set<EnlisteeContainer> villagers = new HashSet<EnlisteeContainer>();
						guiList.getRoll().clear();
												
						message.villagerIds.keySet().forEach(entityId -> {
							
							EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
							if(villager != null)
								villagers.add(new EnlisteeContainer(guiList, villager, message.villagerInfo.get(entityId).getFirst(), message.villagerInfo.get(entityId).getSecond()));
							else
								villagers.add(new EnlisteeContainer(guiList, message.villagerIds.get(entityId)));
						});
						guiList.addContainers(villagers);
					}
				});
				return null;
			}

		}
	
	}
	
public static class BatonBefolkUpdatePacket extends BatonBefolkPacket {
				
		public BatonBefolkUpdatePacket() {}

		public BatonBefolkUpdatePacket(Map<Integer, UUID> villagerIds, Map<Integer, Tuple<Boolean[], Integer>> villagerInfo) {	super(villagerIds, villagerInfo);}
	
		public static class Handler implements IMessageHandler<BatonBefolkUpdatePacket, IMessage> {

			@Override
			public IMessage onMessage(BatonBefolkUpdatePacket message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					if(Minecraft.getMinecraft().currentScreen instanceof GuiBatonTroopSettings)
					{
						GuiVillagerRollList guiList = ((GuiBatonTroopSettings) Minecraft.getMinecraft().currentScreen).getRollList();
						Set<EnlisteeContainer> villagers = new HashSet<EnlisteeContainer>();

						message.villagerIds.keySet().forEach(entityId -> {
							
							EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
							if(villager != null)
								villagers.add(new EnlisteeContainer(((GuiBatonTroopSettings) Minecraft.getMinecraft().currentScreen).getRollList(), villager, message.villagerInfo.get(entityId).getFirst(), message.villagerInfo.get(entityId).getSecond()));
							else
								villagers.add(new EnlisteeContainer(((GuiBatonTroopSettings) Minecraft.getMinecraft().currentScreen).getRollList(), message.villagerIds.get(entityId)));
						});
						guiList.addContainers(villagers);
					}
				});
				return null;
			}

		}
	
	}
	
	public static class FollowVillagers extends VillagerListPacket {
		
		public FollowVillagers() {}

		public FollowVillagers(Map<Integer, UUID> villagerIds) {	super(villagerIds);}
	
		public static class Handler implements IMessageHandler<FollowVillagers, IMessage> {

			@Override
			public IMessage onMessage(FollowVillagers message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {

					message.villagerIds.keySet().forEach(entityId -> {
						
						EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
						EntityPlayer player = ImprovedVils.proxy.getPlayerEntity(ctx);
						if(villager != null && VillagerPlayerDealMethods.getPlayerFealty(player, villager))
						{
							villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerId(player.getUniqueID()).setGuardBlockPos(null).setCommBlock(null).setFollowing(true);
						}
					});
					NetWrapper.NETWORK.sendTo(new BatonBefolkUpdatePacket(message.villagerIds, BatonDealMethods.getVillagerCapabilityInfoAppendMap(message.villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx))),
							(EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx));
				});
				return null;
			}
			
		}
	}
	
public static class GuardVillagers extends VillagerListPacket {
		
		public GuardVillagers() {}

		public GuardVillagers(Map<Integer, UUID> villagerIds) {	super(villagerIds);}
	
		public static class Handler implements IMessageHandler<GuardVillagers, IMessage> {

			@Override
			public IMessage onMessage(GuardVillagers message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					
					message.villagerIds.keySet().forEach(entityId -> {
						
						EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
						EntityPlayer player = ImprovedVils.proxy.getPlayerEntity(ctx);
						if(villager != null && VillagerPlayerDealMethods.getPlayerFealty(player, villager))
							villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(false).setCommBlock(null).setGuardBlockPos(villager.getPosition());
					});
					NetWrapper.NETWORK.sendTo(new BatonBefolkUpdatePacket(message.villagerIds, BatonDealMethods.getVillagerCapabilityInfoAppendMap(message.villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx))),
							(EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx));
				});
				return null;
			}
			
		}
	}

	public static class StopVillagers extends VillagerListPacket {
		
		public StopVillagers() {}
	
		public StopVillagers(Map<Integer, UUID> villagerIds) {	super(villagerIds);}
	
		public static class Handler implements IMessageHandler<StopVillagers, IMessage> {
	
			@Override
			public IMessage onMessage(StopVillagers message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {

					message.villagerIds.keySet().forEach(entityId -> {
						
						EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
						EntityPlayer player = ImprovedVils.proxy.getPlayerEntity(ctx);
						if(villager != null && VillagerPlayerDealMethods.getPlayerFealty(player, villager))
							villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(false).setGuardBlockPos(null).setCommBlock(null).setTroopFaring(TroopCommands.NONE);
					});
					NetWrapper.NETWORK.sendTo(new BatonBefolkUpdatePacket(message.villagerIds, BatonDealMethods.getVillagerCapabilityInfoAppendMap(message.villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx))),
							(EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx));	
				});
				return null;
			}
			
		}
	}
	
	public static class SetVillagersDuty extends VillagerListPacket {
		
		public boolean duty;
	
		public SetVillagersDuty() {}
	
		public SetVillagersDuty(Map<Integer, UUID> villagerIds, boolean duty) 
		{	
			super(villagerIds);
			this.duty = duty;
		}
		
		@Override
		public void fromBytes(ByteBuf buf) 
		{
			super.fromBytes(buf);
			this.duty = buf.readBoolean();
		}
		
		@Override
		public void toBytes(ByteBuf buf)
		{
			super.toBytes(buf);
			buf.writeBoolean(this.duty);
		}
	
		public static class Handler implements IMessageHandler<SetVillagersDuty, IMessage> {
	
			@Override
			public IMessage onMessage(SetVillagersDuty message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {

					message.villagerIds.keySet().forEach(entityId -> {
						
						EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
						EntityPlayer player = ImprovedVils.proxy.getPlayerEntity(ctx);
						if(villager != null)
						{
							if(message.duty == false && villager.getWorld().getVillageCollection().getNearestVillage(villager.getPosition(), 0) == null)
								return;
							if(VillagerPlayerDealMethods.getPlayerFealty(player, villager))
								villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(false).setGuardBlockPos(null).setActiveDuty(message.duty);
						}
					});
					NetWrapper.NETWORK.sendTo(new BatonBefolkUpdatePacket(message.villagerIds, BatonDealMethods.getVillagerCapabilityInfoAppendMap(message.villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx))),
							(EntityPlayerMP) ImprovedVils.proxy.getPlayerEntity(ctx));
				});
				return null;
			}
		}
	}
	
	public static class MoveVillagersPlatoon extends VillagerListPacket {
		
		public int platoon;
	
		public MoveVillagersPlatoon() {}
	
		public MoveVillagersPlatoon(Map<Integer, UUID> villagerIds, int platoon) 
		{	
			super(villagerIds);
			this.platoon = platoon;
		}
		
		@Override
		public void fromBytes(ByteBuf buf) 
		{
			super.fromBytes(buf);
			this.platoon = buf.readInt();
		}
		
		@Override
		public void toBytes(ByteBuf buf)
		{
			super.toBytes(buf);
			buf.writeInt(this.platoon);
		}
	
		public static class Handler implements IMessageHandler<MoveVillagersPlatoon, IMessage> {
	
			@Override
			public IMessage onMessage(MoveVillagersPlatoon message, MessageContext ctx)
			{
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {
					
					EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
					
					if(serverPlayer.getHeldItem(EnumHand.MAIN_HAND).getItem() != ItemHolder.BATON && serverPlayer.getHeldItem(EnumHand.OFF_HAND).getItem() != ItemHolder.BATON)
						return;
					
					IMarshalsBatonCapability cap = serverPlayer.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
					int earlierPlatoon = cap.selectedUnit();
					cap.setPlatoon(message.platoon /10, message.platoon % 10);
					int size = cap.getVillagersSelected().size();
					cap.setPlatoon(earlierPlatoon / 10, earlierPlatoon % 10);
					
					if(size + message.villagerIds.size() >= 30)
					{
						NetWrapper.NETWORK.sendTo(new WarnNoRoom(), serverPlayer);
					}
					else
					{
						message.villagerIds.values().forEach(id -> {
							cap.removeVillager(id);
							cap.addVillager(id, message.platoon /10, message.platoon % 10);
						});
						
						message.villagerIds.keySet().forEach(entityId -> {
							EntityVillager villager = (EntityVillager) ImprovedVils.proxy.getWorld(ctx).getEntityByID(entityId);
							if(villager != null)
							{
								villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null)
									.setFoodStore(cap.getPlatoonFoodStore(message.platoon /10, message.platoon % 10))
									.setKitStore(cap.getPlatoonKitStore(message.platoon /10, message.platoon % 10));
							}
						});
						
						Map<Integer, UUID> villagerIds = BatonDealMethods.getEntityIDsFromBatonPlatoon(serverPlayer);
						Map<Integer, Tuple<Boolean[], Integer>> villagerInfo = BatonDealMethods.getVillagerCapabilityInfoAppendMap(villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx));
						NetWrapper.NETWORK.sendTo(new BatonBefolkPacket(villagerIds, villagerInfo), serverPlayer);
					}

				});
				return null;
			}
		}
	}
	
	public static class DismissVillagers extends VillagerListPacket {
		
		public DismissVillagers() {}
		
		public DismissVillagers(Map<Integer, UUID> entityIDs) { super(entityIDs); }

		public static class Handler implements IMessageHandler<DismissVillagers, IMessage> {

			@Override
			public IMessage onMessage(DismissVillagers message, MessageContext ctx) {
			
				ImprovedVils.proxy.getListener(ctx).addScheduledTask(() -> {

					EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
					
					if(serverPlayer.getHeldItem(EnumHand.MAIN_HAND).getItem() != ItemHolder.BATON && serverPlayer.getHeldItem(EnumHand.OFF_HAND).getItem() != ItemHolder.BATON)
						return;
					
					for(UUID id : message.villagerIds.values())
						serverPlayer.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null).removeVillager(id);
					
					message.villagerIds.keySet().forEach(id -> {
						Entity e = ImprovedVils.proxy.getWorld(ctx).getEntityByID(id);
						if(e != null)
							e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFoodStore(null).setKitStore(null);
					});
					Map<Integer, UUID> villagerIds = BatonDealMethods.getEntityIDsFromBatonPlatoon(serverPlayer);
					Map<Integer, Tuple<Boolean[], Integer>> villagerInfo = BatonDealMethods.getVillagerCapabilityInfoAppendMap(villagerIds.keySet(), ImprovedVils.proxy.getWorld(ctx));
					NetWrapper.NETWORK.sendTo(new BatonBefolkPacket(villagerIds, villagerInfo), serverPlayer);
				});
				return null;
			}
		}
	}
}
