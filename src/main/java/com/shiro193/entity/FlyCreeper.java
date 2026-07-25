package com.shiro193.entity;

import java.util.EnumSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FlyCreeper extends Creeper {
	public static final int TARGET_RANGE = 128;
	private static final double DIVE_START_DISTANCE = 15.0;
	private static final double CRUISE_SPEED = 0.78;
	private static final double DIVE_SPEED = 1.18;
	private static final double BALLISTIC_IMPACT_RADIUS = 2.0;
	public static final double MINIMUM_LAUNCH_APEX_HEIGHT = 35.0;
	public static final double TERRAIN_CLEARANCE = 4.0;
	public static final int TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK = 4;
	public static final int TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK = 3;
	public static final int TAKEOFF_COLOR_PARTICLES_PER_TICK = 8;
	public static final int TAKEOFF_COLOR_PHASE_DURATION_TICKS = 5;
	private static final int[] RAINBOW_FIREWORK_COLORS = {
		0xFF3B30,
		0xFF9500,
		0xFFCC00,
		0x34C759,
		0x32ADE6,
		0x007AFF,
		0xAF52DE
	};
	private static final int VANILLA_CREEPER_FUSE_TICKS = 30;
	private static final double BALLISTIC_GRAVITY = 0.08;
	private static final double BALLISTIC_VERTICAL_DRAG = 0.98;
	private static final int MAX_BALLISTIC_FLIGHT_TICKS = 600;
	private static final double TERRAIN_PLANNING_INCREMENT = 5.0;
	private static final double TARGET_HEIGHT_CLEARANCE = 8.0;

	private VillageTargeting.@Nullable Target villageTarget;
	private @Nullable Vec3 forcedDestination;
	private FlightPhase flightPhase = FlightPhase.SEARCHING;
	private boolean launched;
	private boolean everAirborne;
	private boolean everDived;
	private boolean reachedBallisticApex;
	private boolean descendedUnderGravity;
	private boolean reachedLaunchTarget;
	private boolean launchFlightComplete;
	private boolean terrainRaisedTrajectory;
	private int ballisticLaunchTicks;
	private double launchStartY;
	private double maximumLaunchY;
	private double plannedApexHeight;
	private double plannedTerrainClearance;
	private double closestLaunchTargetDistanceSqr = Double.POSITIVE_INFINITY;
	private double initialLaunchSpeed;
	private double ballisticHorizontalSpeed;
	private Vec3 fixedHorizontalVelocity = Vec3.ZERO;
	private Vec3 initialLaunchVelocity = Vec3.ZERO;
	private double fixedVerticalVelocity;
	private int predictedImpactTicks;
	private int scheduledFuseIgnitionTick;
	private int fuseIgnitedAtLaunchTick = -1;
	private int targetReachedAtLaunchTick = -1;
	private int detonatedAtLaunchTick = -1;
	private int takeoffFireworkEffectsTriggered;
	private int takeoffFireworkLaunchSoundsPlayed;
	private int takeoffFireworkBurstsEmitted;
	private int takeoffFireworkParticlesEmitted;
	private int takeoffColorTrailTicksEmitted;
	private int takeoffColorParticlesEmitted;
	private int takeoffFireworkColorMask;
	private long lastTrailEmissionGameTick = Long.MIN_VALUE;
	private int maximumTrailEmissionGap;

	public FlyCreeper(EntityType<? extends FlyCreeper> type, Level level) {
		super(type, level);
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes().add(Attributes.FOLLOW_RANGE, TARGET_RANGE);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new BombVillageGoal());
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Villager.class, 16.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	public void launchAt(Vec3 destination) {
		if (this.launched && !this.launchFlightComplete) {
			return;
		}
		if (this.isPassenger()) {
			this.stopRiding();
		}

		LaunchPlan launchPlan = this.createLaunchPlan(destination);
		this.forcedDestination = destination;
		this.launched = true;
		this.flightPhase = FlightPhase.BALLISTIC_ASCENT;
		this.everAirborne = true;
		this.everDived = false;
		this.reachedBallisticApex = false;
		this.descendedUnderGravity = false;
		this.reachedLaunchTarget = false;
		this.launchFlightComplete = false;
		this.terrainRaisedTrajectory = launchPlan.terrainRaised();
		this.ballisticLaunchTicks = 0;
		this.launchStartY = this.getY();
		this.maximumLaunchY = this.getY();
		this.plannedApexHeight = launchPlan.apexHeight();
		this.plannedTerrainClearance = launchPlan.minimumTerrainClearance();
		this.closestLaunchTargetDistanceSqr = this.position().distanceToSqr(destination);
		this.initialLaunchVelocity = launchPlan.initialVelocity();
		this.fixedVerticalVelocity = this.initialLaunchVelocity.y;
		this.fixedHorizontalVelocity = new Vec3(
			this.initialLaunchVelocity.x,
			0.0,
			this.initialLaunchVelocity.z
		);
		this.initialLaunchSpeed = this.initialLaunchVelocity.length();
		this.ballisticHorizontalSpeed = this.fixedHorizontalVelocity.length();
		this.predictedImpactTicks = launchPlan.flightTicks();
		this.scheduledFuseIgnitionTick = Math.max(
			1,
			this.predictedImpactTicks - VANILLA_CREEPER_FUSE_TICKS + 1
		);
		this.fuseIgnitedAtLaunchTick = -1;
		this.targetReachedAtLaunchTick = -1;
		this.detonatedAtLaunchTick = -1;
		this.getNavigation().stop();
		this.setNoGravity(false);
		this.setDeltaMovement(this.initialLaunchVelocity);
		this.triggerTakeoffFirework();
	}

	public FlightPhase getFlightPhase() {
		return this.flightPhase;
	}

	public boolean hasEverBecomeAirborne() {
		return this.everAirborne;
	}

	public boolean hasEverDived() {
		return this.everDived;
	}

	public boolean wasLaunched() {
		return this.launched;
	}

	public boolean hasReachedBallisticApex() {
		return this.reachedBallisticApex;
	}

	public boolean hasDescendedUnderGravity() {
		return this.descendedUnderGravity;
	}

	public boolean hasReachedLaunchTarget() {
		return this.reachedLaunchTarget;
	}

	public double getMaximumLaunchHeight() {
		return this.maximumLaunchY - this.launchStartY;
	}

	public double getClosestLaunchTargetDistance() {
		return Math.sqrt(this.closestLaunchTargetDistanceSqr);
	}

	public double getInitialLaunchSpeed() {
		return this.initialLaunchSpeed;
	}

	public int getBallisticLaunchTicks() {
		return this.ballisticLaunchTicks;
	}

	public double getHorizontalLaunchSpeed() {
		return this.ballisticHorizontalSpeed;
	}

	public Vec3 getInitialLaunchVelocity() {
		return this.initialLaunchVelocity;
	}

	public @Nullable Vec3 getLaunchDestination() {
		return this.forcedDestination;
	}

	public Vec3 getFixedHorizontalVelocity() {
		return this.fixedHorizontalVelocity;
	}

	public boolean isLaunchFlightComplete() {
		return this.launchFlightComplete;
	}

	public int getPredictedImpactTicks() {
		return this.predictedImpactTicks;
	}

	public double getPlannedApexHeight() {
		return this.plannedApexHeight;
	}

	public double getPredictedTrajectoryApexHeight() {
		return this.plannedApexHeight;
	}

	public double getPlannedTerrainClearance() {
		return this.plannedTerrainClearance;
	}

	public boolean wasTrajectoryRaisedForTerrain() {
		return this.terrainRaisedTrajectory;
	}

	public int getScheduledFuseIgnitionTick() {
		return this.scheduledFuseIgnitionTick;
	}

	public int getFuseIgnitedAtLaunchTick() {
		return this.fuseIgnitedAtLaunchTick;
	}

	public int getTargetReachedAtLaunchTick() {
		return this.targetReachedAtLaunchTick;
	}

	public int getDetonatedAtLaunchTick() {
		return this.detonatedAtLaunchTick;
	}

	public boolean hasTriggeredTakeoffFirework() {
		return this.takeoffFireworkEffectsTriggered > 0;
	}

	public boolean isTakeoffFireworkActive() {
		return this.shouldEmitFlightTrail();
	}

	public int getTakeoffFireworkEffectsTriggered() {
		return this.takeoffFireworkEffectsTriggered;
	}

	public int getTakeoffFireworkLaunchSoundsPlayed() {
		return this.takeoffFireworkLaunchSoundsPlayed;
	}

	public int getTakeoffFireworkBurstsEmitted() {
		return this.takeoffFireworkBurstsEmitted;
	}

	public int getTakeoffFireworkParticlesEmitted() {
		return this.takeoffFireworkParticlesEmitted;
	}

	public int getTakeoffColorTrailTicksEmitted() {
		return this.takeoffColorTrailTicksEmitted;
	}

	public int getTakeoffColorParticlesEmitted() {
		return this.takeoffColorParticlesEmitted;
	}

	public int getTakeoffFireworkDistinctColorCount() {
		return Integer.bitCount(this.takeoffFireworkColorMask);
	}

	public int getMaximumTrailEmissionGap() {
		return this.maximumTrailEmissionGap;
	}

	public @Nullable Vec3 getAttackDestination() {
		if (this.forcedDestination != null) {
			return this.forcedDestination;
		}

		return this.villageTarget == null ? null : this.villageTarget.currentPosition();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return this.flightPhase == FlightPhase.SEARCHING && super.causeFallDamage(fallDistance, damageModifier, damageSource);
	}

	@Override
	public void tick() {
		if (this.launched && !this.launchFlightComplete) {
			this.setDeltaMovement(
				this.fixedHorizontalVelocity.x,
				this.fixedVerticalVelocity,
				this.fixedHorizontalVelocity.z
			);
		}
		boolean trackingBallisticDetonation = this.launched && !this.isRemoved();
		super.tick();
		if (trackingBallisticDetonation && this.isRemoved() && this.detonatedAtLaunchTick < 0) {
			this.detonatedAtLaunchTick = this.ballisticLaunchTicks;
		}
		if (this.launched && !this.launchFlightComplete && !this.isRemoved()) {
			this.tickBallisticFlight();
		}
		this.tickTakeoffFirework();
	}

	private void triggerTakeoffFirework() {
		if (this.level().isClientSide()) {
			return;
		}
		if (this.launched && this.takeoffFireworkEffectsTriggered > 0) {
			return;
		}

		this.takeoffFireworkEffectsTriggered++;
		this.takeoffFireworkLaunchSoundsPlayed++;
		this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.5F, 1.0F);
	}

	private void tickTakeoffFirework() {
		if (!this.shouldEmitFlightTrail() || !(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Vec3 movement = this.getDeltaMovement();
		Vec3 trailOffset = movement.lengthSqr() > 1.0E-6
			? movement.normalize().scale(-0.65)
			: new Vec3(0.0, -0.65, 0.0);
		Vec3 emitter = this.position().add(0.0, this.getBbHeight() * 0.35, 0.0).add(trailOffset);
		serverLevel.sendParticles(
			ParticleTypes.FIREWORK,
			emitter.x,
			emitter.y,
			emitter.z,
			TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK,
			0.08,
			0.08,
			0.08,
			0.035
		);
		Vec3 rearEmitter = emitter.add(trailOffset.scale(0.8));
		serverLevel.sendParticles(
			ParticleTypes.FIREWORK,
			rearEmitter.x,
			rearEmitter.y,
			rearEmitter.z,
			TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK,
			0.06,
			0.06,
			0.06,
			0.025
		);
		this.takeoffFireworkBurstsEmitted++;
		this.takeoffFireworkParticlesEmitted += TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK
			+ TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK;
		int elapsedTicks = this.takeoffColorTrailTicksEmitted;
		int colorIndex = (elapsedTicks / TAKEOFF_COLOR_PHASE_DURATION_TICKS) % RAINBOW_FIREWORK_COLORS.length;
		DustParticleOptions colorParticle = new DustParticleOptions(RAINBOW_FIREWORK_COLORS[colorIndex], 1.2F);
		serverLevel.sendParticles(
			colorParticle,
			emitter.x,
			emitter.y,
			emitter.z,
			TAKEOFF_COLOR_PARTICLES_PER_TICK / 2,
			0.05,
			0.05,
			0.05,
			0.015
		);
		serverLevel.sendParticles(
			colorParticle,
			rearEmitter.x,
			rearEmitter.y,
			rearEmitter.z,
			TAKEOFF_COLOR_PARTICLES_PER_TICK / 2,
			0.04,
			0.04,
			0.04,
			0.01
		);
		this.takeoffColorTrailTicksEmitted++;
		this.takeoffColorParticlesEmitted += TAKEOFF_COLOR_PARTICLES_PER_TICK;
		this.takeoffFireworkColorMask |= 1 << colorIndex;
		long gameTime = serverLevel.getGameTime();
		if (this.lastTrailEmissionGameTick != Long.MIN_VALUE) {
			this.maximumTrailEmissionGap = Math.max(
				this.maximumTrailEmissionGap,
				(int)(gameTime - this.lastTrailEmissionGameTick)
			);
		}
		this.lastTrailEmissionGameTick = gameTime;
	}

	private boolean shouldEmitFlightTrail() {
		if (this.takeoffFireworkEffectsTriggered == 0 || this.isRemoved() || this.isPassenger()) {
			return false;
		}
		if (this.launched) {
			return !this.launchFlightComplete;
		}
		return this.flightPhase != FlightPhase.SEARCHING || !this.onGround();
	}

	private LaunchPlan createLaunchPlan(Vec3 destination) {
		Vec3 origin = this.position();
		double targetHeightOffset = destination.y - origin.y;
		double minimumRequiredApex = Math.max(
			MINIMUM_LAUNCH_APEX_HEIGHT,
			targetHeightOffset + TARGET_HEIGHT_CLEARANCE
		);
		double candidateApex = minimumRequiredApex;
		double minimumTerrainClearance = Double.POSITIVE_INFINITY;
		boolean terrainRaised = false;

		for (int attempt = 0; attempt < 64; attempt++) {
			double verticalSpeed = solveInitialVerticalSpeedForApex(candidateApex);
			int flightTicks = estimateBallisticFlightTicks(verticalSpeed, targetHeightOffset);
			minimumTerrainClearance = this.estimateMinimumTerrainClearance(
				origin,
				destination,
				verticalSpeed,
				flightTicks
			);
			if (minimumTerrainClearance >= TERRAIN_CLEARANCE) {
				Vec3 horizontalOffset = new Vec3(destination.x - origin.x, 0.0, destination.z - origin.z);
				Vec3 horizontalVelocity = horizontalOffset.lengthSqr() < 1.0E-12
					? Vec3.ZERO
					: horizontalOffset.scale(1.0 / flightTicks);
				return new LaunchPlan(
					horizontalVelocity.add(0.0, verticalSpeed, 0.0),
					flightTicks,
					Math.max(candidateApex, estimateBallisticApexHeight(verticalSpeed)),
					minimumTerrainClearance,
					terrainRaised
				);
			}

			terrainRaised = true;
			candidateApex += Math.max(
				TERRAIN_PLANNING_INCREMENT,
				TERRAIN_CLEARANCE - minimumTerrainClearance + 1.0
			);
		}

		throw new IllegalStateException(
			"Unable to plan a terrain-clearing Fly Creeper arc from " + origin + " to " + destination
		);
	}

	private double estimateMinimumTerrainClearance(
		Vec3 origin,
		Vec3 destination,
		double initialVerticalSpeed,
		int flightTicks
	) {
		double horizontalDistanceSqr = Mth.square(destination.x - origin.x)
			+ Mth.square(destination.z - origin.z);
		if (horizontalDistanceSqr < 1.0E-12) {
			return Double.POSITIVE_INFINITY;
		}

		double minimumClearance = Double.POSITIVE_INFINITY;
		double relativeY = 0.0;
		double verticalSpeed = initialVerticalSpeed;
		for (int tick = 1; tick < flightTicks; tick++) {
			relativeY += verticalSpeed;
			verticalSpeed = (verticalSpeed - BALLISTIC_GRAVITY) * BALLISTIC_VERTICAL_DRAG;
			double progress = (double)tick / flightTicks;
			if (progress < 0.05 || progress > 0.90) {
				continue;
			}

			double x = Mth.lerp(progress, origin.x, destination.x);
			double z = Mth.lerp(progress, origin.z, destination.z);
			int terrainY = this.level().getHeight(
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Mth.floor(x),
				Mth.floor(z)
			);
			minimumClearance = Math.min(minimumClearance, origin.y + relativeY - terrainY);
		}
		return minimumClearance;
	}

	private static double solveInitialVerticalSpeedForApex(double requiredApexHeight) {
		double low = 0.0;
		double high = 8.0;
		for (int iteration = 0; iteration < 64; iteration++) {
			double candidate = (low + high) * 0.5;
			if (estimateBallisticApexHeight(candidate) < requiredApexHeight) {
				low = candidate;
			} else {
				high = candidate;
			}
		}
		return (low + high) * 0.5;
	}

	private static double estimateBallisticApexHeight(double initialVerticalSpeed) {
		double relativeY = 0.0;
		double maximumY = 0.0;
		double verticalSpeed = initialVerticalSpeed;
		for (int tick = 1; tick <= MAX_BALLISTIC_FLIGHT_TICKS; tick++) {
			relativeY += verticalSpeed;
			maximumY = Math.max(maximumY, relativeY);
			verticalSpeed = (verticalSpeed - BALLISTIC_GRAVITY) * BALLISTIC_VERTICAL_DRAG;
			if (verticalSpeed <= 0.0) {
				return maximumY;
			}
		}
		return maximumY;
	}

	private static int estimateBallisticFlightTicks(double initialVerticalSpeed, double targetHeightOffset) {
		double relativeY = 0.0;
		double verticalSpeed = initialVerticalSpeed;
		boolean passedApex = false;
		for (int tick = 1; tick <= MAX_BALLISTIC_FLIGHT_TICKS; tick++) {
			relativeY += verticalSpeed;
			verticalSpeed = (verticalSpeed - BALLISTIC_GRAVITY) * BALLISTIC_VERTICAL_DRAG;
			if (verticalSpeed <= 0.0) {
				passedApex = true;
			}
			if (passedApex && relativeY <= targetHeightOffset) {
				return tick;
			}
		}
		throw new IllegalStateException("Fly Creeper ballistic arc did not converge within the planning limit.");
	}

	private record LaunchPlan(
		Vec3 initialVelocity,
		int flightTicks,
		double apexHeight,
		double minimumTerrainClearance,
		boolean terrainRaised
	) {
	}

	private boolean acquireTarget() {
		if (this.forcedDestination != null) {
			return true;
		}

		if (this.villageTarget != null) {
			Villager villager = this.villageTarget.villager();
			if (villager == null || villager.isAlive()) {
				return true;
			}
		}

		this.villageTarget = VillageTargeting.findNearest(this, TARGET_RANGE).orElse(null);
		if (this.villageTarget != null && this.villageTarget.villager() != null) {
			this.setTarget(this.villageTarget.villager());
		}

		return this.villageTarget != null;
	}

	private void flyToward(Vec3 destination, boolean diving) {
		Vec3 offset = destination.subtract(this.position());
		if (offset.lengthSqr() < 1.0E-6) {
			return;
		}

		double speed = diving ? DIVE_SPEED : CRUISE_SPEED;
		Vec3 desiredVelocity = offset.normalize().scale(speed);
		Vec3 velocity = this.getDeltaMovement().scale(0.55).add(desiredVelocity.scale(0.45));
		this.setDeltaMovement(velocity);
		this.faceVelocity(velocity);
	}

	private void faceVelocity(Vec3 velocity) {
		double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
		float yaw = (float)(Mth.atan2(velocity.z, velocity.x) * Mth.RAD_TO_DEG) - 90.0F;
		float pitch = (float)(-(Mth.atan2(velocity.y, horizontal) * Mth.RAD_TO_DEG));
		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.setYBodyRot(yaw);
		this.setXRot(Mth.clamp(pitch, -85.0F, 85.0F));
	}

	private void tickBallisticFlight() {
		Vec3 target = this.forcedDestination;
		if (target == null) {
			this.launchFlightComplete = true;
			return;
		}
		this.setNoGravity(false);
		this.ballisticLaunchTicks++;
		this.maximumLaunchY = Math.max(this.maximumLaunchY, this.getY());

		Vec3 velocity = this.getDeltaMovement();
		if (!this.isIgnited() && this.ballisticLaunchTicks >= this.scheduledFuseIgnitionTick) {
			this.ignite();
			this.fuseIgnitedAtLaunchTick = this.ballisticLaunchTicks;
		}

		if (this.flightPhase == FlightPhase.BALLISTIC_ASCENT && this.ballisticLaunchTicks > 2 && velocity.y <= 0.0) {
			this.flightPhase = FlightPhase.BALLISTIC_DESCENT;
			this.reachedBallisticApex = true;
			this.everDived = true;
		}

		if (this.flightPhase == FlightPhase.BALLISTIC_DESCENT && velocity.y < -0.03) {
			this.descendedUnderGravity = true;
		}

		this.fixedVerticalVelocity = (this.fixedVerticalVelocity - BALLISTIC_GRAVITY) * BALLISTIC_VERTICAL_DRAG;
		Vec3 fixedCourseVelocity = new Vec3(
			this.fixedHorizontalVelocity.x,
			this.fixedVerticalVelocity,
			this.fixedHorizontalVelocity.z
		);
		this.setDeltaMovement(fixedCourseVelocity);
		this.faceVelocity(fixedCourseVelocity);

		double distanceSqr = this.position().distanceToSqr(target);
		this.closestLaunchTargetDistanceSqr = Math.min(this.closestLaunchTargetDistanceSqr, distanceSqr);
		if (distanceSqr <= BALLISTIC_IMPACT_RADIUS * BALLISTIC_IMPACT_RADIUS) {
			if (!this.reachedLaunchTarget) {
				this.reachedLaunchTarget = true;
				this.targetReachedAtLaunchTick = this.ballisticLaunchTicks;
			}
			if (!this.isIgnited()) {
				this.ignite();
				this.fuseIgnitedAtLaunchTick = this.ballisticLaunchTicks;
			}
			this.launchFlightComplete = true;
		} else if (
			this.flightPhase == FlightPhase.BALLISTIC_DESCENT
				&& this.onGround()
				&& this.ballisticLaunchTicks > 2
		) {
			this.launchFlightComplete = true;
		}
	}

	public enum FlightPhase {
		SEARCHING,
		CRUISING,
		DIVING,
		BALLISTIC_ASCENT,
		BALLISTIC_DESCENT
	}

	private final class BombVillageGoal extends Goal {
		private BombVillageGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return !FlyCreeper.this.isPassenger() && FlyCreeper.this.acquireTarget();
		}

		@Override
		public boolean canContinueToUse() {
			return FlyCreeper.this.isAlive() && !FlyCreeper.this.isPassenger() && FlyCreeper.this.acquireTarget();
		}

		@Override
		public void start() {
			FlyCreeper.this.getNavigation().stop();
			FlyCreeper.this.setNoGravity(!FlyCreeper.this.launched);
			FlyCreeper.this.everAirborne = true;
			if (!FlyCreeper.this.launched) {
				FlyCreeper.this.triggerTakeoffFirework();
			}
		}

		@Override
		public void stop() {
			if (!FlyCreeper.this.isIgnited() && !FlyCreeper.this.launched) {
				FlyCreeper.this.setNoGravity(false);
				FlyCreeper.this.flightPhase = FlightPhase.SEARCHING;
			}
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			Vec3 target = FlyCreeper.this.getAttackDestination();
			if (target == null) {
				return;
			}

			if (FlyCreeper.this.launched) {
				FlyCreeper.this.getLookControl().setLookAt(target.x, target.y, target.z, 90.0F, 90.0F);
				return;
			}

			double horizontalDistance = Math.sqrt(
				Mth.square(target.x - FlyCreeper.this.getX()) + Mth.square(target.z - FlyCreeper.this.getZ())
			);
			boolean diving = FlyCreeper.this.flightPhase == FlightPhase.DIVING
				|| horizontalDistance <= DIVE_START_DISTANCE;

			Vec3 steeringTarget;
			if (diving) {
				FlyCreeper.this.flightPhase = FlightPhase.DIVING;
				FlyCreeper.this.everDived = true;
				steeringTarget = target;
				if (!FlyCreeper.this.isIgnited()) {
					FlyCreeper.this.ignite();
				}
			} else {
				FlyCreeper.this.flightPhase = FlightPhase.CRUISING;
				steeringTarget = new Vec3(target.x, target.y + 12.0, target.z);
			}

			FlyCreeper.this.getLookControl().setLookAt(target.x, target.y, target.z, 90.0F, 90.0F);
			FlyCreeper.this.flyToward(steeringTarget, diving);
		}
	}
}
