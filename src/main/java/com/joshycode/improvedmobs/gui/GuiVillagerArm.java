package com.joshycode.improvedmobs.gui;

import java.io.IOException;

import com.joshycode.improvedmobs.entity.EntityVillagerContainer;
import com.joshycode.improvedmobs.network.NetWrapper;
import com.joshycode.improvedmobs.network.VilEnlistPacket;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVillagerArm extends GuiContainer {
	
	private boolean villagerEnlistState;
	private boolean hasBaton;
	private int company;
	private int platoon;

	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, IInventory villagerHand, boolean hasBaton, boolean isEnlisted) {
		super(new EntityVillagerContainer(playerInv, villagerInv, villagerHand));
		this.villagerEnlistState = isEnlisted;
		this.hasBaton = hasBaton;
		this.company = 0;
		this.platoon = 0;
	}
	
	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, IInventory villagerHand, int company , int platoon) {
		this(playerInv, villagerInv, villagerHand, true, true);
		this.company = company;
		this.platoon = platoon;
	}
	
	public void initGui() {
		super.initGui();
		if(this.hasBaton) {
			if(!this.villagerEnlistState) {
				this.buttonList.add(new GuiButton(100, 44, 20, 6, 12, "<"));
				this.buttonList.add(new GuiButton(101, 62, 20, 6, 12, ">"));
				this.buttonList.add(new GuiButton(102, 44, 40, 6, 12, "<"));
				this.buttonList.add(new GuiButton(103, 62, 40, 6, 12, ">"));
				this.buttonList.add(new GuiButton(104, 44, 62, 16, 12, "Enlist"));
			} else {
				this.buttonList.add(new GuiButton(105, 44, 62, 16, 12, "De-Enlist"));
			}
		}
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// TODO Auto-generated method stub
	}
	
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
		this.fontRenderer.drawString(Integer.toString(company), 53, 20, -1);
		this.fontRenderer.drawString(Integer.toString(platoon), 53, 40, -1);
    }
    
    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException
    {
        if(button.id == 100)
            this.company = this.company <= 0 ? 0 : this.company - 1;
        else if(button.id == 101)
        	this.company = this.company >= 4 ? 4 : this.company + 1;
        else if(button.id == 102)
        	this.platoon = this.platoon <= 0 ? 0 : this.platoon - 1;
        else if(button.id == 103)
        	this.platoon = this.platoon >= 4 ? 4 : this.platoon + 1;
        else if(button.id == 104) {
        	NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(this.mc.player.getEntityId(), this.company, this.platoon, true));
        }else if(button.id == 105) {
        	NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(this.mc.player.getEntityId(), 0, 0, false));

        }
        	
           
    }

	public void setEnlistState(boolean isEnlisted) {
		this.villagerEnlistState = isEnlisted;
	}

}
