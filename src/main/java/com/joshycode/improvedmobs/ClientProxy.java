package com.joshycode.improvedmobs;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.gui.GuiVillagerArm;
import com.joshycode.improvedmobs.handler.CapabilityHandler;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilGuardPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	public static void updateVillagerEnlistGUIInfo(boolean isEnlisted, int company, int platoon) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui instanceof GuiVillagerArm) {
			((GuiVillagerArm) gui).setEnlistState(isEnlisted, company, platoon);
		}
	}
	
	@SubscribeEvent
	public void onModelRegisEvent(ModelRegistryEvent e) {
		ModelLoader.setCustomModelResourceLocation(CommonProxy.ItemHolder.DRAFT_WRIT, 0, 
				new ModelResourceLocation(CommonProxy.ItemHolder.DRAFT_WRIT.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(CommonProxy.ItemHolder.BATON, 0, 
				new ModelResourceLocation(CommonProxy.ItemHolder.BATON.getRegistryName(), "inventory"));
	}

	public static void updateVillagerGuardGUIInfo(@Nullable BlockPos pos, int id) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui instanceof GuiVillagerArm) {
			((GuiVillagerArm) gui).setGuardState(pos, id);
		}
	}
	
	public static void guardHere(int vilId, boolean guard) {
    	NetWrapper.NETWORK.sendToServer(new VilGuardPacket(new BlockPos(0, 0, 0), vilId, guard));
	}
}
