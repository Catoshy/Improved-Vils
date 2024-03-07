package com.joshycode.improvedvils.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiVillagerRollList extends GuiListExtended {

	private GuiBatonStelling fatherGui;
	private ArrayList<EnlisteeContainer> roll;
	private Set<Integer> selectedEnlistees;
	private int lastSelected;

	public GuiVillagerRollList(GuiBatonStelling father, ArrayList<EnlisteeContainer> roll, int listWidth, int listHeight, int slotHeight) 
	{
		super(father.mc, listWidth, listHeight, 0, 0, slotHeight);
		this.selectedEnlistees = new HashSet<>();
		this.fatherGui = father;
		this.roll = roll;
		this.lastSelected = -1;
	}
	
	public void setDimensions()
	{
		int i = (this.fatherGui.width - this.fatherGui.xSize) / 2 + 4;
        int j = (this.fatherGui.height - this.fatherGui.ySize) / 2 + 34;
        this.setSlotXBoundsFromLeft(i);
        this.top = j;
        this.bottom = this.top + this.height;
	}

	@Override
	protected int getSize() 
	{
		return this.roll.size();
	}
	
	public void selectAll() 
	{ 
		for(int i = 0; i < this.roll.size(); i++)
		{
			this.selectedEnlistees.add(i);
		}
		this.lastSelected = -1;
	}
	
	public void unselectAll() 
	{ 
		this.selectedEnlistees.clear();
		this.lastSelected = -1;
	}
	
	public void addOrSetSelectedSlotIndex(int selectedSlotIndexIn)
    {
        if(this.fatherGui.isSelectMultiple())
        {
        	if(GuiScreen.isShiftKeyDown() && this.lastSelected != -1)
        	{
        		int i = this.lastSelected < selectedSlotIndexIn ? 1 : -1;
        		for(int index = this.lastSelected; index != selectedSlotIndexIn; index += i)
        		{
        			this.selectedEnlistees.add(index);
        		}
        		this.selectedEnlistees.add(selectedSlotIndexIn);
        		this.lastSelected = selectedSlotIndexIn;
        	}
        	else
        	{
        		if(this.selectedEnlistees.contains(selectedSlotIndexIn))
        		{
        			this.selectedEnlistees.remove(selectedSlotIndexIn);
        			this.lastSelected = -1;
        		}
        		else
        		{
        			this.selectedEnlistees.add(selectedSlotIndexIn);
        			this.lastSelected = selectedSlotIndexIn;
        		}
        	}
        }
        else
        {
        	this.selectedEnlistees.clear();
        	this.selectedEnlistees.add(selectedSlotIndexIn);
        	this.lastSelected = selectedSlotIndexIn;
        }
    }

	@Override
	public boolean isSelected(int index) 
	{
		return this.selectedEnlistees.contains(index);
	}
	
	public ArrayList<EnlisteeContainer> getRoll()
	{
		return this.roll;
	}
	
	public Set<Integer> getSelected()
	{
		int size = this.roll.size();
		this.selectedEnlistees.removeIf(new Predicate<Integer>() {
			@Override
			public boolean test(Integer t) 
			{
				return t >= size;
			}});
		return this.selectedEnlistees;
	}
	
	public void addContainers(Set<EnlisteeContainer> add)
	{
		for(EnlisteeContainer container : add)
		{
			this.roll.removeIf(new Predicate<EnlisteeContainer>() {

				@Override
				public boolean test(EnlisteeContainer t) 
				{
					if(t.villagerEntityId == container.villagerEntityId || t.villagerUUID == container.villagerUUID)
						return true;
					
					return false;
				}	
			});
		}
		this.roll.addAll(add);
		this.sort();
	}
	
	public void sort()
	{
		this.roll.sort(new Comparator<EnlisteeContainer>()  {

			@Override
			public int compare(EnlisteeContainer arg0, EnlisteeContainer arg1) 
			{
				return arg0.villagerUUID.compareTo(arg1.villagerUUID);
			}
			
		});
	}

	@Override
	protected void drawBackground() {}
	
	@Override
	public void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) 
	{
		int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            int k = insideTop + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;
            int k2 = k;
            int l2 = l;
            
            if (k > this.bottom || k + l < this.top)
            {
                this.updateItemPos(j, insideLeft, k, partialTicks);
                continue;
            }
            
            if(k < this.top)
            {
            	l2 -= this.top - k;
            	k2 = this.top;
            }
            else if(k + l > this.bottom)
            {
            	
            	l2 = this.bottom - k;
            }

            if (this.showSelectionBox && this.isSelected(j))
            {
                int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i1, (double)(k2 + l2 + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(k2 + l2 + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(k2 - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i1, (double)(k2 - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(k2 + l2 + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(k2 + l2 + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(k2 - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(k2 - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);
        }
	}
	
	@Override
	protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)  {}
	
	@Override
    protected int getContentHeight()
    {
        return (this.getSize()) * 35 + 1;
    }

	@Override
	public IGuiListEntry getListEntry(int index) 
	{
		return this.roll.get(index);
	}

	public GuiBatonStelling getFatherGui() 
	{
		return this.fatherGui;
	}
	
	public boolean getEnabled()
	{
		return true;
	}
	
	public int getScrollBarX()
    {
        return this.right - 6;
    }
	
	public int getListWidth()
	{
		return this.width;
	}

	/*
	@Override
	protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) 
	{
		EnlisteeContainer enlistee = this.roll.get(slotIdx);
		String isActive = enlistee.isActiveDuty ? "On Duty" : "In-Active";
		
		FontRenderer font = this.fatherGui.getFontRenderer();
		
		if(!enlistee.isInRenderView)
		{
			font.drawString(font.trimStringToWidth(enlistee.name,		listWidth - 10), this.left + 3 , slotTop +  2, 0xFFFFFF);
            font.drawString(font.trimStringToWidth("Out of Render",		listWidth - (5 + slotBuffer)), this.left + 3 , slotTop + 12, 0xFF2222);
            font.drawString(enlistee.villagerUUID.toString(), 				this.left + 3 , slotTop + 22, 0xCCCCCC);
		}
		else
		{
			String offset	= String.valueOf((int) this.fatherGui.mc.getRenderViewEntity().getDistance(
					this.fatherGui.mc.world.getEntityByID(enlistee.villagerEntityId)));
			font.drawString(font.trimStringToWidth(enlistee.name,		listWidth - 10), this.left + 3 , slotTop +  2, 0xFFFFFF);
			font.drawString(enlistee.villagerUUID.toString(), 				this.left + 3 , slotTop + 12, 0xCCCCCC);
			font.drawString(isActive, 										this.left + 3 , slotTop + 22, 0xCCCCCC);
			font.drawString(offset, 										this.left + 52 , slotTop + 22, 0xCCCCCC);
		}
	}*/
}
