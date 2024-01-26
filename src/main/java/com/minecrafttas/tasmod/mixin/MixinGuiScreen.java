package com.minecrafttas.tasmod.mixin;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.util.Ducks.GuiScreenDuck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public class MixinGuiScreen implements GuiScreenDuck {

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isCreated()Z", shift = Shift.AFTER, remap = false))
	public void injectAfterKeyboardCreated(CallbackInfo ci) {
		TASmodClient.virtual.KEYBOARD.nextKeyboardTick();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean redirectKeyboardNext() {
		return TASmodClient.virtual.KEYBOARD.nextKeyboardSubtick();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectGetEventCharacter() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectGetEventKey() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventState() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardState();
	}

	// =====================================================================================================================================

	@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isCreated()Z", shift = Shift.AFTER, remap = false))
	public void injectAfterMouseCreated(CallbackInfo ci) {
		TASmodClient.virtual.MOUSE.nextMouseTick();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean redirectMouseNext() {
		return TASmodClient.virtual.MOUSE.nextMouseSubtick();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int redirectGetEventButton() {
		return TASmodClient.virtual.MOUSE.getEventMouseKey() + 100;
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean redirectGetEventButtonState() {
		if (TASmodClient.controller.isPlayingback()) {
			Mouse.setCursorPosition(uncalcX(TASmodClient.virtual.MOUSE.getEventCursorX()), uncalcY(TASmodClient.virtual.MOUSE.getEventCursorY()));
		}
		return TASmodClient.virtual.MOUSE.getEventMouseState();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventX()I", remap = false))
	public int redirectGetEventX() {
		return uncalcX(TASmodClient.virtual.MOUSE.getEventCursorX());
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventY()I", remap = false))
	public int redirectGetEventY() {
		return uncalcY(TASmodClient.virtual.MOUSE.getEventCursorY());
	}

	// =====================================================================================================================================

	@Redirect(method = "isCtrlKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsCtrlKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	// =====================================================================================================================================

	@Redirect(method = "isShiftKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsShiftKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	// =====================================================================================================================================

	@Redirect(method = "isAltKeyDown", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsAltKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
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
