package com.shiro193.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class CreeperVariantModel extends EntityModel<CreeperVariantRenderState> {
	private final ModelPart head;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;

	public CreeperVariantModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.leftHindLeg = root.getChild("right_hind_leg");
		this.rightHindLeg = root.getChild("left_hind_leg");
		this.leftFrontLeg = root.getChild("right_front_leg");
		this.rightFrontLeg = root.getChild("left_front_leg");
	}

	@Override
	public void setupAnim(CreeperVariantRenderState state) {
		super.setupAnim(state);
		this.head.yRot = state.yRot * (float)(Math.PI / 180.0);
		this.head.xRot = state.xRot * (float)(Math.PI / 180.0);
		float animationSpeed = state.walkAnimationSpeed;
		float animationPos = state.walkAnimationPos;
		this.rightHindLeg.xRot = Mth.cos(animationPos * 0.6662F) * 1.4F * animationSpeed;
		this.leftHindLeg.xRot = Mth.cos(animationPos * 0.6662F + (float)Math.PI) * 1.4F * animationSpeed;
		this.rightFrontLeg.xRot = Mth.cos(animationPos * 0.6662F + (float)Math.PI) * 1.4F * animationSpeed;
		this.leftFrontLeg.xRot = Mth.cos(animationPos * 0.6662F) * 1.4F * animationSpeed;
	}
}
