package com.joshycode.improvedvils.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.opengl.GL14;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.Log;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability;
import com.joshycode.improvedvils.network.BatonSelectData.BatonSelectServerData;
import com.joshycode.improvedvils.network.BlankNotePacket;
import com.joshycode.improvedvils.network.NetWrapper;
import com.joshycode.improvedvils.util.BatonDealMethods;
import com.joshycode.improvedvils.capabilities.itemstack.EnlisteeContainer;
import com.joshycode.improvedvils.network.VillagerListPacket.*;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiBatonStelling extends GuiScreen {

    private static final int STOP = 110;
	private static final int GUARD_NOW = 109;
	private static final int FOLLOW_ME = 108;
	private static final int SET_OFF_DUTY = 107;
	private static final int SET_ON_DUTY = 106;
	private static final int DISMISS = 133;
	private static final int DISMISS_RU_SURE = 134;
	private static final int PLATOON_MORE = 105;
	private static final int PLATOON_LESS = 104;
	private static final int COMPANY_MORE = 103;
	private static final int COMPANY_LESS = 102;
	private static final int KIT = 101;
	private static final int PROVISION = 100;
	private static final int CANCEL_MOVE = 117;
	private static final int MOVE = 116;
	private static final int MOVE_PLATOON_MORE = 115;
	private static final int MOVE_PLATOON_LESS = 114;
	private static final int MOVE_COMPANY_MORE = 113;
	private static final int MOVE_COMPANY_LESS = 112;
	private static final int MOVE_SELECTED = 111;
	private static final int PICK_MANY = 118;
	private static final int UNPICK_ALL = 120;
	private static final int PICK_ALL = 132;
	private static final ResourceLocation BATON_GUI_TEXTURE = ImprovedVils.location("textures/gui/GuiBatonStelling.png");
    protected GuiVillagerRollList rollList;
    private ArrayList<GuiButton> movePickedButtons;
    private GuiButton rawMovePickedButton;
    private GuiButton unpickAllButton;
    private GuiButton pickAllButton;
    private GuiButton pickManyButton;
	private GuiButton dismissButton;
	private GuiButton rawDismissButton;
	private String hoveringWriting;
	protected int xSize = 256;
    protected int ySize = 182;
    protected int guiLeft;
    protected int guiTop;
	private int company;
	private int platoon;
	private int moveCompany;
	private int movePlatoon;
	private int messageCounter; 
	private boolean doSomething;
	private boolean moveThosePicked;
	private boolean pickMany;
	private boolean initialized;
	private long tick;
	
	public GuiBatonStelling(int selectedPlatoon) 
	{
		int remainder = selectedPlatoon % 10;
		this.company = selectedPlatoon / 10;
		this.platoon = remainder;
		this.messageCounter = 0;
		this.doSomething = false;
		this.initialized = false;
		this.pickMany = false;
		this.moveThosePicked = false;
		this.movePickedButtons = new ArrayList<GuiButton>();
		this.tick = -1;
	}

	@SuppressWarnings({ "serial" })
	@Override
	public void initGui()
	{
		super.initGui();
		if(!this.initialized)
	    {
			this.initialized = true;
			ArrayList<EnlisteeContainer> list = new ArrayList<EnlisteeContainer>();
			this.rollList = new GuiVillagerRollList(this, list, 120, 144, 35);
			this.queryEnlistees();
	    }
		this.rollList.setDimensions();
		this.rollList.sort();
		this.buttonList.clear();
		this.movePickedButtons.clear();
		this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
                
        this.pickAllButton = new GuiButton(PICK_ALL, guiLeft + 4, guiTop + 22, 121, 12, "Pick All");
        this.unpickAllButton = new GuiButton(UNPICK_ALL, guiLeft + 4, guiTop + 22, 121, 12, "Unpick All");
        this.pickManyButton = new GuiButton(PICK_MANY, guiLeft + 4, guiTop + 10, 121, 12, "Pick Many");
    	this.rawMovePickedButton = new GuiButton(MOVE_SELECTED, guiLeft + 152, guiTop + 142, 72, 18, "Move Selected");
    	this.dismissButton = new GuiButton(DISMISS_RU_SURE, guiLeft + 127, guiTop + 20, 64, 16, "Sure?");
    	this.dismissButton.enabled = false;
    	this.rawDismissButton = new GuiButton(DISMISS, guiLeft + 127, guiTop + 20, 64, 16, "Dismiss!");

		this.movePickedButtons.addAll(new ArrayList<GuiButton>()
		{
			{
				add(new GuiButton(MOVE_COMPANY_LESS, guiLeft + 158, guiTop + 136, 6, 12, "<"));
				add(new GuiButton(MOVE_COMPANY_MORE, guiLeft + 176, guiTop + 136, 6, 12, ">"));
				add(new GuiButton(MOVE_PLATOON_LESS, guiLeft + 158, guiTop + 160, 6, 12, "<"));
				add(new GuiButton(MOVE_PLATOON_MORE, guiLeft + 176, guiTop + 160, 6, 12, ">"));
				add(new GuiButton(MOVE, guiLeft + 200, guiTop + 154, 32, 12, "Move"));
				add(new GuiButton(CANCEL_MOVE, guiLeft + 200, guiTop + 140, 32, 12, "Cancel"));
			}
		});
		this.buttonList.addAll(new ArrayList<GuiButton>()
		{
			{
				add(new GuiButton(PROVISION, guiLeft + 196, guiTop + 84, 56, 18, "Provision"));
				add(new GuiButton(KIT, guiLeft + 196, guiTop + 106, 56, 18, "Kit"));
				add(new GuiButton(COMPANY_LESS, guiLeft + 212, guiTop + 20, 6, 12, "<"));
				add(new GuiButton(COMPANY_MORE, guiLeft + 230, guiTop + 20, 6, 12, ">"));
				add(new GuiButton(PLATOON_LESS, guiLeft + 212, guiTop + 44, 6, 12, "<"));
				add(new GuiButton(PLATOON_MORE, guiLeft + 230, guiTop + 44, 6, 12, ">"));
				add(new GuiButton(SET_ON_DUTY, guiLeft + 127, guiTop + 38, 64, 16, "Set On-Duty"));
				add(new GuiButton(SET_OFF_DUTY, guiLeft + 127, guiTop + 56, 64, 16, "Set Off-Duty"));
				add(new GuiButton(FOLLOW_ME, guiLeft + 127, guiTop + 74, 64, 16, "Follow Me"));
				add(new GuiButton(GUARD_NOW, guiLeft + 127, guiTop + 92, 64, 16, "Guard Now"));
				add(new GuiButton(STOP, guiLeft + 127, guiTop + 110, 64, 16, "Stop!"));
			}
		});
		this.buttonList.add(this.rawDismissButton);
		if(!this.moveThosePicked)
		{
			this.movePickedButtons.forEach(t -> {t.enabled = false;});
			this.buttonList.add(this.rawMovePickedButton);
		}
		else
		{
			this.rawMovePickedButton.enabled = false;
			this.buttonList.addAll(this.movePickedButtons);
		}
		if(!this.pickMany)
		{
	        this.unpickAllButton.enabled = false;
			this.buttonList.add(this.pickAllButton);
			this.buttonList.add(this.pickManyButton);
		}
		else
		{
			this.pickManyButton.enabled = false;
			this.buttonList.add(this.pickManyButton);
			this.buttonList.add(this.unpickAllButton);
		}
		
		this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
    	this.hoveringWriting = null;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if(this.rollList != null)
        	this.rollList.drawScreen(mouseX, mouseY, partialTicks);
        this.mc.getTextureManager().bindTexture(BATON_GUI_TEXTURE);
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.fontRenderer.drawString("Select a", this.guiLeft + 202, this.guiTop + 64, 0);
        this.fontRenderer.drawString("refill chest", this.guiLeft + 196, this.guiTop + 64 + this.mc.fontRenderer.FONT_HEIGHT, 0);
        this.fontRenderer.drawString("Company", this.guiLeft + 202, this.guiTop + 8, 0);
        this.fontRenderer.drawString("Platoon", this.guiLeft + 202, this.guiTop + 34, 0);
        
		this.fontRenderer.drawString(Integer.toString(this.company + 1), this.guiLeft + 220, this.guiTop + 22, 0);
		String intPlatoon = Integer.toString(this.platoon + 1);
        int widthPla = this.fontRenderer.getStringWidth(intPlatoon);
		this.fontRenderer.drawString(intPlatoon, this.guiLeft + 218 + ((12 - widthPla) / 2), this.guiTop + 46, 0);
		
		if(this.moveThosePicked)
		{
			this.fontRenderer.drawString("Company", this.guiLeft + 148, this.guiTop + 127, 0);
	        this.fontRenderer.drawString("Platoon", this.guiLeft + 148, this.guiTop + 151, 0);
	        
			this.fontRenderer.drawString(Integer.toString(this.moveCompany + 1), this.guiLeft + 168, this.guiTop + 139, 0);
			String intMovePlatoon = Integer.toString(this.movePlatoon + 1);
	        int widthMovePla = this.fontRenderer.getStringWidth(intMovePlatoon);
			this.fontRenderer.drawString(intMovePlatoon, this.guiLeft + 165 + ((12 - widthMovePla) / 2), this.guiTop + 163, 0);
		}
		
		if(this.messageCounter > 0)
		{
			this.fontRenderer.drawString("Too many to move!, nothing done.", mouseX, mouseY, 16711680);
			this.messageCounter--;
		}
		
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        if(this.hoveringWriting != null)
		{
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringWriting)), mouseX, mouseY);
		}
	}
    
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.rollList.handleMouseInput();
    }
    
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.rollList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.rollList.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException
    {
    	//TODO below works in preventing "double click" bug where overlapping buttons wont work on first click, but louder sound steadygoes.
    	if(this.tick == this.mc.world.getWorldTime())
    		return;
    	else
    		this.tick = this.mc.world.getWorldTime();
    	if(button.enabled)
    	{
    		switch(button.id)
    		{
    			case PROVISION:
    				this.doSomething = true;
    				ImprovedVils.proxy.setProvisioningPlatoon(this.company * 10  + this.platoon, MarshalsBatonCapability.Provisions.PROVISIONS);
    				this.mc.displayGuiScreen((GuiScreen)null);
                    this.mc.setIngameFocus();
    				break;
    			case KIT:
    				this.doSomething = true;
    				ImprovedVils.proxy.setProvisioningPlatoon(this.company * 10 + this.platoon, MarshalsBatonCapability.Provisions.KIT);
    				this.mc.displayGuiScreen((GuiScreen)null);
                    this.mc.setIngameFocus();
    				break;
    			case COMPANY_LESS:
		            this.company = this.company <= 0 ? 0 : this.company - 1;
		            this.updatePlatoonInfo();
		            break;
    			case COMPANY_MORE:
		        	this.company = this.company >= 4 ? 4 : this.company + 1;
		            this.updatePlatoonInfo();
		        	break;
    			case PLATOON_LESS:
    				this.platoon = this.platoon <= 0 ? 0 : this.platoon - 1;
		            this.updatePlatoonInfo();
    				break;
    			case PLATOON_MORE:
		        	this.platoon = this.platoon >= 9 ? 9 : this.platoon + 1;
		            this.updatePlatoonInfo();
		        	break;
    			case DISMISS:
    				this.buttonList.remove(this.rawDismissButton);
    				this.rawDismissButton.enabled = false;
    				this.dismissButton.enabled = true;
    				this.buttonList.add(this.dismissButton);
    				break;
    			case DISMISS_RU_SURE:
    				this.buttonList.remove(this.dismissButton);
    				this.dismissButton.enabled = false;
    				this.rawDismissButton.enabled = true;
    				this.buttonList.add(this.rawDismissButton);
    				this.dismissAll();
    				break;
    			case SET_ON_DUTY:
    				this.updatePlatoonDuty(true);
    				break;
    			case SET_OFF_DUTY:
    				this.updatePlatoonDuty(false);
    				break;
    			case FOLLOW_ME:
    				this.platoonFollow();
    				break;
    			case GUARD_NOW:
    				this.platoonGuard();
    				break;
    			case STOP:
    				this.stopAll();
    				break;
    			case MOVE_SELECTED:
    				this.moveThosePicked = true;
    				this.buttonList.remove(this.rawMovePickedButton);
    				this.rawMovePickedButton.enabled = false;
    				this.movePickedButtons.forEach(t -> {t.enabled = true;});
    				this.buttonList.addAll(this.movePickedButtons);
    				break;
    			case MOVE_COMPANY_LESS:
    				this.moveCompany = this.moveCompany <= 0 ? 0 : this.moveCompany - 1;
    				break;
    			case MOVE_COMPANY_MORE:
    				this.moveCompany = this.moveCompany >= 4 ? 4 : this.moveCompany + 1;
    				break;
    			case MOVE_PLATOON_LESS:
    				this.movePlatoon = this.movePlatoon <= 0 ? 0 : this.movePlatoon - 1;
    				break;
    			case MOVE_PLATOON_MORE:
    				this.movePlatoon = this.movePlatoon >= 9 ? 9 : this.movePlatoon + 1;
    				break;
    			case MOVE:
    				this.changePlatoons();
    				this.resetMoveButtons();
    				break;
    			case CANCEL_MOVE:
    				this.resetMoveButtons();
    				break;
    			case PICK_MANY:
    				this.pickMany = true;
    				this.pickManyButton.enabled = false;
    				this.buttonList.remove(this.pickAllButton);
    				this.pickAllButton.enabled = false;
    				this.unpickAllButton.enabled = true;
    				this.buttonList.add(this.unpickAllButton);
    				break;
    			case PICK_ALL:
    				this.pickMany = true;
    				this.pickManyButton.enabled = false;
    				this.rollList.selectAll();
    				this.buttonList.remove(this.pickAllButton);
    				this.pickAllButton.enabled = false;
    				this.unpickAllButton.enabled = true;
    				this.buttonList.add(this.unpickAllButton);
    				break;
    			case UNPICK_ALL:
    				this.pickMany = false;
    				this.pickManyButton.enabled = true;
    				this.rollList.unselectAll();
    				this.buttonList.remove(this.unpickAllButton);
    				this.unpickAllButton.enabled = false;
    				this.pickAllButton.enabled = true;
    				this.buttonList.add(this.pickAllButton);
    				break;
    				
    		}
    	}
    	super.actionPerformed(button);
    }

	public void resetMoveButtons() 
	{
		this.moveThosePicked = false;
		this.buttonList.removeAll(this.movePickedButtons);
		this.buttonList.add(this.rawMovePickedButton);
		this.rawMovePickedButton.enabled = true;
	}
	
	private void dismissAll()
	{
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new DismissVillagers(BatonDealMethods.getEntityIDsFromRollList(this.rollList)));
	}
    
    private void changePlatoons() 
    {
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new MoveVillagersPlatoon(BatonDealMethods.getEntityIDsFromRollList(this.rollList), this.moveCompany * 10 + this.movePlatoon));
    }

	private void stopAll() 
	{
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new StopVillagers(BatonDealMethods.getEntityIDsFromRollList(this.rollList)));
	}

	private void platoonGuard() 
	{
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new GuardVillagers(BatonDealMethods.getEntityIDsFromRollList(this.rollList)));
	}

	private void platoonFollow() 
	{
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new FollowVillagers(BatonDealMethods.getEntityIDsFromRollList(this.rollList)));
	}

	private void updatePlatoonDuty(boolean b)
	{
		if(this.rollList.getSelected().isEmpty()) return;
		NetWrapper.NETWORK.sendToServer(new SetVillagersDuty(BatonDealMethods.getEntityIDsFromRollList(this.rollList), b));
	}

	private void updatePlatoonInfo() 
	{
		NetWrapper.NETWORK.sendToServer(new BatonSelectServerData(this.company * 10 + this.platoon));
	}

	private void queryEnlistees() 
	{
		NetWrapper.NETWORK.sendToServer(new BlankNotePacket.BatonBefolkQuery());
	}
	
	@Override
    public boolean doesGuiPauseGame()
    {
    	return false;
    }
    
    public void onGuiClosed()
    {
    	if(!this.doSomething)
    		ImprovedVils.proxy.setProvisioningPlatoon(-1, null);
    }
    
    FontRenderer getFontRenderer()
    {
        return this.fontRenderer;
    }

	public boolean isSelectMultiple() 
	{
		return this.pickMany;
	}

	public GuiVillagerRollList getRollList() 
	{
		return rollList;
	}

	public void tooManyToMove() 
	{
		this.messageCounter = 120;
	}

	public void setHoveringWriting(String hoveringWriting) 
	{
		this.hoveringWriting = hoveringWriting;
	}
}

/* Sigh...... Wish this worked
public void drawTexturedModalRectWithWindow(int x, int y, int textureX, int textureY, int width, int height, int windowX, int windowY, int windowWidth, int windowHeight)
{
	GlStateManager.enableBlend();
	GlStateManager.glBlendEquation(GL14.GL_FUNC_SUBTRACT);
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    
    float f = 0.00390625F;
    float f1 = 0.00390625F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    bufferbuilder.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
    bufferbuilder.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
    bufferbuilder.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
    bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
    tessellator.draw();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.
    bufferbuilder.pos((double)(windowX + 0), (double)(windowY + windowHeight), (double)this.zLevel).color(1, 1, 1, 0.0F).endVertex();
    bufferbuilder.pos((double)(windowX + windowWidth), (double)(windowY + windowHeight), (double)this.zLevel).color(1, 1, 1, 0.0F).endVertex();
    bufferbuilder.pos((double)(windowX + windowWidth), (double)(windowY + 0), (double)this.zLevel).color(1, 1, 1, 0.0F).endVertex();
    bufferbuilder.pos((double)(windowX + 0), (double)(windowY + 0), (double)this.zLevel).color(1, 1, 1, 0.0F).endVertex();
    tessellator.draw();
    
    GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
	GlStateManager.disableBlend();

}*/
