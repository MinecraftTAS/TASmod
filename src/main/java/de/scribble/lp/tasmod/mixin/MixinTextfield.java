package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.gui.GuiTextField;

@Mixin(GuiTextField.class)
public class MixinTextfield {

	@Inject(at = @At("HEAD"), method = "setFocused", cancellable = true)
	public void injectsetFocused(boolean focused, CallbackInfo ci) {
		VirtualKeybindings.focused = focused;
	}
	
}
