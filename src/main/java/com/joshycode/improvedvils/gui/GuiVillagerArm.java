package com.joshycode.improvedvils.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.entity.EntityVillagerContainer;
import com.joshycode.improvedvils.entity.InventoryHands;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.network.VilEnlistPacket;
import com.joshycode.improvedvils.network.VilFollowPacket;
import com.joshycode.improvedvils.network.VilFollowPacket.VilDutyPacket;
import com.joshycode.improvedvils.network.VilGuardPacket;
import com.joshycode.improvedvils.network.VilStateQuery;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVillagerArm extends GuiContainer {

	private static final ResourceLocation VILLAGER_GUI_TEXTURE = ImprovedVils.location("textures/gui/GuiVillagerArm.png");
	private boolean villagerEnlistState;
	private boolean hasBaton;
	private boolean hungry;
	private int company;
	private int platoon;
	private int vilId;

	private List<GuiButton> enlistedButtons;
	private List<GuiButton> notEnlistedButtons;
	private GuiButton setDutyButtons;
	private GuiButton notDutyButtons;
	private GuiButton guardButtons;
	private GuiButton notGuardButtons;
	private GuiButton followButtons;
	private GuiButton notFollowButtons;
	private Vec3i vec;
	private int dutyState;
	private int followState;
	private int guardState;

	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, InventoryHands villagerHand, int vilId, boolean hasBaton, boolean isEnlisted)
	{
		super(new EntityVillagerContainer(playerInv, villagerInv, villagerHand));
		this.villagerEnlistState = isEnlisted;
		this.hasBaton = hasBaton;
		this.vilId = vilId;
		this.hungry = false;
		this.company = 0;
		this.platoon = 0;
		this.vec = Vec3i.NULL_VECTOR;
		this.enlistedButtons = new ArrayList<GuiButton>();
		this.notEnlistedButtons = new ArrayList<GuiButton>();
	}

	public GuiVillagerArm(InventoryPlayer playerInv, IInventory villagerInv, InventoryHands villagerHand, int vilId, int company , int platoon)
	{
		this(playerInv, villagerInv, villagerHand, vilId, true, true);
		this.company = company;
		this.platoon = platoon;
	}

	@SuppressWarnings("serial")
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.enlistedButtons.clear();
		this.notEnlistedButtons.clear();
		
		this.notGuardButtons = new GuiButton(106, guiLeft + 107, guiTop + 44, 24, 12, "Guard");
		this.guardButtons = new GuiButton(107, guiLeft + 107, guiTop + 44, 24, 12, "Stop");
		this.notFollowButtons = new GuiButton(108, guiLeft + 71, guiTop + 44, 24, 12, "Follow");
		this.followButtons = new GuiButton(109, guiLeft + 71, guiTop + 44, 24, 12, "Stop");
		this.notDutyButtons = new GuiButton(110, guiLeft + 143, guiTop + 60, 24, 12, "On-Duty");
		this.setDutyButtons = new GuiButton(111, guiLeft + 143, guiTop + 60, 24, 12, "Off-Duty");
		
		this.notGuardButtons.visible = false; this.guardButtons.visible = false; this.notFollowButtons.visible = false;
		this.followButtons.visible = false; this.notDutyButtons.enabled = false; this.setDutyButtons.visible = false;
		
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
		this.buttonList.add(this.notGuardButtons);
		this.buttonList.add(this.guardButtons);
		this.buttonList.add(this.notFollowButtons);
		this.buttonList.add(this.followButtons);
		this.buttonList.add(this.notDutyButtons);
		this.buttonList.add(this.setDutyButtons);
		this.queryState();
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
		
		this.updateDutyButtons();
		this.updateGuardButtons();
		this.updateFollowButtons();
		
		if(this.hungry)
			this.fontRenderer.drawString("Hungry!", guiLeft + 136, guiTop + 76, 0);
	}

    private void updateFollowButtons() 
    {
    	if(this.followState == 1)
		{
			//this.buttonList.remove(followButtons);
			this.followButtons.visible = false;
			this.notFollowButtons.visible = true;
			//this.buttonList.add(notFollowButtons);
		}
		else if (this.followState == 2)
		{
			//this.buttonList.remove(notFollowButtons);
			this.notFollowButtons.visible = false;
			this.followButtons.visible = true;
			//this.buttonList.add(followButtons);
		}
		else
		{
			//this.buttonList.remove(notFollowButtons);
			//this.buttonList.remove(followButtons);
			this.followButtons.visible = false;
			this.notFollowButtons.visible = false;
		}
	}

	private void updateGuardButtons() 
    {
    	if(this.guardState == 1)
		{
			this.vec = Vec3i.NULL_VECTOR;
			//this.buttonList.remove(guardButtons);
			this.guardButtons.visible = false;
			this.notGuardButtons.visible = true;
			//this.buttonList.add(notGuardButtons);
		}
		else if(this.guardState == 2)
		{
			//this.buttonList.remove(notGuardButtons);
			this.notGuardButtons.visible = false;
			this.guardButtons.visible = true;
			//this.buttonList.add(guardButtons);
		}
		else
		{
			this.vec = Vec3i.NULL_VECTOR;
			//this.buttonList.remove(notGuardButtons);
			//this.buttonList.remove(guardButtons);
			this.guardButtons.visible = false;
			this.notGuardButtons.visible = false;
		}
	}

	private void updateDutyButtons() 
    {
    	if(this.dutyState == 1)
		{
			//this.buttonList.remove(setDutyButtons);
			this.setDutyButtons.visible = false;
			this.notDutyButtons.enabled = true;
			this.notDutyButtons.visible = true;
			//this.buttonList.add(notDutyButtons);
			this.followState = 0;
			this.guardState = 0;
		}
		else if (this.dutyState == 2)
		{
			//this.buttonList.remove(notDutyButtons);
			this.notDutyButtons.visible = false; //TODO crash from null pntr, this method prolly called b4 init so notDutyButtons are unInited
			this.setDutyButtons.visible = true;
			//this.buttonList.add(setDutyButtons);
		}
		else
		{
			//this.buttonList.remove(notDutyButtons);
			//this.buttonList.remove(setDutyButtons);
			this.notDutyButtons.visible = false;
			this.setDutyButtons.visible = false;
		}
	}

	@Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException
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
					this.enlist();
				}
	        	break;
			case 105:
				if(this.villagerEnlistState)
				{
					this.villagerEnlistState =  false;
	        		this.unEnlist();
				}
	        	break;
			case 106:
	        	this.guardHere(true);
	        	break;
			case 107:
	        	this.guardHere(false);
	        	break;
			case 108:
	        	this.followPlayer(true);
	        	break;
			case 109:
	        	this.followPlayer(false);
	        	break;
	    	case 110:
	        	this.setDuty(true);
	        	break;
		    case 111:
		    	this.setDuty(false);
		    	break;
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

	public void setGuardState(Vec3i pos, int id) 
	{
		if(!pos.equals(Vec3i.NULL_VECTOR))
		{
			this.vec = pos;
		}
		this.guardState = id;
	}

	public void setFollowState(int id)
	{
		this.followState = id;
	}
	
	public void setDutyState(int duty)
	{
		this.dutyState = duty;
	}
	
	public int getVilId()
	{
		return this.vilId;
	}
	
	public void setHungry(boolean hungry) 
	{
		this.hungry = hungry;
	}
	
	private void queryState()
	{
		NetWrapper.NETWORK.sendToServer(new VilStateQuery(this.vilId));
	}
	
	private void guardHere(boolean guard)
	{
    	NetWrapper.NETWORK.sendToServer(new VilGuardPacket(new BlockPos(0, 0, 0), this.vilId, guard));
	}
	
	private void followPlayer(boolean follow)
	{
		NetWrapper.NETWORK.sendToServer(new VilFollowPacket(this.vilId, follow));
	}

	private void setDuty(boolean duty) 
	{
		NetWrapper.NETWORK.sendToServer(new VilDutyPacket(this.vilId, duty));
	}
	
	private void enlist()
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(this.vilId, this.company, this.platoon, true));
	}
	
	private void unEnlist()
	{
		NetWrapper.NETWORK.sendToServer(new VilEnlistPacket(this.vilId, 0, 0, false));
	}
}
