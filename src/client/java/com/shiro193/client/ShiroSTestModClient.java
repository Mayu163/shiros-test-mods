package com.shiro193.client;

import com.shiro193.ShiroSTestMod;
import com.shiro193.client.render.CreeperVariantRenderer;
import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ShiroSTestModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(
			ModEntities.FLY_CREEPER,
			context -> new CreeperVariantRenderer<FlyCreeper>(context, true)
		);
		EntityRendererRegistry.register(
			ModEntities.CMD_CREEPER,
			context -> new CreeperVariantRenderer<CmdCreeper>(context, false)
		);
		ShiroSTestMod.LOGGER.info("Registered vanilla-asset renderers for Fly Creeper and CMD Creeper.");
	}
}
