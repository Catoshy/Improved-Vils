package com.joshycode.improvedmobs;

import javax.annotation.Nullable;

import com.joshycode.improvedmobs.gui.GuiVillagerArm;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilFollowPacket;
import com.joshycode.improvedmobs.network.VilGuardPacket;
import com.joshycode.improvedmobs.network.VilStateQuery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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

	public static void updateVillagerGuardGUIInfo(@Nullable Vec3i pos, int int1, int int2) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui instanceof GuiVillagerArm) {
			if(int1 == -1) {
				((GuiVillagerArm) gui).setFollowState(int2);
			} else if(int2 == -1)  {
				((GuiVillagerArm) gui).setGuardState(pos, int1);
			} else {
				((GuiVillagerArm) gui).setFollowState(int2);
				((GuiVillagerArm) gui).setGuardState(pos, int1);
			}
		}
	}
	
	public static void guardHere(int vilId, boolean guard) {
    	NetWrapper.NETWORK.sendToServer(new VilGuardPacket(new BlockPos(0, 0, 0), vilId, guard));
	}

	public static void followPlayer(int vilId, boolean follow) {
		NetWrapper.NETWORK.sendToServer(new VilFollowPacket(vilId, follow));
	}

	public static void queryState(int vilId) {
		System.out.println("query state");
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId, 0));
	}
}
