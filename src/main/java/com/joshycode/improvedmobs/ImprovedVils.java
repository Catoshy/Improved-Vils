package com.joshycode.improvedmobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joshycode.improvedmobs.handler.ConfigHandlerVil;
import com.joshycode.improvedmobs.handler.EventHandlerVilAI;
import com.joshycode.improvedmobs.handler.VillagerCapabilityHandler;
import com.joshycode.improvedmobs.item.ItemMarshalsBaton;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = ImprovedVils.MODID, name = ImprovedVils.MODNAME, version = ImprovedVils.VERSION, dependencies = "required:coroutil;")
public class ImprovedVils {

	public static final String MODID = "improvedvils";
	public static final String MODNAME = "Improved Villagers";
	public static final String VERSION = "${@VERSION}";
	public static final Logger logger = LogManager.getLogger(ImprovedVils.MODID);

	@Instance
	public static ImprovedVils instance = new ImprovedVils();

	public static CommonProxy proxyv;

	@EventHandler
	public void onCommonSetup(FMLPreInitializationEvent event) {
		CommonProxy.registerCapabilities();
		CommonProxy.registerPackets();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new VillagerCapabilityHandler());
		MinecraftForge.EVENT_BUS.register(new EventHandlerVilAI());
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new CommonProxy());
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		ConfigHandlerVil.testRegis(); //TESTING ONLY TODO
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		Item bateon = new ItemMarshalsBaton();
		bateon.setCreativeTab(CreativeTabs.COMBAT);
		event.getRegistry().register(bateon);
	}
}