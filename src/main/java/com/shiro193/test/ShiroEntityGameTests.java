package com.shiro193.test;

import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import com.shiro193.entity.SummonCreeper;
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

	@GameTest(maxTicks = 150, skyAccess = true)
	public void cmdCreeperHoldsRangeAndThrowsBothInSlowerHigherGravityArcs(GameTestHelper helper) {
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
					payload -> payload.getHorizontalLaunchSpeedMultiplier() >= 0.86
						&& payload.getHorizontalLaunchSpeedMultiplier() <= 0.91
				),
				"A thrown Fly Creeper's horizontal launch speed was not approximately 10% slower."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getLaunchOriginHeightMultiplier() >= 1.64
						&& payload.getLaunchOriginHeightMultiplier() <= 1.66
				),
				"A thrown Fly Creeper's release height was not approximately 65% higher."
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
					payload -> payload.getTakeoffFireworkBurstsEmitted() > 0
						&& FlyCreeper.TAKEOFF_FIREWORK_DURATION_TICKS >= 60
				),
				"A thrown Fly Creeper did not start a firework trail configured to last at least three seconds."
			);
			require(
				helper,
				initialPayloads.stream().allMatch(
					payload -> payload.getTakeoffFireworkColorBurstsEmitted() >= 7
						&& payload.getTakeoffFireworkDistinctColorCount() == 7
				),
				"A thrown Fly Creeper did not emit all seven vanilla firework color phases."
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

	@GameTest(maxTicks = 55, skyAccess = true)
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
			require(helper, creepers.stream().allMatch(Entity::isRemoved), "Not every Creeper completed its explosion.");
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
}
