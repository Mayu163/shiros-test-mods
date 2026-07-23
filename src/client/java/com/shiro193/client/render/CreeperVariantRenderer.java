package com.shiro193.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public final class CreeperVariantRenderer<T extends Creeper> extends MobRenderer<T, CreeperRenderState, CreeperModel> {
	private static final Identifier VANILLA_CREEPER_TEXTURE = Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");

	public CreeperVariantRenderer(EntityRendererProvider.Context context, boolean renderElytra) {
		super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
		this.addLayer(new CreeperPowerLayer(this, context.getModelSet()));
		if (renderElytra) {
			this.addLayer(new ElytraCreeperLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
		}
	}

	@Override
	protected void scale(CreeperRenderState state, PoseStack poseStack) {
		float swelling = state.swelling;
		float wobble = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
		swelling = Mth.clamp(swelling, 0.0F, 1.0F);
		swelling *= swelling;
		swelling *= swelling;
		float horizontalScale = (1.0F + swelling * 0.4F) * wobble;
		float verticalScale = (1.0F + swelling * 0.1F) / wobble;
		poseStack.scale(horizontalScale, verticalScale, horizontalScale);
	}

	@Override
	protected float getWhiteOverlayProgress(CreeperRenderState state) {
		float swelling = state.swelling;
		return (int)(swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
	}

	@Override
	public Identifier getTextureLocation(CreeperRenderState state) {
		return VANILLA_CREEPER_TEXTURE;
	}

	@Override
	public CreeperRenderState createRenderState() {
		return new CreeperRenderState();
	}

	@Override
	public void extractRenderState(T entity, CreeperRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.swelling = entity.getSwelling(partialTicks);
		state.isPowered = entity.isPowered();
	}
}
