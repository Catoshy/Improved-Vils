package com.joshycode.improvedvils.handler;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.joshycode.improvedvils.capabilities.entity.IImprovedVilCapability;
import com.joshycode.improvedvils.util.VillagerPlayerDealMethods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

public abstract class VillagerPredicate<T extends Entity> implements Predicate<T> {

	protected EntityVillager taskOwner;

	protected VillagerPredicate(EntityVillager taskOwner) 
	{
		super();
		this.taskOwner = taskOwner;
	}
	
	public static class FriendlyFireVillagerPredicate<T extends Entity> extends VillagerPredicate<T> {
		 
		protected FriendlyFireVillagerPredicate(EntityVillager taskOwner) { super(taskOwner); }

		@Override
		public boolean apply(@Nullable T potentialEnemy)
        {
			if((!(potentialEnemy instanceof EntityVillager) && !(potentialEnemy instanceof EntityPlayer)) || potentialEnemy.isEntityEqual(this.taskOwner)) return false;
			
			String team = "";
			IImprovedVilCapability taskOwnerCapability =  this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			
			if(potentialEnemy instanceof EntityVillager)
			{
				IImprovedVilCapability predCapability = potentialEnemy.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
				team = predCapability.getTeam();
			}
			//TODO changes perhaps for attacking those who hurt the player? ITW
			else if(((EntityPlayer) potentialEnemy).getTeam() != null)
			{
				team = ((EntityPlayer) potentialEnemy).getTeam().getName();
			}
			
			return team.isEmpty() || taskOwnerCapability.getTeam().isEmpty() || taskOwnerCapability.getTeam().equals(team);
        }
	}
	
	public static class EnemyVillagerAttackPredicate<T extends EntityVillager> extends VillagerPredicate<T> {
	
		protected EnemyVillagerAttackPredicate(EntityVillager taskOwner) { super(taskOwner); }
		
		@Override
		public boolean apply(T potentialEnemyVil)
        {
			IImprovedVilCapability taskOwnerCapability =  this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			IImprovedVilCapability potentCapability = potentialEnemyVil.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			return !potentCapability.getTeam().isEmpty() && !taskOwnerCapability.getTeam().isEmpty() && !taskOwnerCapability.getTeam().equals(potentCapability.getTeam());
	 	}
	}
	
	public static class EnemyPlayerAttackPredicate<T extends Entity> extends VillagerPredicate<T> {
		
		protected EnemyPlayerAttackPredicate(EntityVillager taskOwner) { super(taskOwner); }

		@Override
		public boolean apply(@Nullable T entity)
        {
			if(!(entity instanceof EntityPlayer)) return false;
			
			IImprovedVilCapability taskOwnerCapability =  this.taskOwner.getCapability(CapabilityHandler.VIL_PLAYER_CAPABILITY, null);
			return !taskOwnerCapability.getTeam().isEmpty() && entity.getTeam() != null && !taskOwnerCapability.getTeam().equals(entity.getTeam().getName()) && checkPlayerRelations(taskOwnerCapability, (EntityPlayer) entity);
        }
		 
		private boolean checkPlayerRelations(IImprovedVilCapability taskOwnerCapability, EntityPlayer player)
		{
			float reputation = taskOwnerCapability.getPlayerReputation(player.getUniqueID());
			return reputation < 2 && (!taskOwnerCapability.isMutinous() || reputation < VillagerPlayerDealMethods.UNBEARABLE_THRESHOLD);
		}
	}
}