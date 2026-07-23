package com.shiro193.entity;

import com.shiro193.ShiroSTestMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.Vec3;

public final class ModEntities {
	public static final int DEFAULT_CREEPER_WEIGHT = 100;
	public static final int DEFAULT_CREEPER_MIN_GROUP = 4;
	public static final int DEFAULT_CREEPER_MAX_GROUP = 4;

	public static final ResourceKey<EntityType<?>> FLY_CREEPER_KEY = ResourceKey.create(
		Registries.ENTITY_TYPE,
		ShiroSTestMod.id("fly_creeper")
	);
	public static final ResourceKey<EntityType<?>> CMD_CREEPER_KEY = ResourceKey.create(
		Registries.ENTITY_TYPE,
		ShiroSTestMod.id("cmd_creeper")
	);

	public static final EntityType<FlyCreeper> FLY_CREEPER = register(
		FLY_CREEPER_KEY,
		FabricEntityType.Builder.createMob(
				FlyCreeper::new,
				MobCategory.MONSTER,
				builder -> builder
					.defaultAttributes(FlyCreeper::createAttributes)
					.spawnPlacement(
						SpawnPlacementTypes.ON_GROUND,
						Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
						Monster::checkMonsterSpawnRules
					)
			)
			.sized(0.6F, 1.7F)
			.clientTrackingRange(8)
			.notInPeaceful()
	);

	public static final EntityType<CmdCreeper> CMD_CREEPER = register(
		CMD_CREEPER_KEY,
		FabricEntityType.Builder.createMob(
				CmdCreeper::new,
				MobCategory.MONSTER,
				builder -> builder
					.defaultAttributes(CmdCreeper::createAttributes)
					.spawnPlacement(
						SpawnPlacementTypes.ON_GROUND,
						Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
						Monster::checkMonsterSpawnRules
					)
			)
			.sized(0.6F, 1.7F)
			.passengerAttachments(new Vec3(-0.38, 1.58, 0.0), new Vec3(0.38, 1.58, 0.0))
			.clientTrackingRange(8)
			.notInPeaceful()
	);

	private ModEntities() {
	}

	public static void initialize() {
		var vanillaCreeperBiomes = BiomeSelectors.spawnsOneOf(EntityTypes.CREEPER);
		BiomeModifications.create(ShiroSTestMod.id("creeper_spawn_parity")).add(
			ModificationPhase.ADDITIONS,
			vanillaCreeperBiomes,
			context -> context.getMobSpawnSettings()
				.getMobs(MobCategory.MONSTER)
				.stream()
				.filter(entry -> entry.value().type() == EntityTypes.CREEPER)
				.findFirst()
				.ifPresent(vanilla -> {
					MobSpawnSettings.SpawnerData vanillaData = vanilla.value();
					context.getMobSpawnSettings().addSpawn(
						MobCategory.MONSTER,
						new MobSpawnSettings.SpawnerData(FLY_CREEPER, vanillaData.minCount(), vanillaData.maxCount()),
						vanilla.weight()
					);
					context.getMobSpawnSettings().addSpawn(
						MobCategory.MONSTER,
						new MobSpawnSettings.SpawnerData(CMD_CREEPER, vanillaData.minCount(), vanillaData.maxCount()),
						vanilla.weight()
					);
				})
		);
	}

	private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> builder) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}
}
