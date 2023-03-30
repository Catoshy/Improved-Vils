package com.joshycode.improvedmobs;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.handler.EventHandlerVil;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ImprovedVils.MODID, name = ImprovedVils.MODNAME, version = ImprovedVils.VERSION, dependencies = "required:tenshilib;")
public class ImprovedVils {

	public static final String MODID = "improvedvils";
	public static final String MODNAME = "Improved Villagers";
	public static final String VERSION = "${@VERSION}";
	public static final Logger logger = LogManager.getLogger(ImprovedVils.MODID);

	@Instance
	public static ImprovedVils instance = new ImprovedVils();
	@SidedProxy(clientSide = "com.joshycode.improvedmobs.ClientProxy", serverSide = "com.joshycode.improvedmobs.ServerProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void onCommonSetup(FMLPreInitializationEvent event) throws IOException {
		MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
		MinecraftForge.EVENT_BUS.register(new EventHandlerVil());
		MinecraftForge.EVENT_BUS.register(proxy);
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e) {}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e) throws IOException {
		proxy.postInit();
	}
	
	
}