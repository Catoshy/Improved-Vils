package com.joshycode.improvedmobs.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilCommandPacket;

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

	public ItemMarshalsBaton() {
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItemMainhand();
		if(stack.getItem() instanceof ItemMarshalsBaton) {
			double d0 = 250D; //TODO Config
			Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
			if(entity != null && Minecraft.getMinecraft() != null) {
				RayTraceResult lookingAt = entity.rayTrace(d0, 1.0F);
				if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) {
					BlockPos pos = lookingAt.getBlockPos();
					NetWrapper.NETWORK.sendToServer(new VilCommandPacket(pos));
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
	
	public static synchronized Set<Entity> getEntitiesByUUID(Set<UUID> ids, World world) {
		
		Set<Entity> applicable = new HashSet();
		if(world.getLoadedEntityList() != null && world.getLoadedEntityList().size() != 0)	{
			List<Entity> list = new CopyOnWriteArrayList <Entity> (world.getLoadedEntityList());
			for (Entity e : list) {
				if(world.getChunkFromBlockCoords(e.getPosition()).isLoaded()) {
					 if(ids.contains(e.getUniqueID())) {
						 applicable.add(e);
						 
					 }
				}
			}
		}
		return applicable;
	}
}
