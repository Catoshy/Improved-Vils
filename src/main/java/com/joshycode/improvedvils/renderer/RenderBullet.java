package com.joshycode.improvedvils.renderer;

import org.lwjgl.opengl.GL11;

import com.joshycode.improvedvils.ImprovedVils;
import com.joshycode.improvedvils.entity.EntityBullet;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBullet<T extends EntityBullet> extends Render<T> {

    public static final ResourceLocation RES_BULLET_DAY = ImprovedVils.location("textures/entities/bullet_day.png");
    public static final ResourceLocation RES_BULLET_NIGHT = ImprovedVils.location("textures/entities/bullet_night.png");
	private ModelBullet model;
    
	public RenderBullet(RenderManager renderManager) 
	{
		super(renderManager);
		this.model = new ModelBullet();
	}
	
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GL11.glPushMatrix();
		this.bindTexture(getEntityTexture(entity));
		//int i = 15728880;
		//int j = i % 65536;
        //int k = i / 65536;
		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k); TODO fun code makes bullet bright but on second thought wouldn't really add anything, commented out for now
		GL11.glTranslated(x, y, z);
		GL11.glRotated(
                entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F,
                0.0F, 1.0F, 0.0F);
        GL11.glRotated(
        		entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
                0.0F, 0.0F, 1.0F);
        this.model.render(entity, (float) x, (float) y, (float) z, entityYaw, partialTicks, .1F);
        GL11.glPopMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(T entity) 
	{
		return RES_BULLET_DAY;
	}

}
