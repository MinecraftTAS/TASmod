package de.scribble.lp.tasmod.mixin;

import javax.swing.text.JTextComponent.KeyBinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
	@Redirect(method = "updateKeyBindState", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z"))
	public boolean redirectIsKeyDown(int i) {
		return VirtualMouseAndKeyboard.isKeyDown(i);
	}
}
