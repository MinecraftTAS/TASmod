package de.scribble.lp.tasmod.mixin;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At.Shift;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public class MixinGuiScreen2 {

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isCreated()V", shift = Shift.AFTER))
	public void injectAfterKeyboardCreated(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentKeyboardEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z"))
	public boolean redirectKeyboardNext() {
		return ClientProxy.virtual.nextKeyboardEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "getEventCharacter"))
	public char redirectGetEventCharacter() {
		return ClientProxy.virtual.getEventCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "getEventKey"))
	public int redirectGetEventKey() {
		return ClientProxy.virtual.getEventKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "getEventState"))
	public boolean redirectGetEventState() {
		return ClientProxy.virtual.getEventState();
	}

	// =====================================================================================================================================

}
