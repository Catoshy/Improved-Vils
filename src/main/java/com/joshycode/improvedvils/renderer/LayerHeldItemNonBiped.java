package com.joshycode.improvedvils.renderer;

import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.EnumHandSide;

public class LayerHeldItemNonBiped extends LayerHeldItem {

	public LayerHeldItemNonBiped(RenderLivingBase<?> livingEntityRendererIn) 
	{
		super(livingEntityRendererIn);
	}

	protected void translateToHand(EnumHandSide p_191361_1_)
    {
        ((ModelVillager)this.livingEntityRenderer.getMainModel()).villagerArms.postRender(0.0625F);
    }
}
