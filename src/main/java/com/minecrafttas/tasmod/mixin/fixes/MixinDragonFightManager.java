package com.minecrafttas.tasmod.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.dimension.end.EnderDragonFight;

@Mixin(EnderDragonFight.class)
public abstract class MixinDragonFightManager {

	@Shadow
	public boolean scanForLegacyFight;
	@Shadow
	public boolean previouslyKilled;
	private int ticks = 20;

	@Inject(at = @At("RETURN"), method = "<init>")
	public void fixes_endInit(CallbackInfo ci) {
		scanForLegacyFight = false;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void fixes_tick(CallbackInfo ci) {
		ticks--;
		if (ticks == 0 && previouslyKilled) {
			scanForLegacyFight = true;
		}
	}

}

