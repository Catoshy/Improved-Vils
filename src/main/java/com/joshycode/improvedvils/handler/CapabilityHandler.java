package com.joshycode.improvedvils.handler;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.StaticCapabilityProvider;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.IMarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.entity.ImprovedVilCapability;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability;
import com.joshycode.improvedvils.capabilities.itemstack.ILetterOfCommandCapability;
import com.joshycode.improvedvils.capabilities.itemstack.LetterOfCommandCapability;
import com.joshycode.improvedvils.capabilities.village.IVillageCapability;
import com.joshycode.improvedvils.capabilities.village.VillageCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.Village;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public final class CapabilityHandler {

		@CapabilityInject(IImprovedVilCapability.class)
	    @Nonnull
	    public static Capability<IImprovedVilCapability> VIL_PLAYER_CAPABILITY = null;
	    public static final ResourceLocation VIL_PLAYER_CAPABILITYR = ImprovedVils.location("vil_player");

	    @CapabilityInject(IMarshalsBatonCapability.class)
	    @Nonnull
	    public static Capability<IMarshalsBatonCapability> MARSHALS_BATON_CAPABILITY = null;
	    public static final ResourceLocation MARSHALS_BATON_CAPABILITYR = ImprovedVils.location("vil_baton");

	    @CapabilityInject(ILetterOfCommandCapability.class)
	    @Nonnull
	    public static Capability<ILetterOfCommandCapability> COMMAND_LETTER_CAPABILITY = null;
	    public static final ResourceLocation COMMAND_LETTER_CAPABILITYR = ImprovedVils.location("vil_letter");
	    
	    @CapabilityInject(IVillageCapability.class)
	    @Nonnull
	    public static Capability<IVillageCapability> VILLAGE_CAPABILITY = null;
	    public static final ResourceLocation VILLAGE_CAPABILITYR = ImprovedVils.location("vil_village");

	    @SubscribeEvent
	    public void onEntityAttachCapabilities(@Nonnull AttachCapabilitiesEvent<Entity> e)
	    {
	        final Entity ent = e.getObject();
        	if(ent instanceof EntityVillager)
        	{
        		final IImprovedVilCapability capability = new ImprovedVilCapability(); // Default Storage could be used
        		e.addCapability(
        				VIL_PLAYER_CAPABILITYR,
        				StaticCapabilityProvider.from(VIL_PLAYER_CAPABILITY, capability)
        		);
        	}
        	else if(ent instanceof EntityPlayer)
        	{
        		final IMarshalsBatonCapability capability = new MarshalsBatonCapability();
	    		e.addCapability(
	    				MARSHALS_BATON_CAPABILITYR,
	    				StaticCapabilityProvider.from(MARSHALS_BATON_CAPABILITY, capability)
	    		);
        	}
	    }
	    
	    @SubscribeEvent
	    public void playerDataPersist(PlayerEvent.Clone e)
	    {
	    	if(!e.isWasDeath()) return;
	    	IMarshalsBatonCapability orgCap = e.getOriginal().getCapability(MARSHALS_BATON_CAPABILITY, null);
	    	e.getEntityPlayer().getCapability(MARSHALS_BATON_CAPABILITY, null).attachInfo(orgCap.getfoodStoreMap(), orgCap.getkitStoreMap(), orgCap.getPlatoonMap());
	    }

	    @SubscribeEvent
	    public void onItemStackAttachCapabilities(@Nonnull AttachCapabilitiesEvent<ItemStack> e)
	    {
	    	final ItemStack stack = e.getObject();
	    	if(stack.getItem().equals(CommonProxy.ItemHolder.LETTER))
	    	{
	    		final ILetterOfCommandCapability capability = new LetterOfCommandCapability();
	    		e.addCapability(
	    				COMMAND_LETTER_CAPABILITYR,
	    				StaticCapabilityProvider.from(COMMAND_LETTER_CAPABILITY, capability)
	    		);
	    	}
	    }

	    @SubscribeEvent
	    public void onVillageAttachCapabilities(@Nonnull AttachCapabilitiesEvent<Village> e)
	    {
	    	if(ConfigHandler.debug)
	    		Log.info("Attach village!", e.getObject());
    		final IVillageCapability capability = new VillageCapability();
    		capability.setUUID(UUID.randomUUID());
    		e.addCapability(
    				VILLAGE_CAPABILITYR,
    				StaticCapabilityProvider.from(VILLAGE_CAPABILITY, capability)
    		);
	    }
}
