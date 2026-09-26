package com.minecrafttas.tasmod.mixin.fixes;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

/**
 * Disables MouseCursor grabbing when playing back and pauseOnLostFocus is false
 * 
 * @author Scribble
 */
@Mixin(MouseHandler.class)
public class MixinMouseHelper {
	@Inject(method = "grabMouseCursor", at = @At(value = "HEAD"), cancellable = true)
	private void fixes_grabMouseCursor(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (TASmodClient.controller.isPlayingback() && !mc.options.pauseOnLostFocus && !mc.getWindow().isFocused())
			ci.cancel();
	}

	@Inject(method = "ungrabMouseCursor", at = @At(value = "HEAD"), cancellable = true)
	private void fixes_ungrabMouseCursor(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (TASmodClient.controller.isPlayingback() && !mc.options.pauseOnLostFocus && !mc.getWindow().isFocused())
			ci.cancel();
	}
}
