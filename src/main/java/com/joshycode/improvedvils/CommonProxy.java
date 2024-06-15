package com.joshycode.improvedvils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import com.joshycode.improvedvils.capabilities.CapabilityStorage;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.ImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability.Provisions;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.capabilities.village.VillageCapability;
import com.joshycode.improvedvils.entity.EntityBullet;
import com.joshycode.improvedvils.gui.VilGuiHandler;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.item.ItemMarshalsBaton;
import com.joshycode.improvedvils.network.BatonSelectData;
import com.joshycode.improvedvils.network.BatonSelectData.BatonSelectServerData;
import com.joshycode.improvedvils.network.BlankNotePacket;
import com.joshycode.improvedvils.network.BlankNotePacket.WarnNoRoom;
import com.joshycode.improvedvils.network.GunFiredPacket;
import com.joshycode.improvedvils.network.MarshalKeyEvent;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.OpenClientGui;
import com.joshycode.improvedvils.network.VilCommandPacket;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilFollowPacket;
import com.joshycode.improvedvils.network.VilFoodStorePacket;
import com.joshycode.improvedvils.network.VilGuardPacket;
import com.joshycode.improvedvils.network.VilGuiQuery;
import com.joshycode.improvedvils.network.VilKitStorePacket;
import com.joshycode.improvedvils.network.VilStateQuery;
import com.joshycode.improvedvils.network.VilStateUpdate;
import com.joshycode.improvedvils.network.VillagerListPacket.BatonBefolkPacket;
import com.joshycode.improvedvils.network.VillagerListPacket.BatonBefolkUpdatePacket;
import com.joshycode.improvedvils.network.VillagerListPacket.DismissVillagers;
import com.joshycode.improvedvils.network.VillagerListPacket.FollowVillagers;
import com.joshycode.improvedvils.network.VillagerListPacket.GuardVillagers;
import com.joshycode.improvedvils.network.VillagerListPacket.MoveVillagersPlatoon;
import com.joshycode.improvedvils.network.VillagerListPacket.SetVillagersDuty;
import com.joshycode.improvedvils.network.VillagerListPacket.StopVillagers;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public class CommonProxy {

	@ObjectHolder(ImprovedVils.MODID)
	public static class ItemHolder {

		@ObjectHolder("draft_writ")
		public static final Item DRAFT_WRIT = null;

		@ObjectHolder("marshals_baton")
		public static final ItemMarshalsBaton BATON = null;

	}

	public static final double GUARD_MAX_PATH = 8;
	public static final double GUARD_MAX_PATH_SQ = GUARD_MAX_PATH * GUARD_MAX_PATH;
	public static final int MAX_GUARD_DIST = 256;
	public static final int GUARD_IGNORE_LIMIT = 4096;
	public static final int BATON_GUI_ID = 101;
	public static final int VIL_GUI_ID = 100;
	public static final int PLATOON_UP = 0;
	public static final int PLATOON_DOWN = 1;
	public static final int COMPANY_UP = 2;
	public static final int COMPANY_DOWN = 3;
	public static final int BATON_GUI = 4;

	@SuppressWarnings("rawtypes")
	public static final HashSet<Class> TARGETS = new HashSet<>();
	@SuppressWarnings("rawtypes")
	public static final HashSet<Class> RANGE_BLACKLIST = new HashSet<>();

	public void preInit() throws IOException
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(ImprovedVils.instance, new VilGuiHandler());
		registerEntities();
		registerCapabilities();
		registerPackets();
		ConfigHandler.load(LoadState.PREINIT);
	}
	
	public void registerEntities() 
	{
		EntityRegistry.registerModEntity(ImprovedVils.location("bullet"), EntityBullet.class, "bullet", 1, ImprovedVils.instance, 124, 1, true);
	}

	public void init() {}

	public void postInit() throws IOException
	{
		ConfigHandler.load(LoadState.POSTINIT);
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerCapabilities()
    {
        CapabilityManager.INSTANCE.register(
        		IImprovedVilCapability.class,
        		new CapabilityStorage(),
                new Callable<IImprovedVilCapability>() {
        			@Override
        			public IImprovedVilCapability call() throws Exception {
        				return new ImprovedVilCapability();
        			}
        		}
        );

        CapabilityManager.INSTANCE.register(
        		IMarshalsBatonCapability.class,
        		new CapabilityStorage(),
        		new Callable<IMarshalsBatonCapability>() {
        			@Override
        			public MarshalsBatonCapability call() throws Exception {
        				return new MarshalsBatonCapability();
        			}
        		}
        );

        CapabilityManager.INSTANCE.register(
        		IVillageCapability.class,
        		new CapabilityStorage(),
        		new Callable<IVillageCapability>() {
        			@Override
        			public VillageCapability call() throws Exception {
        				return new VillageCapability();
        			}
        		}
        );
    }

    public void registerPackets()
    {
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ServerHandler.class, VilEnlistPacket.class, 0, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ClientHandler.class, VilEnlistPacket.class, 1, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilCommandPacket.Handler.class, VilCommandPacket.class, 2, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilGuardPacket.ServerHandler.class, VilGuardPacket.class, 3, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateQuery.Handler.class, VilStateQuery.class, 4, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateUpdate.ClientHandler.class, VilStateUpdate.class, 5, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilFollowPacket.Handler.class, VilFollowPacket.class, 6, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilFollowPacket.VilDutyPacket.Handler.class, VilFollowPacket.VilDutyPacket.class, 7, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilFoodStorePacket.Handler.class, VilFoodStorePacket.class, 8, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilKitStorePacket.Handler.class, VilKitStorePacket.class, 9, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilGuiQuery.Handler.class, VilGuiQuery.class, 10, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(MarshalKeyEvent.Handler.class, MarshalKeyEvent.class, 11, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(BatonSelectData.Handler.class, BatonSelectData.class, 12, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(BatonSelectServerData.Handler.class, BatonSelectServerData.class, 13, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(GunFiredPacket.Handler.class, GunFiredPacket.class, 14, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(OpenClientGui.Handler.class, OpenClientGui.class, 15, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(BatonBefolkPacket.Handler.class, BatonBefolkPacket.class, 16, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(BatonBefolkUpdatePacket.Handler.class, BatonBefolkUpdatePacket.class, 17, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(BlankNotePacket.BatonBefolkQuery.Handler.class, BlankNotePacket.BatonBefolkQuery.class, 18, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(FollowVillagers.Handler.class, FollowVillagers.class, 19, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(GuardVillagers.Handler.class, GuardVillagers.class, 20, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(MoveVillagersPlatoon.Handler.class, MoveVillagersPlatoon.class, 21, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(SetVillagersDuty.Handler.class, SetVillagersDuty.class, 22, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(StopVillagers.Handler.class, StopVillagers.class, 23, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(WarnNoRoom.Handler.class, WarnNoRoom.class, 24, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(DismissVillagers.Handler.class, DismissVillagers.class, 25, Side.SERVER);
    }

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> e)
	{
		e.getRegistry().registerAll(new  Item().setRegistryName("draft_writ")
				.setUnlocalizedName(ImprovedVils.MODID + ".draft_writ")
				.setCreativeTab(CreativeTabs.COMBAT),

									new ItemMarshalsBaton().setRegistryName("marshals_baton")
				.setUnlocalizedName(ImprovedVils.MODID + ".marshals_baton")
				.setCreativeTab(CreativeTabs.COMBAT));
	}

	public enum LoadState {
		PREINIT,  SYNC, POSTINIT
	}

	public IThreadListener getListener(MessageContext ctx)
	{
		return (WorldServer) ctx.getServerHandler().player.world;
	}

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public World getWorld(MessageContext ctx) {
		return ctx.getServerHandler().player.world;
	}
	
	public void setHUDinfo(int platoon) {}

	public int timeAgoSinceHudInfo() {return 24000; }

	public int getSelectedUnit() {return 0; }
	
	public void setProvisioningPlatoon(int platoon, Provisions kit) {}
	
	public int getProvisioningUnit() {return -1; }
	
	public Provisions getStuff() {return null; }
	
	//TODO
	public void syncVillagerToServer(int id) {}

	/**
	 * Takes a set of unique IDs for entities and searches the loaded world for them, can be intensive- ensure use case is sparing
	 * @param ids Set of Entity UUIDs to look for
	 * @param world
	 * @return a Set of the loaded Entities.
	 */
	public static synchronized Set<Entity> getEntitiesByUUID(Set<UUID> ids, World world)
	{
		Set<Entity> applicable = new HashSet<Entity>();
		if(world.getLoadedEntityList() != null && world.getLoadedEntityList().size() != 0)
		{
			List<Entity> list = new CopyOnWriteArrayList <> (world.getLoadedEntityList());
			for (Entity e : list)
			{
				if(world.getChunkFromBlockCoords(e.getPosition()).isLoaded())
				{
					 if(ids.contains(e.getUniqueID()))
					 {
						 applicable.add(e);
					 }
				}
			}
		}
		return applicable;
	}
}
