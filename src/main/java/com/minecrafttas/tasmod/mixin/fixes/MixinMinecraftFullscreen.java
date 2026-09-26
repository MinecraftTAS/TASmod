package com.minecrafttas.tasmod.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

@Mixin(Minecraft.class)
public class MixinMinecraftFullscreen {

	@Shadow
	private Options gameSettings;

	@Inject(method = "toggleFullscreen", at = @At("RETURN"))
	public void fixes_toggleFullscreen(CallbackInfo ci) {
		int keyF11 = this.gameSettings.keyFullscreen.getDefaultKey().getValue();
		TASmodClient.virtual.KEYBOARD.updateNextKeyboard(keyF11, false, Character.MIN_VALUE);
	}
}

