package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		return ClientProxy.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "getEventKey"))
	public int redirectGetEventKey() {
		return ClientProxy.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "getEventKeyState"))
	public boolean redirectGetEventState() {
		return ClientProxy.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isCreated()V", shift = Shift.AFTER))
	public void injectAfterMouseCreated(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentMouseEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z"))
	public boolean redirectMouseNext() {
		return ClientProxy.virtual.nextMouseEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "getEventButton"))
	public int redirectGetEventButton() {
		return ClientProxy.virtual.getEventMouseKey()+100;
	}

	// =====================================================================================================================================
	
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "getEventButtonState"))
	public boolean redirectGetEventButtonState() {
		return ClientProxy.virtual.getEventMouseState();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "getEventX"))
	public int redirectGetEventX() {
		return ClientProxy.virtual.getEventCursorX();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "getEventY"))
	public int redirectGetEventY() {
		return ClientProxy.virtual.getEventCursorY();
	}
	
}
