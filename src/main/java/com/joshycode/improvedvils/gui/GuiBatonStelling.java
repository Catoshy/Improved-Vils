package com.joshycode.improvedvils.gui;

import java.io.IOException;
import java.util.ArrayList;

import com.joshycode.improvedvils.ClientProxy;
import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.itemstack.MarshalsBatonCapability;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiBatonStelling extends GuiScreen {

    private static final ResourceLocation BATON_GUI_TEXTURE = ImprovedVils.location("textures/gui/GuiBatonStelling.png");
	protected int xSize = 176;
    protected int ySize = 166;
    protected int guiLeft;
    protected int guiTop;
	private int company;
	private int platoon;
	private boolean doSomething;
	
	public GuiBatonStelling(int selectedPlatoon) 
	{
		int remainder = selectedPlatoon % 10;
		this.company = selectedPlatoon / 10;
		this.platoon = remainder;
		this.doSomething = false;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList.addAll(new ArrayList<GuiButton>()
		{
			{
				add(new GuiButton(100, guiLeft + 24, guiTop + 20, 64, 12, "Provision"));
				add(new GuiButton(101, guiLeft + 24, guiTop + 38, 64, 12, "Kit"));
				add(new GuiButton(102, guiLeft + 143, guiTop + 20, 6, 12, "<"));
				add(new GuiButton(103, guiLeft + 161, guiTop + 20, 6, 12, ">"));
				add(new GuiButton(104, guiLeft + 143, guiTop + 46, 6, 12, "<"));
				add(new GuiButton(105, guiLeft + 161, guiTop + 46, 6, 12, ">"));
			}
		});
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BATON_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize + 16);
        this.fontRenderer.drawString("Select a refill chest", guiLeft + 8, guiTop + 8, 0);
        this.fontRenderer.drawString("Company", guiLeft + 130, guiTop + 8, 0);
        this.fontRenderer.drawString("Platoon", guiLeft + 136, guiTop + 34, 0);
		this.fontRenderer.drawString(Integer.toString(company + 1), guiLeft + 152, guiTop + 20, 0);
		this.fontRenderer.drawString(Integer.toString(platoon + 1), guiLeft + 152, guiTop + 46, 0);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException
    {
    	if(button.enabled)
    	{
    		switch(button.id)
    		{
    			case 100:
    				this.doSomething = true;
    				ImprovedVils.proxy.setProvisioningPlatoon((10 * this.company) + this.platoon, MarshalsBatonCapability.Provisions.PROVISIONS);
    				this.mc.displayGuiScreen((GuiScreen)null);
                    this.mc.setIngameFocus();
    				break;
    			case 101:
    				this.doSomething = true;
    				ImprovedVils.proxy.setProvisioningPlatoon((10 * this.company) + this.platoon, MarshalsBatonCapability.Provisions.KIT);
    				this.mc.displayGuiScreen((GuiScreen)null);
                    this.mc.setIngameFocus();
    				break;
    			case 102:
		            this.company = this.company <= 0 ? 0 : this.company - 1;
		            break;
    			case 103:
		        	this.company = this.company >= 4 ? 4 : this.company + 1;
		        	break;
    			case 104:
    				this.platoon = this.platoon <= 0 ? 0 : this.platoon - 1;
    				break;
    			case 105:
		        	this.platoon = this.platoon >= 9 ? 9 : this.platoon + 1;
		        	break;
		        	
    		}
    	}
    	super.actionPerformed(button);
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
}
