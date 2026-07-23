package com.shiro193.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

final class ElytraCreeperLayer extends RenderLayer<CreeperRenderState, CreeperModel> {
	private final ElytraModel elytraModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	ElytraCreeperLayer(
		RenderLayerParent<CreeperRenderState, CreeperModel> parent,
		EntityModelSet modelSet,
		EquipmentLayerRenderer equipmentRenderer
	) {
		super(parent);
		this.elytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
		this.equipmentRenderer = equipmentRenderer;
	}

	@Override
	public void submit(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CreeperRenderState state,
		float yRot,
		float xRot
	) {
		ItemStack elytra = Items.ELYTRA.getDefaultInstance();
		Equippable equippable = elytra.get(DataComponents.EQUIPPABLE);
		if (equippable == null || equippable.assetId().isEmpty()) {
			return;
		}

		ResourceKey<EquipmentAsset> asset = equippable.assetId().get();
		HumanoidRenderState wingState = new HumanoidRenderState();
		wingState.chestEquipment = elytra;
		wingState.isFallFlying = true;
		wingState.elytraRotX = (float)(Math.PI / 9.0);
		wingState.elytraRotY = 0.0F;
		wingState.elytraRotZ = (float)(-Math.PI / 2.0);
		wingState.outlineColor = state.outlineColor;
		this.elytraModel.setupAnim(wingState);

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.375F, 0.125F);
		this.equipmentRenderer.renderLayers(
			EquipmentClientInfo.LayerType.WINGS,
			asset,
			this.elytraModel,
			wingState,
			elytra,
			poseStack,
			submitNodeCollector,
			lightCoords,
			state.outlineColor
		);
		poseStack.popPose();
	}
}
