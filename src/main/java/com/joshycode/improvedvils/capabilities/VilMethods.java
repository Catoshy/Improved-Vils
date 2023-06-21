package com.joshycode.improvedvils.capabilities;

import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.CommonProxy;
import com.joshycode.improvedvils.handler.CapabilityHandler;
import com.joshycode.improvedvils.util.InventoryUtil;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VilMethods {

	@Nullable
	public static BlockPos getGuardBlockPos(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getGuardBlockPos();
		} catch (NullPointerException ex) {}
		return null;
	}

	@Nullable
	public static PathPoint guardBlockAsPP(EntityVillager e)
	{
		try
		{
			BlockPos pos = getGuardBlockPos(e);
			return new PathPoint(pos.getX(), pos.getY(), pos.getZ());
		} catch (NullPointerException ex) {}
		return null;
	}

	@Nullable
	public static BlockPos getCommBlockPos(EntityVillager e)
	{
		try
		{
	    	return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getCommBlockPos();
		} catch (NullPointerException ex) {}
	    	return null;
	}

	@Nullable
	public static Vec3d commPosAsVec(EntityVillager e)
	{
		try
		{
			BlockPos pos = getCommBlockPos(e);
			return new Vec3d(pos.getX(), pos.getY(), pos.getZ());

	    } catch (NullPointerException ex) {}
		return null;
	}

	public static boolean isReturning(EntityVillager e)
	{
		try
		{
	    		return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isReturning();
	    } catch (NullPointerException ex) {}
	    return false;
	}

	public static void setCommBlockPos(EntityVillager e, BlockPos pos)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setCommBlock(pos);
		} catch (NullPointerException ex) {}
	}

	public static boolean getHungry(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHungry();
		} catch (NullPointerException ex) {}
		return true;
	}

	public static void setReturning(EntityVillager e, boolean b)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setReturning(b);
		} catch (NullPointerException ex) {}
	}

	@Nullable
	public static Vec3d guardBlockAsVec(EntityVillager e)
	{
		try
		{
			BlockPos pos = getGuardBlockPos(e);
			return new Vec3d(pos.getX(), pos.getY(), pos.getZ());

		} catch (NullPointerException ex) {}
		return null;
	}

	public static void setMovingIndoors(EntityVillager e, boolean b)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMovingIndoors(b);
		} catch (NullPointerException ex) {}
	}

	public static boolean getMovingIndoors(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isMovingIndoors();
		} catch (NullPointerException ex) {}
		return false;
	}

	public static void setFollowing(EntityVillager e, boolean b)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(b);
		} catch (NullPointerException ex) {}
	}

	public static boolean getFollowing(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isFollowing();
		} catch (NullPointerException ex) {}
		return false;
	}

	public static UUID getPlayerId(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
		} catch (NullPointerException ex) {}

		return UUID.randomUUID();
	}

	public static boolean isOutsideHomeDist(EntityVillager attacker)
	{
		if(!attacker.isWithinHomeDistanceCurrentPosition())
		{
			if(InventoryUtil.doesInventoryHaveItem
					(attacker.getVillagerInventory(), CommonProxy.ItemHolder.DRAFT_WRIT) != 0)
			{
				return false;
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean setGuardBlock(EntityVillager entity, BlockPos pos)
	{
		try
		{
			entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(pos);
			return true;
		} catch (NullPointerException e) {return false; }
	}

	public static boolean setFollowState(EntityVillager e, boolean followState)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFollowing(followState);
			return true;
		} catch (NullPointerException ex) {return false; }
	}

	public static BlockPos getFoodStorePos(EntityVillager e) {
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getFoodStorePos();
		} catch (NullPointerException ex) {return null; }
	}

	public static void setFoodStore(EntityVillager e, @Nullable BlockPos pos) {
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFoodStore(pos);
		} catch (NullPointerException ex) {}
	}

	public static boolean isRefillingFood(EntityVillager e) {
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getRefillingFood();
		} catch (NullPointerException ex) {return false; }
	}

	public static boolean setRefilling(EntityVillager e, boolean b) {
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setRefilling(b);
			return true;
		} catch (NullPointerException ex) {return false; }
	}

	public static UUID getHomeVillageId(EntityVillager entityIn)
	{
		return entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHomeVillageID();
	}

	public static void setPlayerId(EntityPlayer player, EntityVillager entityIn)
	{
		entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerId(player.getUniqueID());
	}

	public static void setTeam(EntityVillager villager, String team)
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setTeam(team);
	}

	public static void setPlayerReputation(EntityVillager villager, UUID uniqueID, float f, int i)
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerReputation(uniqueID, f, i);
	}

	@Nullable
	public static String getTeam(EntityVillager villager)
	{
		return villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getTeam();
	}
}
