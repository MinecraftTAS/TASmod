package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventDrawScreen;
import com.minecrafttas.tasmod.util.Ducks.ScreenDuck;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class MixinGuiScreen implements ScreenDuck {

	/**
	 * Redirects a {@link org.lwjgl.glfw.GLFW#next()}. Starts running every tick and continues as long as there are {@link VirtualKeyboardEvent}s in {@link VirtualInput}
	 * @see VirtualInput.VirtualKeyboardInput#nextKeyboardSubtick()
	 * @return If {@link VirtualKeyboardEvent}s are present in {@link VirtualInput}
	 */
	@Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean redirectKeyboardNext() {
		return TASmodClient.virtual.KEYBOARD.nextKeyboardSubtick();
	}

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
		return TASmodClient.virtual.MOUSE.getEventMouseState();
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventX()I", remap = false))
	public int redirectGetEventX() {
		return rescaleX(TASmodClient.virtual.MOUSE.getEventCursorX());
	}

	// =====================================================================================================================================

	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventY()I", remap = false))
	public int redirectGetEventY() {
		return rescaleY(TASmodClient.virtual.MOUSE.getEventCursorY());
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

	@Inject(method = "drawScreen", at = @At("HEAD"))
	private void injectDrawScreen(int i, int j, float f, CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventDrawScreen.class, (Screen) (Object) this, i, j);
	}

	@Shadow
	private int width;

	@Shadow
	private int height;

	@Shadow
	private Minecraft mc;

	@Override
	public int unscaleX(int X) {
		return X * this.width / this.mc.getWindow().getWidth();
	}

	@Override
	public int unscaleY(int Y) {
		return this.height - Y * this.height / this.mc.getWindow().getHeight() - 1;
	}

	@Override
	public int rescaleX(int X) {
		return X * this.mc.getWindow().getWidth() / this.width;
	}

	@Override
	public int rescaleY(int Y) {
		return (this.mc.getWindow().getHeight() * (this.height - Y - 1) / this.height);
	}

}

