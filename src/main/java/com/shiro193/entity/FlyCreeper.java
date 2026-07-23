package com.shiro193.entity;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.EnumSet;
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
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FlyCreeper extends Creeper {
	public static final int TARGET_RANGE = 128;
	private static final double DIVE_START_DISTANCE = 15.0;
	private static final double CRUISE_SPEED = 0.78;
	private static final double DIVE_SPEED = 1.18;
	private static final double BALLISTIC_IMPACT_RADIUS = 2.0;
	public static final int TAKEOFF_FIREWORK_DURATION_TICKS = 80;
	public static final int TAKEOFF_FIREWORK_PRIMARY_PARTICLES_PER_TICK = 4;
	public static final int TAKEOFF_FIREWORK_TRAILING_PARTICLES_PER_TICK = 3;
	public static final int TAKEOFF_FIREWORK_COLOR_BURST_INTERVAL_TICKS = 5;
	// Vanilla uses no negative entity-event IDs; this range avoids packet-global casts.
	private static final byte RAINBOW_FIREWORK_EVENT_BASE = Byte.MIN_VALUE;
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

	private VillageTargeting.@Nullable Target villageTarget;
	private @Nullable Vec3 forcedDestination;
	private FlightPhase flightPhase = FlightPhase.SEARCHING;
	private boolean launched;
	private boolean everAirborne;
	private boolean everDived;
	private boolean reachedBallisticApex;
	private boolean descendedUnderGravity;
	private boolean reachedLaunchTarget;
	private int ballisticLaunchTicks;
	private double launchStartY;
	private double maximumLaunchY;
	private double closestLaunchTargetDistanceSqr = Double.POSITIVE_INFINITY;
	private double initialLaunchSpeed;
	private double ballisticHorizontalSpeed;
	private double referenceHorizontalLaunchSpeed;
	private double legacyLaunchOriginHeightOffset;
	private double launchOriginHeightOffset;
	private double referenceTrajectoryApexHeight;
	private double predictedTrajectoryApexHeight;
	private int predictedImpactTicks;
	private int scheduledFuseIgnitionTick;
	private int fuseIgnitedAtLaunchTick = -1;
	private int targetReachedAtLaunchTick = -1;
	private int detonatedAtLaunchTick = -1;
	private int takeoffFireworkTicks;
	private int takeoffFireworkEffectsTriggered;
	private int takeoffFireworkBurstsEmitted;
	private int takeoffFireworkParticlesEmitted;
	private int takeoffFireworkColorBurstsEmitted;
	private int takeoffFireworkColorMask;

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

	public void launchAt(
		Vec3 destination,
		Vec3 initialVelocity,
		int predictedImpactTicks,
		double referenceTrajectoryApexHeight,
		double predictedTrajectoryApexHeight,
		double referenceHorizontalLaunchSpeed,
		double legacyLaunchOriginHeightOffset,
		double launchOriginHeightOffset
	) {
		if (this.isPassenger()) {
			this.stopRiding();
		}

		this.forcedDestination = destination;
		this.launched = true;
		this.flightPhase = FlightPhase.BALLISTIC_ASCENT;
		this.everAirborne = true;
		this.everDived = false;
		this.reachedBallisticApex = false;
		this.descendedUnderGravity = false;
		this.reachedLaunchTarget = false;
		this.ballisticLaunchTicks = 0;
		this.launchStartY = this.getY();
		this.maximumLaunchY = this.getY();
		this.closestLaunchTargetDistanceSqr = this.position().distanceToSqr(destination);
		this.initialLaunchSpeed = initialVelocity.length();
		this.ballisticHorizontalSpeed = Math.sqrt(initialVelocity.x * initialVelocity.x + initialVelocity.z * initialVelocity.z);
		this.referenceHorizontalLaunchSpeed = referenceHorizontalLaunchSpeed;
		this.legacyLaunchOriginHeightOffset = legacyLaunchOriginHeightOffset;
		this.launchOriginHeightOffset = launchOriginHeightOffset;
		this.referenceTrajectoryApexHeight = referenceTrajectoryApexHeight;
		this.predictedTrajectoryApexHeight = predictedTrajectoryApexHeight;
		this.predictedImpactTicks = Math.max(1, predictedImpactTicks);
		this.scheduledFuseIgnitionTick = Math.max(
			1,
			this.predictedImpactTicks - VANILLA_CREEPER_FUSE_TICKS + 1
		);
		this.fuseIgnitedAtLaunchTick = -1;
		this.targetReachedAtLaunchTick = -1;
		this.detonatedAtLaunchTick = -1;
		this.getNavigation().stop();
		this.setNoGravity(false);
		this.setDeltaMovement(initialVelocity);
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

	public double getHorizontalLaunchSpeedMultiplier() {
		return this.referenceHorizontalLaunchSpeed <= 0.0
			? 0.0
			: this.ballisticHorizontalSpeed / this.referenceHorizontalLaunchSpeed;
	}

	public double getLaunchOriginHeightOffset() {
		return this.launchOriginHeightOffset;
	}

	public double getLaunchOriginHeightMultiplier() {
		return this.legacyLaunchOriginHeightOffset <= 0.0
			? 0.0
			: this.launchOriginHeightOffset / this.legacyLaunchOriginHeightOffset;
	}

	public int getPredictedImpactTicks() {
		return this.predictedImpactTicks;
	}

	public double getReferenceTrajectoryApexHeight() {
		return this.referenceTrajectoryApexHeight;
	}

	public double getPredictedTrajectoryApexHeight() {
		return this.predictedTrajectoryApexHeight;
	}

	public double getPredictedTrajectoryHeightMultiplier() {
		return this.referenceTrajectoryApexHeight <= 0.0
			? 0.0
			: this.predictedTrajectoryApexHeight / this.referenceTrajectoryApexHeight;
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
		return this.takeoffFireworkTicks > 0;
	}

	public int getTakeoffFireworkEffectsTriggered() {
		return this.takeoffFireworkEffectsTriggered;
	}

	public int getTakeoffFireworkBurstsEmitted() {
		return this.takeoffFireworkBurstsEmitted;
	}

	public int getTakeoffFireworkParticlesEmitted() {
		return this.takeoffFireworkParticlesEmitted;
	}

	public int getTakeoffFireworkColorBurstsEmitted() {
		return this.takeoffFireworkColorBurstsEmitted;
	}

	public int getTakeoffFireworkDistinctColorCount() {
		return Integer.bitCount(this.takeoffFireworkColorMask);
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
		boolean trackingBallisticDetonation = this.launched && !this.isRemoved();
		super.tick();
		if (trackingBallisticDetonation && this.isRemoved() && this.detonatedAtLaunchTick < 0) {
			this.detonatedAtLaunchTick = this.ballisticLaunchTicks;
		}
		this.tickTakeoffFirework();
	}

	private void triggerTakeoffFirework() {
		if (this.level().isClientSide()) {
			return;
		}

		this.takeoffFireworkTicks = TAKEOFF_FIREWORK_DURATION_TICKS;
		this.takeoffFireworkEffectsTriggered++;
		this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.5F, 1.0F);
	}

	private void tickTakeoffFirework() {
		if (this.takeoffFireworkTicks <= 0 || !(this.level() instanceof ServerLevel serverLevel)) {
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
		int elapsedTicks = TAKEOFF_FIREWORK_DURATION_TICKS - this.takeoffFireworkTicks;
		if (elapsedTicks % TAKEOFF_FIREWORK_COLOR_BURST_INTERVAL_TICKS == 0) {
			int colorIndex = (elapsedTicks / TAKEOFF_FIREWORK_COLOR_BURST_INTERVAL_TICKS) % RAINBOW_FIREWORK_COLORS.length;
			serverLevel.broadcastEntityEvent(this, (byte)(RAINBOW_FIREWORK_EVENT_BASE + colorIndex));
			this.takeoffFireworkColorBurstsEmitted++;
			this.takeoffFireworkColorMask |= 1 << colorIndex;
		}
		this.takeoffFireworkTicks--;
	}

	@Override
	public void handleEntityEvent(byte id) {
		int colorIndex = id - RAINBOW_FIREWORK_EVENT_BASE;
		if (colorIndex < 0 || colorIndex >= RAINBOW_FIREWORK_COLORS.length || !this.level().isClientSide()) {
			super.handleEntityEvent(id);
			return;
		}

		Vec3 movement = this.getDeltaMovement();
		Vec3 trailOffset = movement.lengthSqr() > 1.0E-6
			? movement.normalize().scale(-0.75)
			: new Vec3(0.0, -0.75, 0.0);
		Vec3 emitter = this.position().add(0.0, this.getBbHeight() * 0.35, 0.0).add(trailOffset);
		int color = RAINBOW_FIREWORK_COLORS[colorIndex];
		int fadeColor = RAINBOW_FIREWORK_COLORS[(colorIndex + 1) % RAINBOW_FIREWORK_COLORS.length];
		this.level().createFireworks(
			emitter.x,
			emitter.y,
			emitter.z,
			movement.x * 0.15,
			movement.y * 0.15,
			movement.z * 0.15,
			List.of(
				new FireworkExplosion(
					FireworkExplosion.Shape.BURST,
					IntList.of(color),
					IntList.of(fadeColor),
					true,
					false
				)
			)
		);
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

	private void tickBallisticFlight(Vec3 target) {
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

		Vec3 horizontalOffset = new Vec3(target.x - this.getX(), 0.0, target.z - this.getZ());
		double horizontalDistance = horizontalOffset.length();
		Vec3 horizontalVelocity = Vec3.ZERO;
		if (horizontalDistance > 1.0E-6) {
			double speed = Math.min(this.ballisticHorizontalSpeed, horizontalDistance);
			horizontalVelocity = horizontalOffset.scale(speed / horizontalDistance);
		}

		Vec3 guidedVelocity = new Vec3(horizontalVelocity.x, velocity.y, horizontalVelocity.z);
		this.setDeltaMovement(guidedVelocity);
		this.faceVelocity(guidedVelocity);

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
				FlyCreeper.this.tickBallisticFlight(target);
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
