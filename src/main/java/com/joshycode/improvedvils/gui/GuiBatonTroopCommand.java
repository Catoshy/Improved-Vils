package com.joshycode.improvedvils.gui;

import java.io.IOException;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.capabilities.entity.MarshalsBatonCapability.TroopCommands;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiBatonTroopCommand extends GuiScreen {
	
	protected int xSize = 80;
    protected int ySize = 84;
	private int guiLeft;
	private int guiTop;
	private static final ResourceLocation BATON_GUI2_TEXTURE = ImprovedVils.location("textures/gui/GuiBatonCommand.png");

	@Override
	public void initGui()
	{
		this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.guiLeft + 12, this.guiTop + 12, 56, 18, "Charge!"));
		this.buttonList.add(new GuiButton(1, this.guiLeft + 12, this.guiTop + 36, 56, 18, "Go Attack"));
		this.buttonList.add(new GuiButton(2, this.guiLeft + 12, this.guiTop + 60, 56, 18, "Forward Now"));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
		this.mc.getTextureManager().bindTexture(BATON_GUI2_TEXTURE);
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        String header = "Baton Commands";
        int headerWidth = this.fontRenderer.getStringWidth(header);
        this.fontRenderer.drawStringWithShadow(header, (this.width / 2) - (headerWidth / 2), this.guiTop + 2, 14737632);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if(button.id == 0) 
		{
			ImprovedVils.proxy.setTroopCommand(TroopCommands.CHARGE);
			this.mc.displayGuiScreen(null);
		}
		else if(button.id == 1)
		{
			ImprovedVils.proxy.setTroopCommand(TroopCommands.FORWARD_ATTACK);
			this.mc.displayGuiScreen(null);
		}
		else if(button.id == 2)
		{
			ImprovedVils.proxy.setTroopCommand(TroopCommands.FORWARD);
			this.mc.displayGuiScreen(null);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		if(mouseX < this.guiLeft || mouseX > this.guiLeft + this.xSize || mouseY < this.guiTop || mouseY > this.guiTop + this.ySize)
			this.mc.displayGuiScreen(null);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
