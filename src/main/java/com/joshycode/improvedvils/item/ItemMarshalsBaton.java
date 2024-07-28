package com.joshycode.improvedvils.item;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.Provisions;
import com.joshycode.improvedvils.handler.ConfigHandler;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilCommandPacket;
import com.joshycode.improvedvils.network.VilFoodStorePacket;
import com.joshycode.improvedvils.network.VilKitStorePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemMarshalsBaton extends Item {
	//TODO onItemRightClick is actually fired on both sides, rendering the network packets stuff unneeded. Should change it sometime.
	public ItemMarshalsBaton() {}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItemMainhand();
		if(world.isRemote && stack.getItem() instanceof ItemMarshalsBaton)
		{
			Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
			if(entity != null && Minecraft.getMinecraft() != null)
			{
				int provisioningUnit = ImprovedVils.proxy.getProvisioningUnit();
				if(provisioningUnit != -1)
				{
					setTileEntityStore(entity, provisioningUnit);
				}
				else
				{
					tryCommandVillagerMovement(entity);
				}
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	private void setTileEntityStore(Entity entity, int provisioningUnit) 
	{
		if(ImprovedVils.proxy.getStuff() == Provisions.PROVISIONS)
		{
			tryFoodStoreTileEntity(entity, provisioningUnit);
		}
		else
		{
			tryKitStoreTileEntity(entity, provisioningUnit);
		}
	}

	private void tryCommandVillagerMovement(Entity entity)
	{
		double d0 = ConfigHandler.commandDist;
		RayTraceResult lookingAt = entity.rayTrace(d0, 1.0F);
		if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			BlockPos pos = lookingAt.getBlockPos();
			NetWrapper.NETWORK.sendToServer(new VilCommandPacket(pos));
		}
	}

	private void tryFoodStoreTileEntity(Entity entity, int provisioningUnit)
	{
		RayTraceResult lookingAt = entity.rayTrace(6.0, 1.0F);
		if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			BlockPos pos = lookingAt.getBlockPos();
			NetWrapper.NETWORK.sendToServer(new VilFoodStorePacket(pos, provisioningUnit));
		}
	}
	
	private void tryKitStoreTileEntity(Entity entity, int provisioningUnit)
	{
		RayTraceResult lookingAt = entity.rayTrace(6.0, 1.0F);
		if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			Log.info("Set kit store!");
			BlockPos pos = lookingAt.getBlockPos();
			NetWrapper.NETWORK.sendToServer(new VilKitStorePacket(pos, provisioningUnit));
		}
	}
}
