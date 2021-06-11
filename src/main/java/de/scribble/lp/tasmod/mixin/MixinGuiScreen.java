package de.scribble.lp.tasmod.mixin;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.duck.GuiScreenDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public class MixinGuiScreen implements GuiScreenDuck {

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isCreated()Z", shift = Shift.AFTER, remap = false))
	public void injectAfterKeyboardCreated(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentKeyboardEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean redirectKeyboardNext() {
		return ClientProxy.virtual.nextKeyboardEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectGetEventCharacter() {
		return ClientProxy.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectGetEventKey() {
		return ClientProxy.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventState() {
		return ClientProxy.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isCreated()Z", shift = Shift.AFTER, remap = false))
	public void injectAfterMouseCreated(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentMouseEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean redirectMouseNext() {
		return ClientProxy.virtual.nextMouseEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int redirectGetEventButton() {
		return ClientProxy.virtual.getEventMouseKey() + 100;
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean redirectGetEventButtonState() {
		if (ClientProxy.virtual.getContainer().isPlayingback()) {
			Mouse.setCursorPosition(uncalcX(ClientProxy.virtual.getEventCursorX()), uncalcY(ClientProxy.virtual.getEventCursorY()));
		}
		return ClientProxy.virtual.getEventMouseState();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventX()I", remap = false))
	public int redirectGetEventX() {
		return uncalcX(ClientProxy.virtual.getEventCursorX());
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventY()I", remap = false))
	public int redirectGetEventY() {
		return uncalcY(ClientProxy.virtual.getEventCursorY());
	}

	// =====================================================================================================================================

	@Redirect(method = "isCtrlKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsCtrlKeyDown(int i) {
		return ClientProxy.virtual.isKeyDown(i);
	}

	// =====================================================================================================================================

	@Redirect(method = "isShiftKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsShiftKeyDown(int i) {
		return ClientProxy.virtual.isKeyDown(i);
	}

	// =====================================================================================================================================

	@Redirect(method = "isAltKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsAltKeyDown(int i) {
		return ClientProxy.virtual.isKeyDown(i);
	}

	// =====================================================================================================================================

	@Shadow
	private int width;

	@Shadow
	private int height;

	@Shadow
	private Minecraft mc;

	@Override
	public int calcX(int X) {
		return X * this.width / this.mc.displayWidth;
	}

	@Override
	public int calcY(int Y) {
		return this.height - Y * this.height / this.mc.displayHeight - 1;
	}

	@Override
	public int uncalcX(int X) {
		return X * this.mc.displayWidth / this.width;
	}

	@Override
	public int uncalcY(int Y) {
		return (this.mc.displayHeight * (this.height - Y - 1) / this.height);
	}

}
