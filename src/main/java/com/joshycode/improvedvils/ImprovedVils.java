package com.joshycode.improvedvils;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joshycode.improvedvils.command.CommandDestroyCommand;
import com.joshycode.improvedvils.command.CommandGetEntityName;
import com.joshycode.improvedvils.command.CommandGetUnlocalName;
import com.joshycode.improvedvils.command.CommandTransferCommand;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.handler.EventHandlerVil;
import com.joshycode.improvedvils.handler.GraveStoneCompHandler;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ImprovedVils.MODID, name = ImprovedVils.MODNAME, version = ImprovedVils.VERSION, certificateFingerprint = ImprovedVils.certificateFingerprint, dependencies = "after:openblocks;")
public class ImprovedVils {

	public static final String MODID = "improvedvils";
	public static final String MODNAME = "Improved Villagers";
	public static final String VERSION = "1.0.4b1";
	public static final String certificateFingerprint = "e34b86ab6155979713e5a093503cb0140ecb7134";
	public static final Logger logger = LogManager.getLogger(ImprovedVils.MODID);

	@Instance
	public static ImprovedVils instance = new ImprovedVils();
	@SidedProxy(clientSide = "com.joshycode.improvedvils.ClientProxy", serverSide = "com.joshycode.improvedvils.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void onCommonSetup(FMLPreInitializationEvent event) throws IOException
	{
		MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
		MinecraftForge.EVENT_BUS.register(new EventHandlerVil());
		MinecraftForge.EVENT_BUS.register(proxy);
		if(Loader.isModLoaded("openblocks"))
			MinecraftForge.EVENT_BUS.register(new GraveStoneCompHandler());
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent e) 
	{
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) throws IOException
	{
		proxy.postInit();
	}
	
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event) 
	{
		event.registerServerCommand(new CommandGetEntityName());
		event.registerServerCommand(new CommandDestroyCommand());
		event.registerServerCommand(new CommandTransferCommand());
	}
	
	@EventHandler
	public void onSignatureFailed(FMLFingerprintViolationEvent e)
	{
		Log.warn("Fingerprint for ImprovedVils is not right. Either the mod has been edited or something is wrong. The mod author cannot support edited versions of the mod.", (Object[]) null);
	}

	public static ResourceLocation location(String string) 
	{
		return new ResourceLocation(MODID, string);
	}


}