package com.joshycode.improvedvils.renderer;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerArmourNonBiped extends LayerBipedArmor {

	public LayerArmourNonBiped(RenderLivingBase<?> rendererIn) 
	{
		super(rendererIn);
	}
	
	@Override
	protected void initArmor()
    {
        this.modelLeggings = new ModelBiped(0.48F);
        this.modelArmor = new ModelBiped(.98F);
        this.modelArmor.bipedHead.offsetY += .2F;
        this.modelArmor.bipedLeftArm.rotateAngleY = -45F;
    }

}
