package com.minecrafttas.tasmod.mixin.killtherng;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.minecrafttas.tasmod.TASmod;

import net.minecraft.entity.passive.EntitySquid;

@Mixin(EntitySquid.class)
public class MixinEntitySquid {

	@WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"))
	public boolean remove_setSeed(Random rand, long seed) {
		if (TASmod.squidRNG.nextInt(1000) == 0) {
			TASmod.LOGGER.error("SQUIDS ARE EVIL!!");
		}
		return false;
	}
}
