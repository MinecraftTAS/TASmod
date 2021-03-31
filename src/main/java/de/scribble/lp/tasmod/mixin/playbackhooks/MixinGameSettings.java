package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Mixin(GameSettings.class)
public class MixinGameSettings {
	
	@Redirect(method = "isKeyDown", at = @At(value= "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z",ordinal = 0, remap=false))
	private static boolean redirectIsKeyDown1(int i, KeyBinding key) {
		return ClientProxy.virtual.isKeyDown(i+100);
	}
	@Redirect(method = "isKeyDown", at = @At(value= "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z",ordinal = 0, remap=false))
	private static boolean redirectIsKeyDown2(int i, KeyBinding key) {
		return ClientProxy.virtual.isKeyDown(i);
	}
}
