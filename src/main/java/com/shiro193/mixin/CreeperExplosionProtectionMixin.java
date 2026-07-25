package com.shiro193.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityBasedExplosionDamageCalculator.class)
public abstract class CreeperExplosionProtectionMixin {
	@Shadow
	@Final
	private Entity source;

	@Inject(method = "shouldBlockExplode", at = @At("HEAD"), cancellable = true)
	private void shirosTestMod$preserveObsidianFromCreepers(
		Explosion explosion,
		BlockGetter level,
		BlockPos pos,
		BlockState state,
		float power,
		CallbackInfoReturnable<Boolean> callback
	) {
		if (this.source instanceof Creeper && state.is(Blocks.OBSIDIAN)) {
			callback.setReturnValue(false);
		}
	}
}
