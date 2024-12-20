package com.joshycode.improvedvils.capabilities;

import java.util.UUID;

import javax.annotation.Nullable;

import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;
import com.joshycode.improvedvils.handler.CapabilityHandler;

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
	
	public static boolean setGuardBlock(EntityVillager entity, BlockPos pos)
	{
		try
		{
			entity.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setGuardBlockPos(pos);
			return true;
		} catch (NullPointerException e) {return false; }
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
	public static Vec3d guardBlockAsVec(EntityVillager e)
	{
		try
		{
			BlockPos pos = getGuardBlockPos(e);
			return new Vec3d(pos.getX(), pos.getY(), pos.getZ());

		} catch (NullPointerException ex) {}
		return null;
	}

	@Nullable
	public static BlockPos getCommBlockPos(EntityVillager e)
	{
		return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getCommBlockPos();
	}

	public static void setCommBlockPos(EntityVillager e, BlockPos pos)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setCommBlock(pos);
		} catch (NullPointerException ex) {}
	}
	
	@Nullable
	public static Vec3d commPosAsVec(EntityVillager e)
	{
		BlockPos pos = getCommBlockPos(e);
		if(pos == null) return null;
		
		return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
	}

	public static BlockPos getFoodStorePos(EntityVillager e) 
	{
		return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getFoodStorePos();
	}

	public static void setFoodStore(EntityVillager e, @Nullable BlockPos pos) 
	{
		e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setFoodStore(pos);
	}

	public static BlockPos getKitStorePos(EntityVillager e) 
	{
		return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getKitStorePos();
	}
	
	public static void setKitStore(EntityVillager e, @Nullable BlockPos pos)
	{
		e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setKitStore(pos);
	}
	
	public static BlockPos getLastDoor(EntityVillager villager) 
	{
		return villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getLastDoor();
	}
	
	public static void setLastDoor(EntityVillager villager, BlockPos lastDoor) 
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setLastDoor(lastDoor);
	}

	public static TroopCommands getTroopFaring(EntityVillager e) 
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getTroopFaring();
		} catch (NullPointerException ex) {}
		return TroopCommands.NONE;
	}
	
	public static boolean isDrinking(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isDrinking();
		} catch(NullPointerException ex) {}
		return false;
	}
	
	public static void setTroopFaring(EntityVillager e, TroopCommands c)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setTroopFaring(c);
		} catch (NullPointerException ex) {}
	}

	public static boolean isReturning(EntityVillager e)
	{
		try
		{
	    		return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isReturning();
	    } catch (NullPointerException ex) {}
	    return false;
	}
	
	public static void setReturning(EntityVillager e, boolean b)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setReturning(b);
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

	public static boolean getFollowing(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isFollowing();
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

	public static boolean getMovingIndoors(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).isMovingIndoors();
		} catch (NullPointerException ex) {}
		return false;
	}
	
	public static void setMovingIndoors(EntityVillager e, boolean b)
	{
		try
		{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setMovingIndoors(b);
		} catch (NullPointerException ex) {}
	}
	
	public static boolean getDuty(EntityVillager villager) 
	{
		return villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getActiveDuty();
	}

	public static void setDuty(EntityVillager villager, boolean duty) 
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setActiveDuty(duty);
	}

	public static boolean isRefillingFood(EntityVillager e) 
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getRefillingFood();
		} catch (NullPointerException ex) {return false; }
	}

	public static void setRefilling(EntityVillager e, boolean b) 
	{
			e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setRefilling(b);
	}
	
	public static boolean isOutsideHomeDist(EntityVillager attacker)
	{
		if(!attacker.isWithinHomeDistanceCurrentPosition()) //not at home?
		{
			if(!VilMethods.getDuty(attacker)) //not on duty?
			{
				return true; //yes he's outside
			}
			return false; //otherwise no.
		}
		else
		{
			return false;
		}
	}

	public static boolean outOfAmmo(EntityVillager villager) 
	{
		return villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getOutOfAmmo();
	}
	
	public static void setOutOfAmmo(EntityVillager villager, boolean noAmmo)
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setIsOutAmmo(noAmmo);
	}

	@Nullable
	public static UUID getPlayerId(EntityVillager e)
	{
		try
		{
			return e.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getPlayerId();
		} catch (NullPointerException ex) {}

		return UUID.randomUUID();
	}

	public static void setPlayerId(EntityPlayer player, EntityVillager entityIn)
	{
		entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerId(player.getUniqueID());
	}

	public static UUID getHomeVillageId(EntityVillager entityIn)
	{
		return entityIn.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getHomeVillageID();
	}

	@Nullable
	public static String getTeam(EntityVillager villager)
	{
		return villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).getTeam();
	}
	
	public static void setTeam(EntityVillager villager, String team)
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setTeam(team);
	}

	public static void setPlayerReputation(EntityVillager villager, UUID uniqueID, float f, int i)
	{
		villager.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null).setPlayerReputation(uniqueID, f, i);
	}

	public static Vec3d asVec3D(BlockPos pos) 
	{
		return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
	}
}
