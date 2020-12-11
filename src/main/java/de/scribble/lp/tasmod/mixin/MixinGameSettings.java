package de.scribble.lp.tasmod.mixin;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Mixin(GameSettings.class)
public class MixinGameSettings {
	
	@Redirect(method = "isKeyDown", at = @At(value= "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z",ordinal = 0))
	private static boolean redirectIsKeyDown1(int i, KeyBinding key) {
		return VirtualMouseAndKeyboard.isKeyDown(i+100);
	}
	@Redirect(method = "isKeyDown", at = @At(value= "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z",ordinal = 0))
	private static boolean redirectIsKeyDown2(int i, KeyBinding key) {
		return VirtualMouseAndKeyboard.isKeyDown(i);
	}
}
