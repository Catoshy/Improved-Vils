package com.joshycode.improvedvils;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;

import com.joshycode.improvedvils.capabilities.CapabilityStorage;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.ImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability;
import com.joshycode.improvedvils.gui.VilGuiHandler;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.ConfigHandlerVil;
import com.joshycode.improvedvils.item.ItemMarshalsBaton;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilCommandPacket;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilFollowPacket;
import com.joshycode.improvedvils.network.VilFoodStorePacket;
import com.joshycode.improvedvils.network.VilGuardPacket;
import com.joshycode.improvedvils.network.VilGuiQuery;
import com.joshycode.improvedvils.network.VilStateQuery;
import com.joshycode.improvedvils.network.VilStateUpdate;
import com.joshycode.improvedvils.util.InventoryUtil;
import com.joshycode.improvedvils.util.Pair;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
/*import techguns.TGSounds;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.items.guns.ProjectileSelector;
import techguns.items.guns.ammo.AmmoTypes;*/

@Mod.EventBusSubscriber
public abstract class CommonProxy {
	
	@ObjectHolder(ImprovedVils.MODID)
	public static class ItemHolder {
		
		@ObjectHolder("draft_writ")
		public static final Item DRAFT_WRIT = null;
		
		@ObjectHolder("marshals_baton")
		public static final ItemMarshalsBaton BATON = null;
		
	}
	
	public static final double GUARD_MAX_PATH = 576;
	public static final int MAX_GUARD_DIST = 256;
	public static final int GUARD_IGNORE_LIMIT = 4096;
	
	@SuppressWarnings("rawtypes")
	public static final HashSet<Class> TARGETS = new HashSet<Class>();
		
	public void preInit() throws IOException 
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(ImprovedVils.instance, new VilGuiHandler());
		registerCapabilities();
		registerPackets();
		ConfigHandlerVil.load(LoadState.PREINIT);
	}
	
	public void postInit() throws IOException 
	{
		ConfigHandlerVil.load(LoadState.POSTINIT);
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
    }
    
    public void registerPackets() 
    {
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ServerHandler.class, VilEnlistPacket.class, 0, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ClientHandler.class, VilEnlistPacket.class, 1, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilCommandPacket.Handler.class, VilCommandPacket.class, 2, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilGuardPacket.ServerHandler.class, VilGuardPacket.class, 3, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateQuery.Handler.class, VilStateQuery.class, 5, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateUpdate.ClientHandler.class, VilStateUpdate.class, 6, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilFollowPacket.Handler.class, VilFollowPacket.class, 7, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilFoodStorePacket.Handler.class, VilFoodStorePacket.class, 8, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilGuiQuery.Handler.class, VilGuiQuery.class, 9, Side.SERVER);
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
}
