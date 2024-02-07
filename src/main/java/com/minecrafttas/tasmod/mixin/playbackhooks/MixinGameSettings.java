package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Mixin(GameSettings.class)
public class MixinGameSettings {
	
	/**
	 * Redirect Mouse.isButtonDown in keybindings
	 * @param i The keycode
	 * @param key The keybinding
	 * @return Whether the key is down
	 */
	@Redirect(method = "isKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z", remap = false))
	private static boolean redirectIsKeyDown1(int i, KeyBinding key) {
		return TASmodClient.virtual.isKeyDown(i + 100);
	}

	/**
	 * Redirect Keyboard.isKeyDown in keybindings
	 * @param i The keycode
	 * @param key The keybinding
	 * @return Whether the key is down
	 */
	@Redirect(method = "isKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsKeyDown2(int i, KeyBinding key) {
		return TASmodClient.virtual.isKeyDown(i);
	}
}
