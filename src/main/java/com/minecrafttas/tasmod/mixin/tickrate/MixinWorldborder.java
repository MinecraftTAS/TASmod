package com.minecrafttas.tasmod.mixin.tickrate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public class MixinWorldborder {

	@ModifyVariable(method = "renderWorldBorder", at = @At(value = "STORE"), index = 20, ordinal = 4)
	public float injectf3(float f) {
		return (TASmodClient.tickratechanger.getMilliseconds() % 3000L) / 3000.0F;
	}

}

