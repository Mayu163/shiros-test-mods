package com.shiro193.test;

import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import com.shiro193.item.ModItems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.Vec3;

public final class ShiroEntityGameTests {
	@GameTest(maxTicks = 1)
	public void registrationsAndVanillaSpawnParity(GameTestHelper helper) {
		require(helper, ModEntities.FLY_CREEPER.getCategory() == MobCategory.MONSTER, "Fly Creeper is not a monster.");
		require(helper, ModEntities.CMD_CREEPER.getCategory() == MobCategory.MONSTER, "CMD Creeper is not a monster.");
		require(
			helper,
			SpawnPlacements.getPlacementType(ModEntities.FLY_CREEPER) == SpawnPlacements.getPlacementType(EntityTypes.CREEPER),
			"Fly Creeper placement differs from vanilla Creeper."
		);
		require(
			helper,
			SpawnPlacements.getPlacementType(ModEntities.CMD_CREEPER) == SpawnPlacements.getPlacementType(EntityTypes.CREEPER),
			"CMD Creeper placement differs from vanilla Creeper."
		);
		require(
			helper,
			SpawnPlacements.getHeightmapType(ModEntities.FLY_CREEPER) == SpawnPlacements.getHeightmapType(EntityTypes.CREEPER),
			"Fly Creeper heightmap differs from vanilla Creeper."
		);
		require(
			helper,
			SpawnPlacements.getHeightmapType(ModEntities.CMD_CREEPER) == SpawnPlacements.getHeightmapType(EntityTypes.CREEPER),
			"CMD Creeper heightmap differs from vanilla Creeper."
		);
		require(
			helper,
			SpawnEggItem.spawnsEntity(new ItemStack(ModItems.FLY_CREEPER_SPAWN_EGG), ModEntities.FLY_CREEPER),
			"Fly Creeper spawn egg points to the wrong entity."
		);
		require(
			helper,
			SpawnEggItem.spawnsEntity(new ItemStack(ModItems.CMD_CREEPER_SPAWN_EGG), ModEntities.CMD_CREEPER),
			"CMD Creeper spawn egg points to the wrong entity."
		);

		int creeperBiomeCount = 0;
		boolean foundDefaultCreeperEntry = false;
		for (Holder.Reference<Biome> biome : helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).listElements().toList()) {
			List<Weighted<MobSpawnSettings.SpawnerData>> entries = biome.value()
				.getMobSettings()
				.getMobs(MobCategory.MONSTER)
				.unwrap();
			Optional<Weighted<MobSpawnSettings.SpawnerData>> vanilla = findSpawn(entries, EntityTypes.CREEPER);
			if (vanilla.isEmpty()) {
				continue;
			}

			creeperBiomeCount++;
			if (vanilla.get().weight() == ModEntities.DEFAULT_CREEPER_WEIGHT
				&& vanilla.get().value().minCount() == ModEntities.DEFAULT_CREEPER_MIN_GROUP
				&& vanilla.get().value().maxCount() == ModEntities.DEFAULT_CREEPER_MAX_GROUP) {
				foundDefaultCreeperEntry = true;
			}
			assertSpawnMatches(helper, entries, vanilla.get(), ModEntities.FLY_CREEPER, "Fly Creeper");
			assertSpawnMatches(helper, entries, vanilla.get(), ModEntities.CMD_CREEPER, "CMD Creeper");
		}

		require(helper, creeperBiomeCount > 0, "No vanilla Creeper biome was available for parity testing.");
		require(helper, foundDefaultCreeperEntry, "The standard vanilla Creeper 100 / 4-4 spawn entry was not found.");
		helper.succeed();
	}

	@GameTest(maxTicks = 110, skyAccess = true)
	public void cmdCreeperCarriesAtMostTwoAndThrowsBothInGravityArcs(GameTestHelper helper) {
		CmdCreeper cmd = helper.spawn(ModEntities.CMD_CREEPER, new Vec3(1.0, 2.0, 1.0), EntitySpawnReason.NATURAL);
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new Vec3(8.0, 2.0, 1.0), EntitySpawnReason.SPAWN_ITEM_USE);
		villager.setNoAi(true);
		villager.setInvulnerable(true);
		villager.setNoGravity(true);
		cmd.setNoGravity(true);

		require(helper, cmd.getPayloadCount() == CmdCreeper.MAX_PAYLOAD, "CMD Creeper did not spawn with exactly two payloads.");
		List<FlyCreeper> initialPayloads = cmd.getPassengers()
			.stream()
			.map(FlyCreeper.class::cast)
			.toList();
		require(
			helper,
			initialPayloads.stream().allMatch(payload -> payload.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)),
			"A CMD payload was not equipped with Elytra."
		);

		FlyCreeper thirdPayload = helper.spawn(ModEntities.FLY_CREEPER, new Vec3(1.0, 2.0, 2.0), EntitySpawnReason.SPAWN_ITEM_USE);
		require(helper, !cmd.tryLoad(thirdPayload), "CMD Creeper accepted a third payload.");
		thirdPayload.discard();

		helper.succeedWhen(() -> {
			require(
				helper,
				cmd.getTotalThrown() == 2,
				"CMD Creeper has not thrown both payloads (thrown=" + cmd.getTotalThrown()
					+ ", payloads=" + cmd.getPayloadCount()
					+ ", alive=" + cmd.isAlive()
					+ ", target=" + cmd.hasVillageTarget()
					+ ", goalTicks=" + cmd.getThrowGoalTicks()
					+ ", distanceSq=" + cmd.getLastTargetDistanceSqr() + ")."
			);
			require(helper, cmd.getPayloadCount() == 0, "CMD Creeper still holds a payload after two throws.");
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::wasLaunched),
				"A carried Fly Creeper was not launched."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(payload -> payload.getInitialLaunchSpeed() >= 1.2),
				"A thrown Fly Creeper did not receive the high initial launch speed."
			);
			require(helper, cmd.getLastPredictedHitTicks() > 0, "CMD Creeper did not calculate a predicted hit time.");
			require(
				helper,
				initialPayloads.stream().allMatch(payload -> payload.getPredictedImpactTicks() > 0),
				"A thrown Fly Creeper did not receive the CMD Creeper's predicted hit time."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getScheduledFuseIgnitionTick()
						== Math.max(1, payload.getPredictedImpactTicks() - 30 + 1)
				),
				"A thrown Fly Creeper's fuse schedule did not match its predicted hit time."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getFuseIgnitedAtLaunchTick() == payload.getScheduledFuseIgnitionTick()
				),
				"A thrown Fly Creeper did not ignite at its CMD-predicted fuse tick."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(payload -> payload.getTakeoffFireworkEffectsTriggered() == 1),
				"A thrown Fly Creeper did not trigger exactly one takeoff firework effect."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffFireworkBurstsEmitted() == FlyCreeper.TAKEOFF_FIREWORK_DURATION_TICKS
				),
				"A thrown Fly Creeper did not emit its complete long firework trail."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffFireworkParticlesEmitted()
						>= FlyCreeper.TAKEOFF_FIREWORK_DURATION_TICKS
							* (
								FlyCreeper.TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK
									+ FlyCreeper.TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK
							)
				),
				"A thrown Fly Creeper's firework trail was not dense enough."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::hasReachedBallisticApex),
				"A thrown Fly Creeper did not reach the apex of its curved trajectory."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getPredictedTrajectoryHeightMultiplier() >= 1.28
						&& payload.getPredictedTrajectoryHeightMultiplier() <= 1.32
				),
				"A thrown Fly Creeper's predicted apex was not approximately 30% higher."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getMaximumLaunchHeight() >= payload.getPredictedTrajectoryApexHeight() - 0.75
				),
				"A thrown Fly Creeper did not reach its raised predicted apex."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::hasDescendedUnderGravity),
				"A thrown Fly Creeper did not enter a gravity-driven descent."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::hasReachedLaunchTarget),
				"A thrown Fly Creeper did not reach its assigned target."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> Math.abs(payload.getTargetReachedAtLaunchTick() - payload.getPredictedImpactTicks()) <= 3
				),
				"A thrown Fly Creeper reached its target more than three ticks from the predicted hit time."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(payload -> payload.getClosestLaunchTargetDistance() <= 2.0),
				"A thrown Fly Creeper missed its assigned target by more than two blocks."
			);
			require(
				helper,
				initialPayloads.stream().noneMatch(FlyCreeper::isNoGravity),
				"A thrown Fly Creeper disabled gravity during its ballistic arc."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::isIgnited),
				"A thrown Fly Creeper did not arm its vanilla fuse on the predicted schedule."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::isRemoved),
				"A thrown Fly Creeper did not detonate after reaching its target."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> Math.abs(payload.getDetonatedAtLaunchTick() - payload.getPredictedImpactTicks()) <= 2
				),
				"A thrown Fly Creeper did not detonate within two ticks of its predicted hit time."
			);
		});
	}

	@GameTest(maxTicks = 75, skyAccess = true)
	public void flyCreeperFindsVillagerDivesAndExplodes(GameTestHelper helper) {
		FlyCreeper fly = helper.spawn(ModEntities.FLY_CREEPER, new Vec3(1.0, 3.0, 1.0), EntitySpawnReason.NATURAL);
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new Vec3(8.0, 3.0, 1.0), EntitySpawnReason.SPAWN_ITEM_USE);
		villager.setNoAi(true);
		villager.setInvulnerable(true);

		helper.succeedWhen(() -> {
			require(helper, fly.hasEverBecomeAirborne(), "Fly Creeper never entered flight.");
			require(
				helper,
				fly.getTakeoffFireworkEffectsTriggered() == 1 && fly.getTakeoffFireworkBurstsEmitted() > 0,
				"Fly Creeper did not emit exactly one vanilla firework takeoff effect."
			);
			require(helper, fly.hasEverDived(), "Fly Creeper never entered its dive.");
			require(helper, fly.isIgnited(), "Fly Creeper never armed the vanilla Creeper fuse.");
			require(helper, fly.isRemoved(), "Fly Creeper did not complete its bomb explosion.");
		});
	}

	@GameTest(maxTicks = 40, skyAccess = true, padding = 128)
	public void villagePoiFallbackTargetsVillageWithoutVillagers(GameTestHelper helper) {
		BlockPos villagePoi = new BlockPos(8, 2, 2);
		helper.setBlock(villagePoi, Blocks.BELL);

		FlyCreeper fly = helper.spawn(ModEntities.FLY_CREEPER, new Vec3(1.0, 3.0, 1.0), EntitySpawnReason.NATURAL);
		CmdCreeper cmd = helper.spawn(ModEntities.CMD_CREEPER, new Vec3(1.0, 3.0, 4.0), EntitySpawnReason.NATURAL);
		fly.setInvulnerable(true);
		cmd.setInvulnerable(true);

		require(
			helper,
			helper.getLevel().getEntitiesOfClass(Villager.class, fly.getBoundingBox().inflate(FlyCreeper.TARGET_RANGE), Villager::isAlive).isEmpty(),
			"The isolated village-POI test unexpectedly contains a Villager."
		);

		Vec3 expectedDestination = Vec3.atCenterOf(helper.absolutePos(villagePoi));
		helper.succeedWhen(() -> {
			Vec3 flyDestination = fly.getAttackDestination();
			require(helper, flyDestination != null, "Fly Creeper did not acquire the Bell village POI.");
			require(
				helper,
				flyDestination.distanceToSqr(expectedDestination) < 1.0E-6,
				"Fly Creeper's village fallback destination is not the Bell POI."
			);
			require(helper, cmd.hasVillageTarget(), "CMD Creeper did not acquire the Bell village POI.");
		});
	}

	private static Optional<Weighted<MobSpawnSettings.SpawnerData>> findSpawn(
		List<Weighted<MobSpawnSettings.SpawnerData>> entries,
		net.minecraft.world.entity.EntityType<?> type
	) {
		return entries.stream().filter(entry -> entry.value().type() == type).findFirst();
	}

	private static void assertSpawnMatches(
		GameTestHelper helper,
		List<Weighted<MobSpawnSettings.SpawnerData>> entries,
		Weighted<MobSpawnSettings.SpawnerData> vanilla,
		net.minecraft.world.entity.EntityType<?> customType,
		String name
	) {
		Weighted<MobSpawnSettings.SpawnerData> custom = findSpawn(entries, customType)
			.orElseThrow(() -> helper.assertionException(Component.literal(name + " is absent from a vanilla Creeper biome.")));
		require(helper, custom.weight() == vanilla.weight(), name + " spawn weight differs from vanilla Creeper.");
		require(helper, custom.value().minCount() == vanilla.value().minCount(), name + " minimum group differs from vanilla Creeper.");
		require(helper, custom.value().maxCount() == vanilla.value().maxCount(), name + " maximum group differs from vanilla Creeper.");
	}

	private static void require(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			throw helper.assertionException(Component.literal(message));
		}
	}
}
