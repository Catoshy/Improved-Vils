package com.joshycode.improvedmobs.handler;

import javax.annotation.Nonnull;

import com.joshycode.improvedmobs.CommonProxy;
import com.joshycode.improvedmobs.ImprovedVils;
import com.joshycode.improvedmobs.capabilities.StaticCapabilityProvider;
import com.joshycode.improvedmobs.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedmobs.capabilities.entity.ImprovedVilCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.MarshalsBatonCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CapabilityHandler {
	 	@CapabilityInject(IImprovedVilCapability.class)
	    @Nonnull
	    public static Capability<IImprovedVilCapability> VIL_PLAYER_CAPABILITY = null;
	    public static final ResourceLocation VIL_PLAYER_CAPABILITYR = new ResourceLocation(ImprovedVils.MODID, "vil_player");
	    @CapabilityInject(IMarshalsBatonCapability.class)
	    @Nonnull
	    public static Capability<IMarshalsBatonCapability> MARSHALS_BATON_CAPABILITY = null;
	    public static final ResourceLocation MARSHALS_BATON_CAPABILITYR = new ResourceLocation(ImprovedVils.MODID, "vil_baton");
	    
	    @SubscribeEvent
	    public void onEntityAttachCapabilities(@Nonnull AttachCapabilitiesEvent<Entity> e) {
	        final Entity ent = e.getObject();
        	if(ent instanceof EntityVillager) {
        		final IImprovedVilCapability capability = new ImprovedVilCapability(); // Default Storage could be used
        		e.addCapability(
        				VIL_PLAYER_CAPABILITYR,
        				StaticCapabilityProvider.from(VIL_PLAYER_CAPABILITY, capability)
        		);
        	}
	    }
	    
	    @SubscribeEvent
	    public void onItemStackAttachCapabilities(@Nonnull AttachCapabilitiesEvent<ItemStack> e) {
	    	final ItemStack stack = e.getObject();
	    	if(stack.getItem().equals(CommonProxy.ItemHolder.BATON)) {
	    		final IMarshalsBatonCapability capability = new MarshalsBatonCapability();
	    		e.addCapability(
	    				MARSHALS_BATON_CAPABILITYR, 
	    				StaticCapabilityProvider.from(MARSHALS_BATON_CAPABILITY, capability));
	    	}
	    }
}
