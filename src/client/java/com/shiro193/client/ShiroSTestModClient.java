package com.shiro193.client;

import com.shiro193.ShiroSTestMod;
import com.shiro193.client.render.CreeperVariantRenderer;
import com.shiro193.client.render.CreeperHelmetModel;
import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import com.shiro193.entity.SummonCreeper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class ShiroSTestModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(CreeperHelmetModel.LAYER, CreeperHelmetModel::createBodyLayer);
		EntityRendererRegistry.register(
			ModEntities.FLY_CREEPER,
			context -> new CreeperVariantRenderer<FlyCreeper>(context, true)
		);
		EntityRendererRegistry.register(
			ModEntities.CMD_CREEPER,
			context -> new CreeperVariantRenderer<CmdCreeper>(context, false)
		);
		EntityRendererRegistry.register(
			ModEntities.SUMMON_CREEPER,
			context -> new CreeperVariantRenderer<SummonCreeper>(context, false)
		);
		ShiroSTestMod.LOGGER.info(
			"Registered vanilla-asset renderers for Fly Creeper, CMD Creeper, and Summon Creeper."
		);
	}
}
