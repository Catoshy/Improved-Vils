package com.joshycode.improvedmobs.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.joshycode.improvedmobs.ImprovedVils;
import com.joshycode.improvedmobs.capabilities.CapabilityStorage;
import com.joshycode.improvedmobs.capabilities.StaticCapabilityProvider;
import com.joshycode.improvedmobs.capabilities.entity.IVilPlayerIdCapability;
import com.joshycode.improvedmobs.capabilities.entity.VilPlayerCapabilityFactory;
import com.joshycode.improvedmobs.capabilities.entity.VilPlayerIdCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.MarshalsBatonCapability;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedmobs.item.ItemMarshalsBaton;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class VillagerCapabilityHandler {
	 	@CapabilityInject(IVilPlayerIdCapability.class)
	    @Nonnull
	    @SuppressWarnings("ConstantConditions")
	    public static Capability<IVilPlayerIdCapability> VIL_PLAYER_CAPABILITY = null;
	    public static final ResourceLocation VIL_PLAYER_CAPABILITYR = new ResourceLocation(ImprovedVils.MODID, "vil_player");
	    @CapabilityInject(IMarshalsBatonCapability.class)
	    @Nonnull
	    @SuppressWarnings("ConstantConditions")
	    public static Capability<IMarshalsBatonCapability> MARSHALS_BATON_CAPABILITY = null;
	    public static final ResourceLocation MARSHALS_BATON_CAPABILITYR = new ResourceLocation(ImprovedVils.MODID, "vil_baton");
	    
	    @SubscribeEvent
	    public void onEntityAttachCapabilities(@Nonnull AttachCapabilitiesEvent<Entity> e) {
	        final Entity ent = e.getObject();
        	if(ent instanceof EntityVillager) {
        		final IVilPlayerIdCapability capability = new VilPlayerIdCapability(); // Default Storage could be used
        		e.addCapability(
        				VIL_PLAYER_CAPABILITYR,
        				StaticCapabilityProvider.from(VIL_PLAYER_CAPABILITY, capability)
        		);
        	}
	    }
	    
	    @SubscribeEvent
	    public void onItemStackAttachCapabilities(@Nonnull AttachCapabilitiesEvent<ItemStack> e) {
	    	final ItemStack stack = e.getObject();
	    	if(stack.getItem().equals(Items.STICK)) { //TODO Marshals Baton
	    		final IMarshalsBatonCapability capability = new MarshalsBatonCapability();
	    		e.addCapability(
	    				MARSHALS_BATON_CAPABILITYR, 
	    				StaticCapabilityProvider.from(MARSHALS_BATON_CAPABILITY, capability));
	    	}
	    }
}
