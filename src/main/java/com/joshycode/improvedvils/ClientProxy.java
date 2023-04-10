package com.joshycode.improvedvils;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.gui.GuiVillagerArm;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilFollowPacket;
import com.joshycode.improvedvils.network.VilGuardPacket;
import com.joshycode.improvedvils.network.VilStateQuery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	public static void updateVillagerEnlistGUIInfo(boolean isEnlisted, int company, int platoon) 
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui instanceof GuiVillagerArm) 
		{
			((GuiVillagerArm) gui).setEnlistState(isEnlisted, company, platoon);
		}
	}
	
	@SubscribeEvent
	public void onModelRegisEvent(ModelRegistryEvent e) 
	{
		ModelLoader.setCustomModelResourceLocation(CommonProxy.ItemHolder.DRAFT_WRIT, 0, 
				new ModelResourceLocation(CommonProxy.ItemHolder.DRAFT_WRIT.getRegistryName(), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(CommonProxy.ItemHolder.BATON, 0, 
				new ModelResourceLocation(CommonProxy.ItemHolder.BATON.getRegistryName(), "inventory"));
	}

	public static void updateVillagerGuardGUIInfo(@Nullable Vec3i pos, int int1, int int2) 
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		
		if(gui instanceof GuiVillagerArm) 
		{
			if(int1 == -1) 
			{
				((GuiVillagerArm) gui).setFollowState(int2);
			} 
			else if(int2 == -1)  
			{
				((GuiVillagerArm) gui).setGuardState(pos, int1);
			} 
			else 
			{
				((GuiVillagerArm) gui).setFollowState(int2);
				((GuiVillagerArm) gui).setGuardState(pos, int1);
			}
		}
	}
	
	public static void guardHere(int vilId, boolean guard) 
	{
    	NetWrapper.NETWORK.sendToServer(new VilGuardPacket(new BlockPos(0, 0, 0), vilId, guard));
	}

	public static void followPlayer(int vilId, boolean follow) 
	{
		NetWrapper.NETWORK.sendToServer(new VilFollowPacket(vilId, follow));
	}

	public static void queryState(int vilId) 
	{
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId, 0));
	}

	public static void close(int vilId) 
	{
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId, -2));
	}

	public static void enlist(int vilId, int company, int platoon) 
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(vilId, company, platoon, true));
	}

	public static void unEnlist(int vilId) 
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(vilId, 0, 0, false));
	}
}
