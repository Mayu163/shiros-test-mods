package com.shiro193.entity;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class VillageTargeting {
	private VillageTargeting() {
	}

	static Optional<Target> findNearest(Mob seeker, int range) {
		if (!(seeker.level() instanceof ServerLevel level)) {
			return Optional.empty();
		}

		Optional<Villager> nearestVillager = level
			.getEntitiesOfClass(Villager.class, seeker.getBoundingBox().inflate(range), Villager::isAlive)
			.stream()
			.min(Comparator.comparingDouble(seeker::distanceToSqr));
		if (nearestVillager.isPresent()) {
			Villager villager = nearestVillager.get();
			return Optional.of(new Target(villager.getBoundingBox().getCenter(), villager));
		}

		return level.getPoiManager()
			.findClosest(holder -> holder.is(PoiTypeTags.VILLAGE), seeker.blockPosition(), range, PoiManager.Occupancy.ANY)
			.map(pos -> new Target(Vec3.atCenterOf(pos), null));
	}

	record Target(Vec3 position, @Nullable Villager villager) {
		Vec3 currentPosition() {
			return this.villager != null && this.villager.isAlive()
				? this.villager.getBoundingBox().getCenter()
				: this.position;
		}
	}
}
