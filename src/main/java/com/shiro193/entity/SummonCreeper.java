package com.shiro193.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SummonCreeper extends Creeper {
	public static final int FLY_SUMMON_INTERVAL_TICKS = 10 * 20;
	public static final int CMD_SUMMON_INTERVAL_TICKS = 30 * 20;

	private int summonTimerTicks;
	private int spawnLightningEffectsTriggered;
	private int timedFlySummons;
	private int timedCmdSummons;

	public SummonCreeper(EntityType<? extends SummonCreeper> type, Level level) {
		super(type, level);
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
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
			&& spawnReason != EntitySpawnReason.CONVERSION) {
			this.triggerSpawnLightning(level.getLevel());
		}

		return result;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel serverLevel) || !this.isAlive()) {
			return;
		}

		this.summonTimerTicks++;
		if (this.summonTimerTicks % FLY_SUMMON_INTERVAL_TICKS == 0) {
			FlyCreeper fly = ModEntities.FLY_CREEPER.spawn(
				serverLevel,
				this.findSummonPosition(1),
				EntitySpawnReason.MOB_SUMMONED
			);
			if (fly != null) {
				fly.setPersistenceRequired();
				this.timedFlySummons++;
			}
		}

		if (this.summonTimerTicks % CMD_SUMMON_INTERVAL_TICKS == 0) {
			CmdCreeper cmd = ModEntities.CMD_CREEPER.spawn(
				serverLevel,
				this.findSummonPosition(2),
				EntitySpawnReason.MOB_SUMMONED
			);
			if (cmd != null) {
				cmd.setPersistenceRequired();
				this.timedCmdSummons++;
			}
		}
	}

	private void triggerSpawnLightning(ServerLevel level) {
		LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (lightning == null) {
			return;
		}

		lightning.snapTo(this.getX(), this.getY(), this.getZ());
		lightning.setVisualOnly(true);
		level.addFreshEntity(lightning);
		this.spawnLightningEffectsTriggered++;
	}

	private BlockPos findSummonPosition(int salt) {
		int xOffset = salt == 1 ? 1 : -1;
		int zOffset = (this.summonTimerTicks / FLY_SUMMON_INTERVAL_TICKS) % 2 == 0 ? 1 : -1;
		return BlockPos.containing(this.getX() + xOffset, this.getY(), this.getZ() + zOffset);
	}

	public int getSummonTimerTicks() {
		return this.summonTimerTicks;
	}

	public int getSpawnLightningEffectsTriggered() {
		return this.spawnLightningEffectsTriggered;
	}

	public int getTimedFlySummons() {
		return this.timedFlySummons;
	}

	public int getTimedCmdSummons() {
		return this.timedCmdSummons;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SummonTimerTicks", this.summonTimerTicks);
		output.putInt("SpawnLightningEffectsTriggered", this.spawnLightningEffectsTriggered);
		output.putInt("TimedFlySummons", this.timedFlySummons);
		output.putInt("TimedCmdSummons", this.timedCmdSummons);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.summonTimerTicks = input.getIntOr("SummonTimerTicks", 0);
		this.spawnLightningEffectsTriggered = input.getIntOr("SpawnLightningEffectsTriggered", 0);
		this.timedFlySummons = input.getIntOr("TimedFlySummons", 0);
		this.timedCmdSummons = input.getIntOr("TimedCmdSummons", 0);
	}
}
