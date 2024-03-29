package com.joshycode.improvedvils.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.entity.EntityVillagerContainer;
import com.joshycode.improvedvils.entity.InventoryHands;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVillagerArm extends GuiContainer {

	private static final ResourceLocation VILLAGER_GUI_TEXTURE = ImprovedVils.location("textures/gui/GuiVillagerArm.png");
	private boolean villagerEnlistState;
	private boolean hasBaton;
	private boolean guard;
	private boolean follow;
	private int company;
	private int platoon;
	private int vilId;

	private List<GuiButton> enlistedButtons;
	private List<GuiButton> notEnlistedButtons;
	private List<GuiButton> guardButtons;
	private List<GuiButton> notGuardButtons;
	private List<GuiButton> followButtons;
	private List<GuiButton> notFollowButtons;
	private Vec3i vec;

	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, InventoryHands villagerHand, int vilId, boolean hasBaton, boolean isEnlisted)
	{
		super(new EntityVillagerContainer(playerInv, villagerInv, villagerHand));
		this.villagerEnlistState = isEnlisted;
		this.hasBaton = hasBaton;
		this.vilId = vilId;
		this.guard = false;
		this.follow = false;
		this.company = 0;
		this.platoon = 0;
		this.vec = Vec3i.NULL_VECTOR;
		this.enlistedButtons = new ArrayList();
		this.notEnlistedButtons = new ArrayList();
		this.guardButtons = new ArrayList();
		this.notGuardButtons = new ArrayList();
		this.followButtons = new ArrayList();
		this.notFollowButtons = new ArrayList();
	}

	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, InventoryHands villagerHand, int vilId, int company , int platoon)
	{
		this(playerInv, villagerInv, villagerHand, vilId, true, true);
		this.company = company;
		this.platoon = platoon;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.notGuardButtons.add(new GuiButton(106, guiLeft + 107, guiTop + 44, 24, 12, "Guard"));
		this.guardButtons.add(new GuiButton(107, guiLeft + 107, guiTop + 44, 24, 12, "Stop"));
		this.notFollowButtons.add(new GuiButton(108, guiLeft + 71, guiTop + 44, 24, 12, "Follow"));
		this.followButtons.add(new GuiButton(109, guiLeft + 71, guiTop + 44, 24, 12, "Stop"));
		this.enlistedButtons.add(new GuiButton(105, guiLeft + 143, guiTop + 44, 24, 12, "De-Enlist"));

		this.notEnlistedButtons.addAll(new ArrayList<GuiButton>()
		{
			{
				add(new GuiButton(100, guiLeft + 143, guiTop + 8, 6, 12, "<"));
				add(new GuiButton(101, guiLeft + 161, guiTop + 8, 6, 12, ">"));
				add(new GuiButton(102, guiLeft + 143, guiTop + 26, 6, 12, "<"));
				add(new GuiButton(103, guiLeft + 161, guiTop + 26, 6, 12, ">"));
				add(new GuiButton(104, guiLeft + 143, guiTop + 44, 24, 12, "Enlist"));
			}
		});
		if(this.hasBaton)
		{
			if(!this.villagerEnlistState)
			{
				this.buttonList.addAll(notEnlistedButtons);
			}
			else
			{
				this.buttonList.addAll(enlistedButtons);
			}
		}
		ClientProxy.queryState(vilId);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(VILLAGER_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize + 16);
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
		this.fontRenderer.drawString(Integer.toString(company + 1), guiLeft + 152, guiTop + 8, 0);
		this.fontRenderer.drawString(Integer.toString(platoon + 1), guiLeft + 152, guiTop + 26, 0);
		this.fontRenderer.drawString("X: " + Integer.toString(this.vec.getX()), guiLeft + 107, guiTop + 8, 0);
		this.fontRenderer.drawString("Y: " + Integer.toString(this.vec.getY()), guiLeft + 107, guiTop + 17, 0);
		this.fontRenderer.drawString("Z: " + Integer.toString(this.vec.getZ()), guiLeft + 107, guiTop + 26, 0);
	}

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException
    {
    	if(button.enabled)
    	{
    		switch(button.id)
    		{
    			case 100:
		            this.company = this.company <= 0 ? 0 : this.company - 1;
		            break;
    			case 101:
		        	this.company = this.company >= 4 ? 4 : this.company + 1;
		        	break;
    			case 102:
    				this.platoon = this.platoon <= 0 ? 0 : this.platoon - 1;
    				break;
    			case 103:
		        	this.platoon = this.platoon >= 9 ? 9 : this.platoon + 1;
		        	break;
    			case 104:
    				if(!this.villagerEnlistState)
    				{
		        		this.villagerEnlistState = true;
    					ClientProxy.enlist(this.vilId, this.company, this.platoon);
    				}
		        	break;
    			case 105:
    				if(this.villagerEnlistState)
    				{
    					this.villagerEnlistState =  false;
		        		ClientProxy.unEnlist(this.vilId);
    				}
		        	break;
    			case 106:
    				if(!this.guard)
    				{
    					this.guard = true;
		        		ClientProxy.guardHere(this.vilId, true);
    				}
		        	break;
    			case 107:
    				if(this.guard)
    				{
    					this.guard = false;
		        		ClientProxy.guardHere(this.vilId, false);
    				}
		        	break;
    			case 108:
    				if(!this.follow)
    				{
    					this.follow = true;
		        		ClientProxy.followPlayer(this.vilId, true);
    				}
		        	break;
    			case 109:
    				if(this.follow)
    				{
    					this.follow = false;
		        		ClientProxy.followPlayer(this.vilId, false);
    				}
		        	break;
		        }
    		}
    	super.actionPerformed(button);
    }

	public void setEnlistState(boolean isEnlisted, int company, int platoon)
	{
		this.villagerEnlistState = isEnlisted;
		if(!isEnlisted)
		{
			this.company = 0;
			this.platoon = 0;
			this.buttonList.removeAll(enlistedButtons);
			this.buttonList.addAll(notEnlistedButtons);
		}
		else
		{
			this.company = company;
			this.platoon = platoon;
			this.buttonList.removeAll(notEnlistedButtons);
			this.buttonList.addAll(enlistedButtons);
		}
	}

	public void setGuardState(Vec3i pos, int id) {
		if(!pos.equals(Vec3i.NULL_VECTOR))
		{
			this.vec = pos;
		}
		if(id == 1)
		{
			this.guard = false;
			this.buttonList.removeAll(guardButtons);
			this.buttonList.addAll(notGuardButtons);
		}
		else if(id == 2)
		{
			this.guard = true;
			this.buttonList.removeAll(notGuardButtons);
			this.buttonList.addAll(guardButtons);
		}
		else
		{
			this.guard = false;
			this.buttonList.removeAll(notGuardButtons);
			this.buttonList.removeAll(guardButtons);
		}
	}

	public void setFollowState(int int2)
	{
		if(int2 == 1)
		{
			this.follow = false;
			this.buttonList.removeAll(followButtons);
			this.buttonList.addAll(notFollowButtons);
		}
		else if (int2 == 2)
		{
			this.follow = true;
			this.buttonList.removeAll(notFollowButtons);
			this.buttonList.addAll(followButtons);
		}
		else
		{
			this.follow = false;
			this.buttonList.removeAll(notFollowButtons);
			this.buttonList.removeAll(followButtons);
		}
	}
	
	public int getVilId()
	{
		return vilId;
	}
}
