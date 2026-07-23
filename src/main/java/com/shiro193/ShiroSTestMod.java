package com.shiro193;

import com.shiro193.entity.ModEntities;
import com.shiro193.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiroSTestMod implements ModInitializer {
	public static final String MOD_ID = "shiros-test-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntities.initialize();
		ModItems.initialize();
		LOGGER.info("Registered Fly Creeper, CMD Creeper, their spawn eggs, and vanilla-creeper spawn parity.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
