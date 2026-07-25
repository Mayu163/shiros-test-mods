package com.shiro193.entity;

import java.util.EnumSet;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CmdCreeper extends Creeper {
	public static final int MAX_PAYLOAD = 2;
	public static final int TARGET_RANGE = 128;
	public static final double THROW_RANGE = 40.0;
	public static final double LAUNCH_ORIGIN_HEIGHT_MULTIPLIER = 1.65;

	private VillageTargeting.@Nullable Target villageTarget;
	private int totalThrown;
	private int throwGoalTicks;
	private int lastPredictedHitTicks;
	private double lastTargetDistanceSqr = Double.NaN;
	private int approachRequests;
	private int inRangeHoldTicks;

	public CmdCreeper(EntityType<? extends CmdCreeper> type, Level level) {
		super(type, level);
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes().add(Attributes.FOLLOW_RANGE, TARGET_RANGE);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new CarryAndThrowGoal());
		this.goalSelector.addGoal(2, new FloatGoal(this));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Villager.class, 16.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(
		ServerLevelAccessor level,
		DifficultyInstance difficulty,
		EntitySpawnReason spawnReason,
		@Nullable SpawnGroupData groupData
	) {
		SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
		if (spawnReason != EntitySpawnReason.LOAD
			&& spawnReason != EntitySpawnReason.DIMENSION_TRAVEL
			&& spawnReason != EntitySpawnReason.CONVERSION
			&& this.getPayloadCount() == 0) {
			for (int index = 0; index < MAX_PAYLOAD; index++) {
				FlyCreeper payload = ModEntities.FLY_CREEPER.create(level.getLevel(), EntitySpawnReason.JOCKEY);
				if (payload != null) {
					payload.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
					payload.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
					this.tryLoad(payload);
				}
			}
		}

		return result;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return passenger instanceof FlyCreeper && this.getPayloadCount() < MAX_PAYLOAD;
	}

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		return null;
	}

	public boolean tryLoad(FlyCreeper payload) {
		return this.getPayloadCount() < MAX_PAYLOAD && payload.startRiding(this);
	}

	public int getPayloadCount() {
		return (int)this.getPassengers().stream().filter(FlyCreeper.class::isInstance).count();
	}

	public int getTotalThrown() {
		return this.totalThrown;
	}

	public int getThrowGoalTicks() {
		return this.throwGoalTicks;
	}

	public int getLastPredictedHitTicks() {
		return this.lastPredictedHitTicks;
	}

	public double getLastTargetDistanceSqr() {
		return this.lastTargetDistanceSqr;
	}

	public int getApproachRequests() {
		return this.approachRequests;
	}

	public int getInRangeHoldTicks() {
		return this.inRangeHoldTicks;
	}

	public boolean hasVillageTarget() {
		return this.villageTarget != null;
	}

	public boolean throwOneAt(Vec3 target) {
		FlyCreeper payload = this.getPassengers()
			.stream()
			.filter(FlyCreeper.class::isInstance)
			.map(FlyCreeper.class::cast)
			.findFirst()
			.orElse(null);
		if (payload == null) {
			return false;
		}

		payload.stopRiding();
		double legacyLaunchHeightOffset = this.getBbHeight() + 0.35;
		double launchHeightOffset = legacyLaunchHeightOffset * LAUNCH_ORIGIN_HEIGHT_MULTIPLIER;
		Vec3 launchOrigin = this.position().add(0.0, launchHeightOffset, 0.0);
		payload.snapTo(launchOrigin.x, launchOrigin.y, launchOrigin.z, this.getYRot(), -10.0F);
		payload.launchAt(target);
		this.lastPredictedHitTicks = payload.getPredictedImpactTicks();
		this.totalThrown++;
		return true;
	}

	private boolean acquireTarget() {
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

	private final class CarryAndThrowGoal extends Goal {
		private int throwCooldown;

		private CarryAndThrowGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return CmdCreeper.this.getPayloadCount() > 0 && CmdCreeper.this.acquireTarget();
		}

		@Override
		public boolean canContinueToUse() {
			return CmdCreeper.this.isAlive() && CmdCreeper.this.getPayloadCount() > 0 && CmdCreeper.this.villageTarget != null;
		}

		@Override
		public void start() {
			this.throwCooldown = 10;
		}

		@Override
		public void stop() {
			CmdCreeper.this.getNavigation().stop();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			CmdCreeper.this.throwGoalTicks++;
			VillageTargeting.Target target = CmdCreeper.this.villageTarget;
			if (target == null) {
				return;
			}

			Vec3 targetPosition = target.currentPosition();
			CmdCreeper.this.getLookControl().setLookAt(targetPosition.x, targetPosition.y, targetPosition.z, 60.0F, 60.0F);
			double deltaX = targetPosition.x - CmdCreeper.this.getX();
			double deltaZ = targetPosition.z - CmdCreeper.this.getZ();
			double horizontalDistanceSqr = deltaX * deltaX + deltaZ * deltaZ;
			CmdCreeper.this.lastTargetDistanceSqr = horizontalDistanceSqr;
			if (horizontalDistanceSqr > THROW_RANGE * THROW_RANGE) {
				CmdCreeper.this.approachRequests++;
				CmdCreeper.this.getNavigation().moveTo(targetPosition.x, targetPosition.y, targetPosition.z, 1.0);
				return;
			}

			CmdCreeper.this.getNavigation().stop();
			CmdCreeper.this.inRangeHoldTicks++;
			Vec3 movement = CmdCreeper.this.getDeltaMovement();
			CmdCreeper.this.setDeltaMovement(0.0, movement.y, 0.0);
			if (--this.throwCooldown <= 0 && CmdCreeper.this.throwOneAt(targetPosition)) {
				this.throwCooldown = 24;
			}
		}
	}
}
