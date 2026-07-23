package com.shiro193.client.render;

import com.shiro193.ShiroSTestMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class CreeperHelmetModel extends EntityModel<CreeperVariantRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
		ShiroSTestMod.id("creeper_helmet"),
		"main"
	);

	private final ModelPart head;

	public CreeperHelmetModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
			PartPose.offset(0.0F, 6.0F, 0.0F)
		);
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(CreeperVariantRenderState state) {
		super.setupAnim(state);
		this.head.yRot = state.yRot * (float)(Math.PI / 180.0);
		this.head.xRot = state.xRot * (float)(Math.PI / 180.0);
	}
}
