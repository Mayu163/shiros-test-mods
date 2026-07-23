package com.shiro193.item;

import com.shiro193.ShiroSTestMod;
import com.shiro193.entity.ModEntities;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final Item FLY_CREEPER_SPAWN_EGG = registerSpawnEgg("fly_creeper_spawn_egg", ModEntities.FLY_CREEPER);
	public static final Item CMD_CREEPER_SPAWN_EGG = registerSpawnEgg("cmd_creeper_spawn_egg", ModEntities.CMD_CREEPER);

	private ModItems() {
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output ->
			output.insertAfter(Items.CREEPER_SPAWN_EGG, FLY_CREEPER_SPAWN_EGG, CMD_CREEPER_SPAWN_EGG)
		);
	}

	private static Item registerSpawnEgg(String name, EntityType<?> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ShiroSTestMod.id(name));
		Item item = new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
