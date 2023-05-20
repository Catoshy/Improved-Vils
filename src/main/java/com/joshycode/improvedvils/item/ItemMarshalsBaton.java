package com.joshycode.improvedvils.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.joshycode.improvedvils.handler.ConfigHandlerVil;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilCommandPacket;
import com.joshycode.improvedvils.network.VilFoodStorePacket;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMarshalsBaton extends Item {

	public ItemMarshalsBaton() {}
	
	@SideOnly(Side.CLIENT)
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) 
	{
		ItemStack stack = player.getHeldItemMainhand();
		if(stack.getItem() instanceof ItemMarshalsBaton) 
		{
			Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
			if(entity != null && Minecraft.getMinecraft() != null) 
			{
				if(player.isSneaking()) 
				{
					tryFoodStoreTileEntity(entity);
				}
				else
				{
					tryCommandVillagerMovement(entity);
				}
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
	
	private void tryCommandVillagerMovement(Entity entity) 
	{
		double d0 = ConfigHandlerVil.commandDist;
		RayTraceResult lookingAt = entity.rayTrace(d0, 1.0F);
		if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) 
		{
			BlockPos pos = lookingAt.getBlockPos();
			NetWrapper.NETWORK.sendToServer(new VilCommandPacket(pos));
		}
	}

	private void tryFoodStoreTileEntity(Entity entity) 
	{
		RayTraceResult lookingAt = entity.rayTrace(6.0, 1.0F);
		if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) 
		{
			BlockPos pos = lookingAt.getBlockPos();
			NetWrapper.NETWORK.sendToServer(new VilFoodStorePacket(pos));
		}
	}

	public static synchronized Set<Entity> getEntitiesByUUID(Set<UUID> ids, World world) 
	{
		Set<Entity> applicable = new HashSet();
		if(world.getLoadedEntityList() != null && world.getLoadedEntityList().size() != 0)	
		{
			List<Entity> list = new CopyOnWriteArrayList <Entity> (world.getLoadedEntityList());
			for (Entity e : list) 
			{
				if(world.getChunkFromBlockCoords(e.getPosition()).isLoaded()) 
				{
					 if(ids.contains(e.getUniqueID())) 
					 {
						 applicable.add(e);
					 }
				}
			}
		}
		return applicable;
	}
}
