package com.joshycode.improvedmobs.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CapabilityHandler {
	 	@CapabilityInject(IImprovedVilCapability.class)
	    @Nonnull
	    @SuppressWarnings("ConstantConditions")
	    public static Capability<IImprovedVilCapability> VIL_PLAYER_CAPABILITY = null;
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
	    
	    @Nullable
	    public static BlockPos getGuardBlockPos(EntityVillager e) {
	    	try {
	    		return e.getCapability(VIL_PLAYER_CAPABILITY, null).getGuardBlockPos();
	    	} catch (NullPointerException ex) {}
	    	return null;
	    }
	    
	    @Nullable
		public static PathPoint guardBlockAsPP(EntityVillager e) {
			try {
				BlockPos pos = getGuardBlockPos(e);
				return new PathPoint(pos.getX(), pos.getY(), pos.getZ());
			} catch (NullPointerException ex) {}
			return null;
		}

		@Nullable
		public static BlockPos getCommBlockPos(EntityVillager e) {
			  try {
		    		return e.getCapability(VIL_PLAYER_CAPABILITY, null).getCommBlockPos();
		    	} catch (NullPointerException ex) {}
		    	return null;
		}

		@Nullable
		public static Vec3d commPosAsVec(EntityVillager e) {
			try {
				BlockPos pos = getCommBlockPos(e);
				return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		    } catch (NullPointerException ex) {}
			return null;
		}
		
		public static boolean isReturning(EntityVillager e) {
			try {
		    		return e.getCapability(VIL_PLAYER_CAPABILITY, null).isReturning();
		    } catch (NullPointerException ex) {}
		    return false;
		}

		public static void setCommBlockPos(EntityVillager e, BlockPos pos) {
			try {
	    		e.getCapability(VIL_PLAYER_CAPABILITY, null).setCommBlock(pos);
			} catch (NullPointerException ex) {}
		}

		public static boolean getHungry(EntityVillager e) {
			try {
	    		return e.getCapability(VIL_PLAYER_CAPABILITY, null).getHungry();
			} catch (NullPointerException ex) {}
			return true;
		}

		public static void setReturning(EntityVillager e, boolean b) {
			try {
	    		e.getCapability(VIL_PLAYER_CAPABILITY, null).setReturning(b);
			} catch (NullPointerException ex) {}
		}

		@Nullable
		public static Vec3d guardBlockAsVec(EntityVillager e) {
			try {
				BlockPos pos = getGuardBlockPos(e);
				return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
			} catch (NullPointerException ex) {}
			return null;
		}

		public static void setMovingIndoors(EntityVillager e, boolean b) {
			try {
	    		e.getCapability(VIL_PLAYER_CAPABILITY, null).setMovingIndoors(b);
			} catch (NullPointerException ex) {}
		}
		
		public static boolean getMovingIndoors(EntityVillager e) {
			try {
	    		return e.getCapability(VIL_PLAYER_CAPABILITY, null).isMovingIndoors();
			} catch (NullPointerException ex) {}
			return false;
		}
}
