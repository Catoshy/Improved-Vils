package com.joshycode.improvedvils.renderer;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBipedVillager extends ModelBiped {
	
	private float staticRotation;

	public ModelBipedVillager(float staticRotation, float scale) {
		super(scale);
		this.staticRotation = staticRotation;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		this.bipedLeftArm.rotateAngleX = this.staticRotation;
		this.bipedRightArm.rotateAngleX = this.staticRotation;
    }
	
}
