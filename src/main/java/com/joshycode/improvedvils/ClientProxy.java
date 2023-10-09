package com.joshycode.improvedvils;

import java.io.IOException;

import javax.annotation.Nullable;

import org.jline.utils.Log;
import org.lwjgl.input.Keyboard;

import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability.Provisions;
import com.joshycode.improvedvils.entity.EntityBullet;
import com.joshycode.improvedvils.gui.GuiVillagerArm;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.MarshalKeyEvent;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilFollowPacket;
import com.joshycode.improvedvils.network.VilGuardPacket;
import com.joshycode.improvedvils.network.VilGuiQuery;
import com.joshycode.improvedvils.network.VilStateQuery;
import com.joshycode.improvedvils.renderer.LayerArmourNonBiped;
import com.joshycode.improvedvils.renderer.LayerHeldItemNonBiped;
import com.joshycode.improvedvils.renderer.ModelBipedVillager;
import com.joshycode.improvedvils.renderer.RenderBullet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends CommonProxy {
	
	public static KeyBinding[] keyBindings;
	
	private long hudUpdateTime;
	private int selectedPlatoon;
	private int provisioningPlatoon;
	private Provisions provisions;
	
	@SuppressWarnings("unchecked")
	@Override
	public void preInit() throws IOException
	{
		super.preInit();
		RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, new IRenderFactory() 
		{
			@Override
			public Render createRenderFor(RenderManager manager) {
				return new RenderBullet(manager);
			}
		});
	}
	
	@Override
	public void init()
	{
		keyBindings = new KeyBinding[5];
		keyBindings[0] = new KeyBinding("key.marshal.platoon+", Keyboard.KEY_UP, "Improvedvils.keybinds");
		keyBindings[1] = new KeyBinding("key.marshal.platoon-", Keyboard.KEY_DOWN, "Improvedvils.keybinds");
		keyBindings[2] = new KeyBinding("key.marshal.company+", Keyboard.KEY_RIGHT, "Improvedvils.keybinds");
		keyBindings[3] = new KeyBinding("key.marshal.company-", Keyboard.KEY_LEFT, "Improvedvils.keybinds");
		keyBindings[4] = new KeyBinding("key.marshal.baton_gui", Keyboard.KEY_LCONTROL, "Improvedvils.keybinds");
		
		for(KeyBinding bind : keyBindings)
		{
			ClientRegistry.registerKeyBinding(bind);
		}
	}

	@Override
	public void postInit() throws IOException
	{
		super.postInit();
		this.addLayersToVillagerModel();
	}
	
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addLayersToVillagerModel() 
	{
		if(!ConfigHandler.renderItemsAndArmour) return;
		
		RenderLiving<EntityVillager> renderer = (RenderLiving) Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(new EntityVillager(null));
		renderer.addLayer(new LayerHeldItemNonBiped(renderer));
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(renderer)
        {
            protected void initArmor()
            {
            	this.modelLeggings = new ModelBiped(0.48F);
                this.modelArmor = new ModelBipedVillager(37F, .98F);
                this.modelArmor.bipedHead.offsetY -= .15F;
            }
        };
        renderer.addLayer(layerbipedarmor);
	}

	public static void updateVillagerGuardGUIInfo(@Nullable Vec3i pos, int guardStateVal, int followStateVal, int enlistStateAndCompany, int enlistPlatoon)
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;

		if(gui instanceof GuiVillagerArm)
		{
			if(guardStateVal == -1)
			{
				((GuiVillagerArm) gui).setFollowState(followStateVal);
			}
			else if(followStateVal == -1)
			{
				((GuiVillagerArm) gui).setGuardState(pos, guardStateVal);
			}
			else
			{
				((GuiVillagerArm) gui).setFollowState(followStateVal);
				((GuiVillagerArm) gui).setGuardState(pos, guardStateVal);
			}
		}
	}
	
	public void setHUDinfo(int platoon)
	{
		if(Minecraft.getMinecraft().world == null) return;
			
		if(ConfigHandler.debug)
			Log.info("updating HUD info? platoon:  %s", platoon);
		this.hudUpdateTime = Minecraft.getMinecraft().world.getWorldTime();
		this.selectedPlatoon = platoon;
	}
	
	@Override
	public int timeAgoSinceHudInfo() 
	{
		if(Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().world.getWorldTime() < 200) return 24000;
		
		return (int) (Minecraft.getMinecraft().world.getWorldTime() - this.hudUpdateTime);
	}
	
	public int getSelectedUnit()
	{
		return this.selectedPlatoon;
	}
	
	@Override
	public void setProvisioningPlatoon(int platoon, Provisions kit) 
	{
		this.provisions = kit;
		this.provisioningPlatoon = platoon;
	}
	
	@Override
	public int getProvisioningUnit() 
	{
		return this.provisioningPlatoon; 
	}
	
	@Override
	public Provisions getKit() 
	{
		return this.provisions;
	}

	@Override
	public IThreadListener getListener(MessageContext ctx)
	{
		return ctx.side.isClient() ? Minecraft.getMinecraft() : super.getListener(ctx);
	}

	@Override
	public World getWorld(MessageContext ctx)
	{
		return ctx.side.isClient() ? Minecraft.getMinecraft().world : super.getWorld(ctx);
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx)
	{
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx);
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
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId));
	}

	public static void close(int vilId)
	{
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId, true));
	}

	public static void enlist(int vilId, int company, int platoon)
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(vilId, company, platoon, true));
	}

	public static void unEnlist(int vilId)
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(vilId, 0, 0, false));
	}

	public static void openGuiForPlayerIfOK(int entityId)
	{
		NetWrapper.NETWORK.sendToServer(new VilGuiQuery(entityId));
	}

	public static void marshalKeyEvent(int i) 
	{
		NetWrapper.NETWORK.sendToServer(new MarshalKeyEvent(i));
	}
}
