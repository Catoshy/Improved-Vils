package com.joshycode.improvedmobs;

import com.joshycode.improvedmobs.capabilities.CapabilityStorage;
import com.joshycode.improvedmobs.capabilities.entity.IVilPlayerIdCapability;
import com.joshycode.improvedmobs.capabilities.entity.VilPlayerCapabilityFactory;
import com.joshycode.improvedmobs.capabilities.itemstack.IMarshalsBatonCapability;
import com.joshycode.improvedmobs.capabilities.itemstack.MarshalsBatonCapabilityFactory;
import com.joshycode.improvedmobs.entity.EntityVillagerContainer;
import com.joshycode.improvedmobs.entity.InventoryHands;
import com.joshycode.improvedmobs.entity.ai.VillagerAIAttackMelee;
import com.joshycode.improvedmobs.entity.ai.VillagerAIShootRanged;
import com.joshycode.improvedmobs.gui.GuiVillagerArm;
import com.joshycode.improvedmobs.handler.VillagerCapabilityHandler;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilEnlistPacket;
import com.joshycode.improvedmobs.util.InventoryUtil;
import com.joshycode.improvedmobs.util.Pair;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler {
		
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
        		IVilPlayerIdCapability.class,
        		new CapabilityStorage(),
                new VilPlayerCapabilityFactory()
        );
        CapabilityManager.INSTANCE.register(
        		IMarshalsBatonCapability.class,
        		new CapabilityStorage(),
        		new MarshalsBatonCapabilityFactory()
        );
    }
    
    public static void registerPackets() {
    	NetWrapper.NETWORK.registerMessage(VilEnlistPacket.Handler.class, VilEnlistPacket.class, 0, Side.SERVER);
    }
    
	public static void openVillagerGUI(EntityPlayer player, World world, EntityVillager entityIn) {
		if(!entityIn.isChild()) {
			entityIn.tasks.taskEntries.forEach(t -> {
				if(t.action instanceof VillagerAIAttackMelee) {
					((VillagerAIAttackMelee) t.action).setPlayer(player.getUniqueID());
				}
			});
			player.openGui(ImprovedVils.instance, 100, world, entityIn.getEntityId(), 0, 0);
		}
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{//TODO change id from 100 to something that makes more sense like a static final var
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			InventoryHands equipInv = new InventoryHands(e, "Hands", false);
			
			e.tasks.taskEntries.forEach(t -> {
				if(t.action instanceof VillagerAIShootRanged) {
					if(!((VillagerAIShootRanged)t.action).isListening()) {
						e.getVillagerInventory().addInventoryChangeListener((IInventoryChangedListener) t.action);
						((VillagerAIShootRanged) t.action).setListening();
					}
					/** the hand/armor inventory is destroyed every time it closes so listeners must be added
					 * every time it's called*/
					equipInv.addInventoryChangeListener((IInventoryChangedListener) t.action);
				}
			});
			
			gui = new EntityVillagerContainer(player.inventory, e.getVillagerInventory(), equipInv);
		}
		return gui;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		Object gui = null;
		if(ID == 100 && world.getEntityByID(x) instanceof EntityVillager)
		{
			EntityVillager e = (EntityVillager) world.getEntityByID(x);
			if(InventoryUtil.doesInventoryHaveItem(player.inventory, Items.STICK) == 1/*TODO make Marshal Baton*/) {
				ItemStack stack = InventoryUtil.getStackByItem(player.inventory, Items.STICK); //TODO
				IMarshalsBatonCapability cap = stack.getCapability(VillagerCapabilityHandler.MARSHALS_BATON_CAPABILITY, null);
				if(cap != null) {
					Pair<Integer, Integer> p = cap.getVillagerPlace(e.getUniqueID());
					if(p != null) {
						gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), p.a, p.b);
					}
				} else {
					gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), true, false);
				}
			} else {
				gui = new GuiVillagerArm(player.inventory, e.getVillagerInventory(), new InventoryHands(e, "Hands"), false, false);
			}
		}
		return gui;
	}

}
