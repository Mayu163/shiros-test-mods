package com.shiro193.client.render;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.Identifier;

final class CreeperVariantPowerLayer extends EnergySwirlLayer<CreeperVariantRenderState, CreeperVariantModel> {
	private static final Identifier VANILLA_POWER_TEXTURE =
		Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
	private final CreeperVariantModel model;

	CreeperVariantPowerLayer(
		RenderLayerParent<CreeperVariantRenderState, CreeperVariantModel> parent,
		EntityModelSet modelSet
	) {
		super(parent);
		this.model = new CreeperVariantModel(modelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
	}

	@Override
	protected boolean isPowered(CreeperVariantRenderState state) {
		return state.isPowered;
	}

	@Override
	protected float xOffset(float ticks) {
		return ticks * 0.01F;
	}

	@Override
	protected Identifier getTextureLocation() {
		return VANILLA_POWER_TEXTURE;
	}

	@Override
	protected CreeperVariantModel model() {
		return this.model;
	}
}
