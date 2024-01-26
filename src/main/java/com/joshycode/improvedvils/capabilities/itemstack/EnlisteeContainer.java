package com.joshycode.improvedvils.capabilities.itemstack;

import java.util.UUID;

import com.joshycode.improvedvils.gui.GuiBatonStelling;
import com.joshycode.improvedvils.gui.GuiVillagerRollList;
import com.joshycode.improvedvils.handler.CapabilityHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnlisteeContainer implements IGuiListEntry {
	
	protected final GuiVillagerRollList fatherGui;
	protected final Minecraft mc;
	protected static int fakeId = -1;
	public final UUID villagerUUID;
	public final String name;
	public final int villagerEntityId;
	public final BlockPos villagerPos;
	public final int hungerDaysLeft;
	private boolean isInRenderView;
	public final boolean isActiveDuty;
	public final boolean isFollowing;
	public final boolean isGuarding;
	public final boolean foodStore;
	public final boolean kitStore;
	
	public EnlisteeContainer(GuiVillagerRollList fatherGui, EntityVillager villager, Boolean[] info, int hungerDaysLeft)
	{
		this.fatherGui = fatherGui;
		this.mc = Minecraft.getMinecraft();
		this.villagerUUID = villager.getUniqueID();
		this.name = villager.getName();
		this.villagerEntityId = villager.getEntityId();
		this.villagerPos = villager.getPos();
		this.isActiveDuty = info[0];
		this.isFollowing = info[1];
		this.isGuarding = info[2];
		this.foodStore = info[3];
		this.kitStore = info[4];
		this.hungerDaysLeft = hungerDaysLeft;
		this.isInRenderView = villager.getWorld().isBlockLoaded(villagerPos);
	}
	
	public EnlisteeContainer(GuiVillagerRollList fatherGui, UUID villagerId)
	{
		this.fatherGui = fatherGui;
		this.mc = Minecraft.getMinecraft();
		this.villagerUUID = villagerId;
		this.name = "";
		this.villagerEntityId = fakeId--;
		this.villagerPos = BlockPos.ORIGIN;
		this.isActiveDuty = false;
		this.isInRenderView = false;
		this.isFollowing = false;
		this.isGuarding = false;
		this.foodStore = false;
		this.kitStore = false;
		this.hungerDaysLeft = 0;
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
			boolean isSelected, float partialTicks) 
	{
		String isActive = this.isActiveDuty ? "On Duty" : "In-Active";
		
		FontRenderer font = this.mc.fontRenderer;
		
		boolean line1 = y - 2 + font.FONT_HEIGHT < this.fatherGui.bottom;
		boolean line2 = y + 8 + font.FONT_HEIGHT < this.fatherGui.bottom;
		boolean line3 = y + 18 + font.FONT_HEIGHT < this.fatherGui.bottom;
		
		Entity villager = this.mc.world.getEntityByID(this.villagerEntityId);
		this.isInRenderView = villager != null;
		
		if(!this.isInRenderView)
		{
			if(line1) font.drawString(font.trimStringToWidth(this.name, listWidth - 10), 						x + 3 , y +  2, 0xFFFFFF);
			if(line2) font.drawString(font.trimStringToWidth("Out of Render",	listWidth - 10), 				x + 3 , y + 12, 0xFF2222);
			if(line3) font.drawString(font.trimStringToWidth(this.villagerUUID.toString(), listWidth - 10),		x + 3 , y + 22, 0xCCCCCC);
		}
		else
		{
			String offset	= String.valueOf((int) this.mc.getRenderViewEntity().getDistance(villager));
			String follow 	= this.isFollowing ? "F" : "";
			String guard	= this.isGuarding ? "G" : "";
			
			if(line1) font.drawString(font.trimStringToWidth(this.name,		listWidth - 10), 					x + 3 , y +  2, 0xFFFFFF);
			if(line2) font.drawString(font.trimStringToWidth(this.villagerUUID.toString(), listWidth - 10),		x + 3 , y + 12, 0xCCCCCC);
			if(line3) {
					  font.drawString(isActive, 																x + 3 , y + 22, 0xCCCCCC);
					  font.drawString(offset, 																	x + 52 , y + 22, 0xCCCCCC);
					  font.drawString(follow,																	x + 70, y + 22, 0xCCCCCC);
					  font.drawString(guard,																	x + 82, y + 22, 0xCCCCCC);
					  }
			this.mc.getTextureManager().bindTexture(Gui.ICONS);
			String hungerInfo = this.hungerDaysLeft + " days left";
			int x1 = this.fatherGui.getScrollBarX() - 9;
			int y1 = y + slotHeight - 9;
	        this.fatherGui.getFatherGui().drawTexturedModalRect(x1, y1, 16, 27, 9, 9);
	        if(this.hungerDaysLeft > 3)
	        {
		        this.fatherGui.getFatherGui().drawTexturedModalRect(x1, y1, 52, 27, 9, 9);
	        }
	        else if(this.hungerDaysLeft > 0)
	        {
		        this.fatherGui.getFatherGui().drawTexturedModalRect(x1, y1, 61, 27, 9, 9);
	        }
	        else
	        {
	        	hungerInfo = "Out of food!";
	        }
	        if(mouseX >= x1 && mouseX <= x1 + 9 && mouseY >= y1 && mouseY <= y + slotHeight)
	        	this.fatherGui.getFatherGui().setHoveringWriting(hungerInfo);
	        
	        String storeInfo = "No store";
	        boolean flag = false;
	        if(this.foodStore)
	        {
	        	storeInfo = "Villager has food store";
	        	flag = true;
	        }
	        if(this.kitStore)
	        {
	        	if(!flag)
	        		storeInfo = "Villager has kit store";
	        	else
	        		storeInfo += " & kit store";	
	        }
	        	
	        if(mouseX >= x && mouseX <= x + ((this.fatherGui.getScrollBarX() - x) / 2) && mouseY >= y && mouseY <= y + 12)
	        	this.fatherGui.getFatherGui().setHoveringWriting(storeInfo);

		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) 
	{
		this.fatherGui.addOrSetSelectedSlotIndex(slotIndex);
		return true;
	}

	@Override
	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}
	
	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}
}
