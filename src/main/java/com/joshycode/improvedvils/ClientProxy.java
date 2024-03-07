package com.joshycode.improvedvils;

import java.io.IOException;

import javax.annotation.Nullable;

import org.jline.utils.Log;
import org.lwjgl.input.Keyboard;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability.Provisions;
import com.joshycode.improvedvils.entity.EntityBullet;
import com.joshycode.improvedvils.gui.GuiVillagerArm;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.MarshalKeyEvent;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilGuiQuery;
import com.joshycode.improvedvils.network.VilStateQuery;
import com.joshycode.improvedvils.renderer.LayerHeldItemNonBiped;
import com.joshycode.improvedvils.renderer.ModelBipedVillager;
import com.joshycode.improvedvils.renderer.RenderBullet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
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
	
	@Override
	public void preInit() throws IOException
	{
		super.preInit();
		RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, new IRenderFactory<EntityBullet>() 
		{
			@Override
			public Render<EntityBullet> createRenderFor(RenderManager manager) {
				return new RenderBullet<EntityBullet>(manager);
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
		try 
		{
			ConfigHandler.load(LoadState.POSTINIT);
		}
		catch(Exception ex)
		{
			if(ex instanceof JsonSyntaxException)
			{
				throw new CustomModLoadingErrorDisplayException() 
				{

					private static final long serialVersionUID = -3193026792627553979L;

					@Override
					public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

					@Override
					public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX,
							int mouseRelY, float tickTime) 
					{
						String message = "Could not read Gun-Config Json in the configs for ImprovedVils due to improper syntax. ";
						String messageLine2 = "Please check your configs and correct any syntax errors.";
						String messageLine3 =  "You can delete or rename the current json to generate a readme file.";
						int stringWidth = fontRenderer.getStringWidth(message);
						int stringWidth2 = fontRenderer.getStringWidth(messageLine2);
						int stringWidth3 = fontRenderer.getStringWidth(messageLine3);
						fontRenderer.drawString(message, errorScreen.width / 2 - stringWidth / 2, errorScreen.height / 2, 16777215);
						fontRenderer.drawString(messageLine2, errorScreen.width / 2 - stringWidth2 / 2, errorScreen.height / 2 + fontRenderer.FONT_HEIGHT, 16777215);
						fontRenderer.drawString(messageLine3, errorScreen.width / 2 - stringWidth3 / 2, errorScreen.height / 2 + fontRenderer.FONT_HEIGHT * 2, 16777215);
					}	
				};
			}
			else if(ex instanceof IOException || ex instanceof JsonParseException)
			{
				throw new CustomModLoadingErrorDisplayException() 
				{

					private static final long serialVersionUID = -2189386486689782027L;

					@Override
					public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

					@Override
					public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX,
							int mouseRelY, float tickTime) 
					{
						String message = "Could not read Gun-Config Json in the configs for ImprovedVils due to IO error. ";
						String messageLine2 =  "Please check your configs and ensure Minecraft can access and read any files and folders.";
						int stringWidth = fontRenderer.getStringWidth(message);
						int stringWidth2 = fontRenderer.getStringWidth(messageLine2);
						fontRenderer.drawString(message, errorScreen.width / 2 - stringWidth / 2, errorScreen.height / 2, 16777215);
						fontRenderer.drawString(messageLine2, errorScreen.width / 2 - stringWidth2 / 2, errorScreen.height / 2 + fontRenderer.FONT_HEIGHT, 16777215);
					}	
				};
			}
		}
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

	public static void updateVillagerGuardGUIInfo(@Nullable Vec3i pos, int guardStateVal, int followStateVal, int dutyStateVal, boolean hungry, int enlistStateAndCompany, int enlistPlatoon)
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;

		if(gui instanceof GuiVillagerArm)
		{
			((GuiVillagerArm) gui).setDutyState(dutyStateVal);
			((GuiVillagerArm) gui).setHungry(hungry);
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
	public Provisions getStuff() 
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

	public static void close(int vilId)
	{
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(vilId, true));
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
