package com.shiro193.client.test;

import com.shiro193.ShiroSTestMod;
import com.shiro193.entity.CmdCreeper;
import com.shiro193.entity.FlyCreeper;
import com.shiro193.entity.ModEntities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class ShiroClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		context.getInput().resizeWindow(1280, 720);
		try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
			world.getServer().runCommand("time set noon");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("difficulty hard");

			BlockPos sceneCenter = world.getServer().computeOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				ServerLevel level = player.level();
				int baseX = player.getBlockX();
				int baseZ = player.getBlockZ() + 8;
				int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, baseX, baseZ);
				int platformY = surfaceY;
				BlockPos center = new BlockPos(baseX, platformY + 1, baseZ);

				for (int x = -8; x <= 8; x++) {
					for (int z = -7; z <= 9; z++) {
						level.setBlockAndUpdate(center.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
					}
				}

				FlyCreeper fly = ModEntities.FLY_CREEPER.spawn(
					level,
					center.offset(-3, 0, 3),
					EntitySpawnReason.SPAWN_ITEM_USE
				);
				if (fly == null) {
					throw new AssertionError("Visible test could not spawn Fly Creeper.");
				}
				fly.setNoAi(true);
				fly.setPersistenceRequired();
				fly.setCustomName(Component.literal("Fly Creeper"));
				fly.setCustomNameVisible(true);

				CmdCreeper cmd = ModEntities.CMD_CREEPER.spawn(
					level,
					center.offset(3, 0, 3),
					EntitySpawnReason.SPAWN_ITEM_USE
				);
				if (cmd == null || cmd.getPayloadCount() != 2) {
					throw new AssertionError("Visible test CMD Creeper did not stage with two payloads.");
				}
				cmd.setNoAi(true);
				cmd.setPersistenceRequired();
				cmd.setCustomName(Component.literal("CMD Creeper (2 payloads)"));
				cmd.setCustomNameVisible(true);
				cmd.getPassengers().forEach(passenger -> {
					if (passenger instanceof FlyCreeper payload) {
						payload.setNoAi(true);
						payload.setPersistenceRequired();
					}
				});

				Villager villager = EntityTypes.VILLAGER.spawn(
					level,
					center.offset(0, 0, 7),
					EntitySpawnReason.SPAWN_ITEM_USE
				);
				if (villager == null) {
					throw new AssertionError("Visible test could not spawn Villager.");
				}
				villager.setNoAi(true);
				villager.setInvulnerable(true);
				villager.setCustomName(Component.literal("Villager target"));
				villager.setCustomNameVisible(true);

				player.setGameMode(GameType.CREATIVE);
				player.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() - 6.5);
				return center;
			});

			world.getClientLevel().waitForChunksDownload();
			context.getInput().lookAt(sceneCenter.above());
			context.waitTicks(60);

			int[] serverCounts = world.getServer().computeOnServer(server -> {
				ServerLevel level = server.getPlayerList().getPlayers().getFirst().level();
				AABB scene = new AABB(sceneCenter).inflate(20.0);
				return new int[] {
					level.getEntitiesOfClass(FlyCreeper.class, scene, entity -> true).size(),
					level.getEntitiesOfClass(CmdCreeper.class, scene, entity -> true).size(),
					level.getEntitiesOfClass(Villager.class, scene, entity -> true).size()
				};
			});
			if (serverCounts[0] != 3 || serverCounts[1] != 1 || serverCounts[2] < 1) {
				throw new AssertionError(
					"Visible scene server mismatch: expected 3 Fly Creepers, 1 CMD Creeper, and a Villager; got "
						+ serverCounts[0] + ", " + serverCounts[1] + ", " + serverCounts[2] + "."
				);
			}

			context.runOnClient(minecraft -> {
				if (minecraft.level == null) {
					throw new AssertionError("Visible test client has no loaded level.");
				}

				AABB scene = new AABB(sceneCenter).inflate(20.0);
				int flyCount = minecraft.level.getEntitiesOfClass(FlyCreeper.class, scene, entity -> true).size();
				int cmdCount = minecraft.level.getEntitiesOfClass(CmdCreeper.class, scene, entity -> true).size();
				int villagerCount = minecraft.level.getEntitiesOfClass(Villager.class, scene, entity -> true).size();
				if (flyCount != 3 || cmdCount != 1 || villagerCount < 1) {
					throw new AssertionError(
						"Visible scene mismatch: expected 3 Fly Creepers, 1 CMD Creeper, and a Villager; got "
							+ flyCount + ", " + cmdCount + ", " + villagerCount
							+ " (server had " + serverCounts[0] + ", " + serverCounts[1] + ", " + serverCounts[2] + ")."
					);
				}
			});

			Path screenshot = context.takeScreenshot("shiros-test-mod-entity-scene");
			if (!Files.isRegularFile(screenshot)) {
				throw new AssertionError("Visible entity test did not produce a screenshot.");
			}
			ShiroSTestMod.LOGGER.info("Visible client entity test screenshot: {}", screenshot.toAbsolutePath());

			world.getServer().computeOnServer(server -> {
				ServerLevel level = server.getPlayerList().getPlayers().getFirst().level();
				AABB scene = new AABB(sceneCenter).inflate(20.0);
				CmdCreeper cmd = level.getEntitiesOfClass(CmdCreeper.class, scene, entity -> true)
					.stream()
					.findFirst()
					.orElseThrow(() -> new AssertionError("Visible ballistic test could not find its CMD Creeper."));
				cmd.getPassengers().stream().filter(FlyCreeper.class::isInstance).map(FlyCreeper.class::cast).forEach(payload -> {
					payload.setNoAi(false);
					payload.setCustomName(Component.literal("Ballistic Fly Creeper"));
					payload.setCustomNameVisible(true);
				});
				cmd.setNoAi(false);
				return null;
			});

			context.waitTicks(11);
			BlockPos fireworkPayload = world.getServer().computeOnServer(server -> {
				ServerLevel level = server.getPlayerList().getPlayers().getFirst().level();
				AABB scene = new AABB(sceneCenter).inflate(24.0);
				FlyCreeper payload = level.getEntitiesOfClass(
						FlyCreeper.class,
						scene,
						fly -> fly.wasLaunched() && fly.isTakeoffFireworkActive()
					)
					.stream()
					.findFirst()
					.orElseThrow(() -> new AssertionError("No launched Fly Creeper had an active takeoff firework effect."));
				if (payload.getTakeoffFireworkEffectsTriggered() != 1 || payload.getTakeoffFireworkBurstsEmitted() <= 0) {
					throw new AssertionError(
						"Visible payload did not emit exactly one takeoff firework effect (triggers="
							+ payload.getTakeoffFireworkEffectsTriggered()
							+ ", bursts=" + payload.getTakeoffFireworkBurstsEmitted() + ")."
					);
				}
				if (payload.getTakeoffFireworkParticlesEmitted()
					< (
						FlyCreeper.TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK
							+ FlyCreeper.TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK
					)) {
					throw new AssertionError("Visible payload did not emit the denser firework trace.");
				}
				if (payload.getPredictedImpactTicks() <= 0
					|| payload.getScheduledFuseIgnitionTick()
						!= Math.max(1, payload.getPredictedImpactTicks() - 30 + 1)) {
					throw new AssertionError(
						"Visible payload did not receive a valid CMD-predicted fuse schedule (impact="
							+ payload.getPredictedImpactTicks()
							+ ", ignition=" + payload.getScheduledFuseIgnitionTick() + ")."
					);
				}
				return payload.blockPosition();
			});
			context.getInput().lookAt(fireworkPayload);
			context.waitTicks(2);
			Path fireworkScreenshot = context.takeScreenshot("shiros-test-mod-firework-takeoff");
			if (!Files.isRegularFile(fireworkScreenshot)) {
				throw new AssertionError("Visible firework-takeoff test did not produce a screenshot.");
			}
			ShiroSTestMod.LOGGER.info("Visible firework takeoff screenshot: {}", fireworkScreenshot.toAbsolutePath());

			context.waitTicks(15);
			BlockPos airbornePayload = world.getServer().computeOnServer(server -> {
				ServerLevel level = server.getPlayerList().getPlayers().getFirst().level();
				AABB scene = new AABB(sceneCenter).inflate(24.0);
				FlyCreeper payload = level.getEntitiesOfClass(
						FlyCreeper.class,
						scene,
						fly -> fly.wasLaunched() && fly.hasReachedBallisticApex() && !fly.hasReachedLaunchTarget()
					)
					.stream()
					.findFirst()
					.orElseThrow(() -> new AssertionError("No Fly Creeper was visible on the descending ballistic arc."));
				if (payload.getPredictedTrajectoryHeightMultiplier() < 1.28
					|| payload.getPredictedTrajectoryHeightMultiplier() > 1.32
					|| payload.getMaximumLaunchHeight() < payload.getPredictedTrajectoryApexHeight() - 0.75
					|| !payload.hasDescendedUnderGravity()
					|| payload.isNoGravity()) {
					throw new AssertionError(
						"Visible payload did not complete the 30%-higher gravity arc (height="
							+ payload.getMaximumLaunchHeight()
							+ ", predicted=" + payload.getPredictedTrajectoryApexHeight()
							+ ", multiplier=" + payload.getPredictedTrajectoryHeightMultiplier()
							+ ", phase=" + payload.getFlightPhase() + ")."
					);
				}
				return payload.blockPosition();
			});
			context.getInput().lookAt(airbornePayload);
			context.waitTicks(2);
			Path arcScreenshot = context.takeScreenshot("shiros-test-mod-ballistic-arc");
			if (!Files.isRegularFile(arcScreenshot)) {
				throw new AssertionError("Visible ballistic-arc test did not produce a screenshot.");
			}
			ShiroSTestMod.LOGGER.info("Visible ballistic arc screenshot: {}", arcScreenshot.toAbsolutePath());

			context.getInput().lookAt(sceneCenter.offset(0, 1, 7));
			context.waitTicks(2);
			Optional<BlockPos> impactPayloadCandidate = Optional.empty();
			for (int waitTick = 0; waitTick < 24 && impactPayloadCandidate.isEmpty(); waitTick++) {
				context.waitTicks(1);
				impactPayloadCandidate = world.getServer().computeOnServer(server -> {
					ServerLevel level = server.getPlayerList().getPlayers().getFirst().level();
					AABB scene = new AABB(sceneCenter).inflate(24.0);
					return level.getEntitiesOfClass(
							FlyCreeper.class,
							scene,
							fly -> fly.wasLaunched() && fly.hasReachedLaunchTarget()
						)
						.stream()
						.filter(payload -> payload.getClosestLaunchTargetDistance() <= 2.0 && payload.isIgnited())
						.filter(
							payload -> Math.abs(payload.getTargetReachedAtLaunchTick() - payload.getPredictedImpactTicks()) <= 3
						)
						.map(FlyCreeper::blockPosition)
						.findFirst();
				});
			}
			BlockPos impactPayload = impactPayloadCandidate.orElseThrow(
				() -> new AssertionError("No fuse-scheduled ballistic Fly Creeper reached the visible Villager target.")
			);
			context.getInput().lookAt(impactPayload);
			context.waitTicks(4);
			Path impactScreenshot = context.takeScreenshot("shiros-test-mod-ballistic-impact");
			if (!Files.isRegularFile(impactScreenshot)) {
				throw new AssertionError("Visible ballistic-impact test did not produce a screenshot.");
			}
			ShiroSTestMod.LOGGER.info("Visible ballistic impact screenshot: {}", impactScreenshot.toAbsolutePath());
		}
	}
}
