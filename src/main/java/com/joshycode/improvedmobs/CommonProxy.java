package com.joshycode.improvedmobs;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Callable;

import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;
import com.joshycode.improvedmobs.capabilities.CapabilityStorage;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.capabilities.entity.ImprovedVilCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.MarshalsBatonCapability;
import com.joshycode.improvedmobs.gui.VilGuiHandler;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import com.joshycode.improvedmobs.item.ItemMarshalsBaton;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilCommandPacket;
import com.joshycode.improvedmobs.network.VilEnlistPacket;
import com.joshycode.improvedmobs.network.VilFollowPacket;
import com.joshycode.improvedmobs.network.VilGuardPacket;
import com.joshycode.improvedmobs.network.VilStateQuery;
import com.joshycode.improvedmobs.util.InventoryUtil;
import com.joshycode.improvedmobs.util.Pair;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
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

	public static final int MAX_GUARD_DIST = 256;
	public static final int GUARD_IGNORE_LIMIT = 16384;
	@SuppressWarnings("rawtypes")
	public static final HashSet<Class> TARGETS = new HashSet<Class>();
	public static final double GUARD_MAX_PATH = 576;
		
	public void preInit() throws IOException {
		NetworkRegistry.INSTANCE.registerGuiHandler(ImprovedVils.instance, new VilGuiHandler());
		registerCapabilities();
		registerPackets();
		ConfigHandlerVil.load(LoadState.PREINIT);
	}
	
	public void postInit() throws IOException {
		ConfigHandlerVil.load(LoadState.POSTINIT);
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerCapabilities() {
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
    
    public void registerPackets() {
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ServerHandler.class, VilEnlistPacket.class, 0, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.ClientHandler.class, VilEnlistPacket.class, 1, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilCommandPacket.Handler.class, VilCommandPacket.class, 2, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilGuardPacket.ServerHandler.class, VilGuardPacket.class, 3, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateQuery.ServerHandler.class, VilStateQuery.class, 5, Side.SERVER);
    	NetWrapper.NETWORK.registerMessage(VilStateQuery.ClientHandler.class, VilStateQuery.class, 6, Side.CLIENT);
    	NetWrapper.NETWORK.registerMessage(VilFollowPacket.Handler.class, VilFollowPacket.class, 7, Side.SERVER);
    }
    
	public static void openVillagerGUI(EntityPlayer player, World world, EntityVillager entityIn) {
		if(!entityIn.isChild()) {
			setPlayerId(player, entityIn);
			int intA = -2;
			int intB = 0;
			ItemStack stack = InventoryUtil.get1StackByItem(player.inventory, CommonProxy.ItemHolder.BATON);
			if(stack != null) {
				IMarshalsBatonCapability cap = stack.getCapability(CapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				if(cap != null) {
					System.out.println("openVillagerGUI -- capacity != null");
					Pair<Integer, Integer> p = cap.getVillagerPlace(entityIn.getUniqueID());
					intA = -1; /* Has the Baton but is not Enlisted*/
					if(p != null) {
						intA = p.a;
						intB = p.b % 10;
					}
				}
			} else {
				intA = -2; /*Does not have baton, cannot be Enlisted*/
			}
			System.out.println("intA = " + intA);
			player.openGui(ImprovedVils.instance, 100, world, entityIn.getEntityId(), intA, intB);
		}
	}
	
	private static void setPlayerId(EntityPlayer player, EntityVillager entityIn) {
		try {
			entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerId(player.getUniqueID());
		} catch (NullPointerException ex) {}
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> e) {
		System.out.println("registerItems(RegistryEvent.Register<Item> e)");
		e.getRegistry().registerAll(new  Item().setRegistryName("draft_writ")
				.setUnlocalizedName(ImprovedVils.MODID + ".draft_writ")
				.setCreativeTab(CreativeTabs.COMBAT),
									new ItemMarshalsBaton().setRegistryName("marshals_baton")
				.setUnlocalizedName(ImprovedVils.MODID + ".marshals_baton")
				.setCreativeTab(CreativeTabs.COMBAT));
	}
}
