package com.shiro193.test;

import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import com.shiro193.entity.SummonCreeper;
import com.shiro193.item.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShiroEntityGameTests {
	@GameTest(maxTicks = 1)
	public void registrationsAndVanillaSpawnParity(GameTestHelper helper) {
		require(helper, ModEntities.FLY_CREEPER.getCategory() == MobCategory.MONSTER, "Fly Creeper is not a monster.");
		require(helper, ModEntities.CMD_CREEPER.getCategory() == MobCategory.MONSTER, "CMD Creeper is not a monster.");
		require(helper, ModEntities.SUMMON_CREEPER.getCategory() == MobCategory.MONSTER, "Summon Creeper is not a monster.");
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
			SpawnPlacements.getPlacementType(ModEntities.SUMMON_CREEPER)
				== SpawnPlacements.getPlacementType(EntityTypes.CREEPER),
			"Summon Creeper placement differs from vanilla Creeper."
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
			SpawnPlacements.getHeightmapType(ModEntities.SUMMON_CREEPER)
				== SpawnPlacements.getHeightmapType(EntityTypes.CREEPER),
			"Summon Creeper heightmap differs from vanilla Creeper."
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
		require(
			helper,
			SpawnEggItem.spawnsEntity(new ItemStack(ModItems.SUMMON_CREEPER_SPAWN_EGG), ModEntities.SUMMON_CREEPER),
			"Summon Creeper spawn egg points to the wrong entity."
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

	@GameTest(maxTicks = 220, skyAccess = true, padding = 128)
	public void cmdCreeperCapturesAndLaunchesBothPayloadsOnFixedHighArcs(GameTestHelper helper) {
		CmdCreeper cmd = helper.spawn(ModEntities.CMD_CREEPER, new Vec3(1.0, 2.0, 1.0), EntitySpawnReason.NATURAL);
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new Vec3(8.0, 2.0, 1.0), EntitySpawnReason.SPAWN_ITEM_USE);
		villager.setNoAi(true);
		villager.setInvulnerable(true);
		villager.setNoGravity(true);
		cmd.setNoGravity(true);

		require(helper, cmd.getPayloadCount() == CmdCreeper.MAX_PAYLOAD, "CMD Creeper did not spawn with exactly two payloads.");
		require(
			helper,
			cmd.getItemBySlot(EquipmentSlot.HEAD).is(Items.CHAINMAIL_HELMET),
			"CMD Creeper is not equipped with its chainmail helmet."
		);
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
				cmd.getApproachRequests() == 0 && cmd.getInRangeHoldTicks() >= 10,
				"CMD Creeper approached even though its target started within throw range (approaches="
					+ cmd.getApproachRequests() + ", holdTicks=" + cmd.getInRangeHoldTicks() + ")."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::wasLaunched),
				"A carried Fly Creeper was not launched."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getPlannedApexHeight() >= FlyCreeper.MINIMUM_LAUNCH_APEX_HEIGHT
						&& payload.getMaximumLaunchHeight() >= FlyCreeper.MINIMUM_LAUNCH_APEX_HEIGHT - 0.75
				),
				"A CMD-launched Fly Creeper did not follow the global 35-block-minimum high arc: "
					+ initialPayloads.stream()
						.map(
							payload -> "planned=" + payload.getPlannedApexHeight()
								+ ", actual=" + payload.getMaximumLaunchHeight()
								+ ", phase=" + payload.getFlightPhase()
								+ ", ticks=" + payload.getBallisticLaunchTicks()
								+ ", removed=" + payload.isRemoved()
						)
						.toList()
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
				initialPayloads.stream().allMatch(payload -> payload.getTakeoffFireworkLaunchSoundsPlayed() == 1),
				"A thrown Fly Creeper did not play exactly one launch sound."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffFireworkBurstsEmitted() > 0
						&& payload.getTakeoffColorTrailTicksEmitted()
							>= Math.max(1, payload.getBallisticLaunchTicks() - 1)
				),
				"A thrown Fly Creeper's trail did not remain active for its complete flight: "
					+ initialPayloads.stream()
						.map(
							payload -> "trail=" + payload.getTakeoffColorTrailTicksEmitted()
								+ ", flight=" + payload.getBallisticLaunchTicks()
								+ ", gap=" + payload.getMaximumTrailEmissionGap()
						)
						.toList()
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffColorTrailTicksEmitted() >= 7
						&& payload.getTakeoffFireworkDistinctColorCount() == 7
				),
				"A thrown Fly Creeper did not emit all seven vanilla firework color phases."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffColorTrailTicksEmitted() == payload.getTakeoffFireworkBurstsEmitted()
						&& payload.getTakeoffColorParticlesEmitted()
							== payload.getTakeoffColorTrailTicksEmitted()
								* FlyCreeper.TAKEOFF_COLOR_PARTICLES_PER_TICK
						&& payload.getMaximumTrailEmissionGap() <= 1
				),
				"A thrown Fly Creeper's colorful trail was not continuous on every active tick."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(FlyCreeper::hasReachedBallisticApex),
				"A thrown Fly Creeper did not reach the apex of its curved trajectory."
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

	@GameTest(maxTicks = 1, skyAccess = true, padding = 128)
	public void undergroundCmdCreeperRefusesImpossibleLaunchWithoutCrashing(GameTestHelper helper) {
		CmdCreeper cmd = helper.spawn(ModEntities.CMD_CREEPER, new Vec3(2.0, 2.0, 2.0), EntitySpawnReason.NATURAL);
		cmd.setNoAi(true);
		for (int y = 6; y <= 72; y++) {
			helper.setBlock(new BlockPos(2, y, 2), Blocks.STONE);
		}

		List<FlyCreeper> payloads = cmd.getPassengers()
			.stream()
			.map(FlyCreeper.class::cast)
			.toList();
		require(helper, payloads.size() == CmdCreeper.MAX_PAYLOAD, "Underground CMD test did not start with two payloads.");

		Vec3 elevatedTarget = cmd.position().add(17.8, 66.6, -16.8);
		require(
			helper,
			!cmd.throwOneAt(elevatedTarget),
			"CMD Creeper launched a payload through terrain instead of refusing the impossible arc."
		);
		require(
			helper,
			cmd.getPayloadCount() == CmdCreeper.MAX_PAYLOAD
				&& payloads.stream().allMatch(FlyCreeper::isPassenger)
				&& payloads.stream().noneMatch(FlyCreeper::wasLaunched)
				&& cmd.getTotalThrown() == 0,
			"An impossible CMD launch detached, launched, or lost a Fly Creeper payload."
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 320, skyAccess = true, padding = 128)
	public void everyFlyCreeperLaunchUsesFixedTerrainAwareHighArcAndContinuousTrail(GameTestHelper helper) {
		for (int y = 1; y <= 48; y++) {
			for (int z = 68; z <= 72; z++) {
				helper.setBlock(new BlockPos(25, y, z), Blocks.STONE);
			}
		}

		List<LaunchScenario> scenarios = new ArrayList<>();
		scenarios.add(launchScenario(helper, "zero-distance", new Vec3(4.0, 4.0, 4.0), new Vec3(0.0, 0.0, 0.0), false));
		scenarios.add(launchScenario(helper, "short-range", new Vec3(4.0, 4.0, 18.0), new Vec3(3.0, 0.0, 1.0), false));
		scenarios.add(launchScenario(helper, "diagonal", new Vec3(4.0, 4.0, 34.0), new Vec3(28.0, 0.0, 23.0), false));
		scenarios.add(launchScenario(helper, "long-range", new Vec3(4.0, 4.0, 52.0), new Vec3(90.0, 0.0, 0.0), false));
		scenarios.add(launchScenario(helper, "high-target", new Vec3(55.0, 4.0, 4.0), new Vec3(24.0, 42.0, 0.0), false));
		scenarios.add(launchScenario(helper, "low-target", new Vec3(55.0, 30.0, 24.0), new Vec3(30.0, -20.0, 0.0), false));
		scenarios.add(launchScenario(helper, "elevated-origin", new Vec3(55.0, 26.0, 48.0), new Vec3(-36.0, -16.0, 18.0), false));
		LaunchScenario terrainScenario = launchScenario(
			helper,
			"48-block-ridge",
			new Vec3(4.0, 4.0, 70.0),
			new Vec3(42.0, 0.0, 0.0),
			true
		);
		scenarios.add(terrainScenario);

		for (LaunchScenario scenario : scenarios) {
			FlyCreeper fly = scenario.fly();
			Vec3 originalDestination = fly.getLaunchDestination();
			Vec3 originalVelocity = fly.getInitialLaunchVelocity();
			fly.launchAt(originalDestination.add(100.0, -30.0, 100.0));
			fly.setDeltaMovement(originalVelocity.add(0.75, 1.0, -0.5));
			require(
				helper,
				originalDestination.equals(fly.getLaunchDestination())
					&& originalVelocity.equals(fly.getInitialLaunchVelocity()),
				scenario.name() + " launch plan changed after takeoff."
			);
		}

		helper.succeedWhen(() -> {
			for (LaunchScenario scenario : scenarios) {
				assertCompletedHighArc(helper, scenario);
			}
			require(
				helper,
				terrainScenario.fly().wasTrajectoryRaisedForTerrain(),
				"The obstructed launch was not automatically raised above its 48-block ridge."
			);
			require(
				helper,
				terrainScenario.fly().getPlannedTerrainClearance() >= FlyCreeper.TERRAIN_CLEARANCE,
				"The terrain-aware trajectory did not retain the required clearance."
			);
			require(
				helper,
				scenarios.stream()
					.filter(scenario -> scenario.name().equals("high-target"))
					.allMatch(
						scenario -> scenario.fly().getPlannedApexHeight()
							>= 42.0 + 8.0 - 0.01
					),
				"The high-target trajectory did not automatically raise its apex above the 35-block minimum."
			);
			scenarios.stream()
				.flatMap(scenario -> scenario.forcedChunks().stream())
				.distinct()
				.forEach(chunk -> helper.getLevel().setChunkForced(chunk.x(), chunk.z(), false));
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
				fly.getTakeoffFireworkEffectsTriggered() == 1
					&& fly.getTakeoffFireworkLaunchSoundsPlayed() == 1
					&& fly.getTakeoffFireworkBurstsEmitted() > 0,
				"Fly Creeper did not emit exactly one vanilla firework takeoff effect."
			);
			require(helper, fly.hasEverDived(), "Fly Creeper never entered its dive.");
			require(helper, fly.isIgnited(), "Fly Creeper never armed the vanilla Creeper fuse.");
			require(helper, fly.isRemoved(), "Fly Creeper did not complete its bomb explosion.");
		});
	}

	@GameTest(maxTicks = 630, skyAccess = true)
	public void summonCreeperTriggersLightningAndSummonsOnTenAndThirtySecondSchedules(GameTestHelper helper) {
		SummonCreeper summon = helper.spawn(
			ModEntities.SUMMON_CREEPER,
			new Vec3(4.0, 2.0, 4.0),
			EntitySpawnReason.SPAWN_ITEM_USE
		);
		summon.setNoAi(true);
		summon.setNoGravity(true);
		summon.setInvulnerable(true);
		summon.setPersistenceRequired();

		require(
			helper,
			summon.getItemBySlot(EquipmentSlot.HEAD).is(Items.GOLDEN_HELMET),
			"Summon Creeper is not equipped with its golden helmet."
		);
		require(
			helper,
			summon.getSpawnLightningEffectsTriggered() == 1,
			"Summon Creeper did not trigger exactly one visual lightning bolt when spawned."
		);

		helper.succeedWhen(() -> {
			require(
				helper,
				summon.getSummonTimerTicks() >= SummonCreeper.CMD_SUMMON_INTERVAL_TICKS,
				"Summon Creeper did not reach its 30-second schedule."
			);
			require(
				helper,
				summon.getTimedFlySummons() == 3,
				"Summon Creeper did not summon one Fly Creeper every 10 seconds (count="
					+ summon.getTimedFlySummons() + ")."
			);
			require(
				helper,
				summon.getTimedCmdSummons() == 1,
				"Summon Creeper did not summon one CMD Creeper at 30 seconds."
			);
			AABB searchArea = summon.getBoundingBox().inflate(24.0);
			require(
				helper,
				helper.getLevel()
					.getEntitiesOfClass(CmdCreeper.class, searchArea, Entity::isAlive)
					.stream()
					.anyMatch(cmd -> cmd.getPayloadCount() == CmdCreeper.MAX_PAYLOAD),
				"The timed CMD reinforcement was absent or did not carry two Fly Creepers."
			);
		});
	}

	@GameTest(maxTicks = 80, skyAccess = true)
	public void everyCreeperExplosionPreservesObsidianAndBedrock(GameTestHelper helper) {
		List<BlockPos> creeperPositions = List.of(
			new BlockPos(1, 2, 3),
			new BlockPos(4, 2, 3),
			new BlockPos(7, 2, 3),
			new BlockPos(10, 2, 3)
		);
		List<Creeper> creepers = List.of(
			helper.spawn(EntityTypes.CREEPER, Vec3.atLowerCornerOf(creeperPositions.get(0)), EntitySpawnReason.SPAWN_ITEM_USE),
			helper.spawn(ModEntities.FLY_CREEPER, Vec3.atLowerCornerOf(creeperPositions.get(1)), EntitySpawnReason.SPAWN_ITEM_USE),
			helper.spawn(ModEntities.CMD_CREEPER, Vec3.atLowerCornerOf(creeperPositions.get(2)), EntitySpawnReason.SPAWN_ITEM_USE),
			helper.spawn(ModEntities.SUMMON_CREEPER, Vec3.atLowerCornerOf(creeperPositions.get(3)), EntitySpawnReason.SPAWN_ITEM_USE)
		);
		List<BlockPos> obsidianPositions = creeperPositions.stream().map(pos -> pos.offset(0, 0, 1)).toList();
		List<BlockPos> bedrockPositions = creeperPositions.stream().map(pos -> pos.offset(0, 0, -1)).toList();
		List<BlockPos> controlPositions = creeperPositions.stream().map(pos -> pos.offset(1, 0, 0)).toList();

		for (int index = 0; index < creepers.size(); index++) {
			helper.setBlock(obsidianPositions.get(index), Blocks.OBSIDIAN);
			helper.setBlock(bedrockPositions.get(index), Blocks.BEDROCK);
			helper.setBlock(controlPositions.get(index), Blocks.DIRT);
			Creeper creeper = creepers.get(index);
			EntityBasedExplosionDamageCalculator calculator = new EntityBasedExplosionDamageCalculator(creeper);
			require(
				helper,
				!calculator.shouldBlockExplode(
					null,
					helper.getLevel(),
					helper.absolutePos(obsidianPositions.get(index)),
					Blocks.OBSIDIAN.defaultBlockState(),
					Float.MAX_VALUE
				),
				"A Creeper explosion calculator did not explicitly veto obsidian destruction."
			);
			require(
				helper,
				calculator.shouldBlockExplode(
					null,
					helper.getLevel(),
					helper.absolutePos(controlPositions.get(index)),
					Blocks.DIRT.defaultBlockState(),
					Float.MAX_VALUE
				),
				"Creeper explosion protection incorrectly vetoed an ordinary dirt block."
			);
			creeper.setNoAi(true);
			creeper.setNoGravity(true);
			if (creeper instanceof CmdCreeper cmd) {
				cmd.getPassengers().forEach(passenger -> {
					if (passenger instanceof FlyCreeper payload) {
						payload.setNoAi(true);
					}
				});
			}
			creeper.ignite();
		}

		helper.succeedWhen(() -> {
			require(
				helper,
				creepers.stream().allMatch(Entity::isRemoved),
				"Not every Creeper completed its explosion: "
					+ creepers.stream()
						.filter(creeper -> !creeper.isRemoved())
						.map(creeper -> creeper.getType().toString() + "(ignited=" + creeper.isIgnited() + ")")
						.toList()
			);
			for (BlockPos obsidianPos : obsidianPositions) {
				require(helper, helper.getBlockState(obsidianPos).is(Blocks.OBSIDIAN), "A Creeper explosion destroyed obsidian.");
			}
			for (BlockPos bedrockPos : bedrockPositions) {
				require(helper, helper.getBlockState(bedrockPos).is(Blocks.BEDROCK), "A Creeper explosion destroyed bedrock.");
			}
			require(
				helper,
				controlPositions.stream().anyMatch(pos -> !helper.getBlockState(pos).is(Blocks.DIRT)),
				"The explosion control blocks were all intact; block-damage protection was not meaningfully exercised."
			);
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

	private static LaunchScenario launchScenario(
		GameTestHelper helper,
		String name,
		Vec3 spawnPosition,
		Vec3 targetOffset,
		boolean terrainExpected
	) {
		FlyCreeper fly = helper.spawn(ModEntities.FLY_CREEPER, spawnPosition, EntitySpawnReason.SPAWN_ITEM_USE);
		fly.setInvulnerable(true);
		fly.setCustomName(Component.literal("Fly Creeper"));
		Vec3 destination = fly.position().add(targetOffset);
		Set<ChunkPos> forcedChunks = forceTrajectoryChunks(helper, fly.position(), destination);
		fly.launchAt(destination);
		return new LaunchScenario(
			name,
			fly,
			destination,
			fly.getInitialLaunchVelocity(),
			terrainExpected,
			List.copyOf(forcedChunks)
		);
	}

	private static Set<ChunkPos> forceTrajectoryChunks(GameTestHelper helper, Vec3 origin, Vec3 destination) {
		Set<ChunkPos> chunks = new HashSet<>();
		int samples = Math.max(1, (int)Math.ceil(origin.distanceTo(destination) / 8.0));
		for (int sample = 0; sample <= samples; sample++) {
			double progress = (double)sample / samples;
			BlockPos position = BlockPos.containing(origin.lerp(destination, progress));
			ChunkPos chunk = new ChunkPos(position.getX() >> 4, position.getZ() >> 4);
			if (chunks.add(chunk)) {
				helper.getLevel().setChunkForced(chunk.x(), chunk.z(), true);
			}
		}
		return chunks;
	}

	private static void assertCompletedHighArc(GameTestHelper helper, LaunchScenario scenario) {
		FlyCreeper fly = scenario.fly();
		require(helper, fly.wasLaunched(), scenario.name() + " was not launched.");
		require(
			helper,
			fly.hasReachedBallisticApex(),
			scenario.name() + " never completed its initial ascent (phase=" + fly.getFlightPhase()
				+ ", ticks=" + fly.getBallisticLaunchTicks()
				+ ", planned=" + fly.getPlannedApexHeight()
				+ ", actual=" + fly.getMaximumLaunchHeight()
				+ ", velocity=" + fly.getDeltaMovement()
				+ ", complete=" + fly.isLaunchFlightComplete()
				+ ", removed=" + fly.isRemoved() + ")."
		);
		require(
			helper,
			fly.getPlannedApexHeight() >= FlyCreeper.MINIMUM_LAUNCH_APEX_HEIGHT
				&& fly.getMaximumLaunchHeight() >= FlyCreeper.MINIMUM_LAUNCH_APEX_HEIGHT - 0.75,
			scenario.name() + " selected a low or ground-skimming trajectory (planned="
				+ fly.getPlannedApexHeight() + ", actual=" + fly.getMaximumLaunchHeight() + ")."
		);
		require(
			helper,
			fly.isLaunchFlightComplete() || fly.isRemoved(),
			scenario.name() + " did not land, reach its target, or otherwise complete its flight."
		);
		require(
			helper,
			scenario.destination().equals(fly.getLaunchDestination())
				&& scenario.initialVelocity().equals(fly.getInitialLaunchVelocity()),
			scenario.name() + " changed its fixed destination or initial trajectory during flight."
		);
		Vec3 fixedHorizontal = fly.getFixedHorizontalVelocity();
		require(
			helper,
			fixedHorizontal.lengthSqr() < 1.0E-12
				|| Math.abs(fixedHorizontal.normalize().dot(scenario.initialVelocity().multiply(1.0, 0.0, 1.0).normalize()) - 1.0)
					< 1.0E-9,
			scenario.name() + " did not retain its launch-time horizontal course."
		);
		require(
			helper,
			!scenario.terrainExpected()
				|| fly.wasTrajectoryRaisedForTerrain()
					&& fly.getPlannedTerrainClearance() >= FlyCreeper.TERRAIN_CLEARANCE,
			scenario.name() + " did not automatically raise and clear its obstructing terrain."
		);
		require(
			helper,
			fly.getTakeoffFireworkEffectsTriggered() == 1
				&& fly.getTakeoffFireworkLaunchSoundsPlayed() == 1,
			scenario.name() + " did not preserve the exactly-once launch sound behavior."
		);
		require(
			helper,
			fly.getTakeoffColorTrailTicksEmitted() >= Math.max(1, fly.getBallisticLaunchTicks() - 1)
				&& fly.getTakeoffColorTrailTicksEmitted() > fly.getPredictedImpactTicks() / 2
				&& fly.getMaximumTrailEmissionGap() <= 1,
			scenario.name() + " trail was interrupted or stopped during the latter half of flight (trailTicks="
				+ fly.getTakeoffColorTrailTicksEmitted() + ", flightTicks=" + fly.getBallisticLaunchTicks()
				+ ", maxGap=" + fly.getMaximumTrailEmissionGap() + ")."
		);
		require(
			helper,
			fly.getTakeoffFireworkBurstsEmitted() == fly.getTakeoffColorTrailTicksEmitted()
				&& fly.getTakeoffColorParticlesEmitted()
					== fly.getTakeoffColorTrailTicksEmitted() * FlyCreeper.TAKEOFF_COLOR_PARTICLES_PER_TICK
				&& fly.getTakeoffFireworkDistinctColorCount() == 7,
			scenario.name() + " did not preserve the continuous seven-color visual trail."
		);
	}

	private record LaunchScenario(
		String name,
		FlyCreeper fly,
		Vec3 destination,
		Vec3 initialVelocity,
		boolean terrainExpected,
		List<ChunkPos> forcedChunks
	) {
	}
}
