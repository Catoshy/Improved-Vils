package com.joshycode.improvedvils.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBullet extends ModelBase {
	ModelRenderer bullet;
	
	public ModelBullet() 
	{
		super();
		this.bullet = new ModelRenderer(this, 0, 0);
		this.bullet.addBox(0F, 0F, 0F, 1, 1, 1);
		this.bullet.setRotationPoint(0F, 0F, 0F);
		this.bullet.setTextureSize(64, 32);
		this.bullet.mirror = true;
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.bullet.render(scale);
	}
}
