package com.minecrafttas.tasmod.mixin.playbackhooks;

import com.minecrafttas.tasmod.virtual.VirtualInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualMouseInput;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Shadow
	private GuiScreen currentScreen;
	
	/**
	 * Runs every frame.
	 * @see VirtualInput#update(GuiScreen)
	 * @param ci CBI
	 */
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void playback_injectRunGameLoop(CallbackInfo ci) {
		TASmodClient.virtual.update(currentScreen);
	}
	
	// ============================ Keyboard
	
	/**
	 * Run at the start of run tick keyboard. Runs every tick.
	 * @see VirtualInput.VirtualKeyboardInput#nextKeyboardTick()
	 * @param ci CBI
	 */
	@Inject(method = "runTickKeyboard", at = @At(value = "HEAD"))
	public void playback_injectRunTickKeyboard(CallbackInfo ci) {
		TASmodClient.virtual.KEYBOARD.nextKeyboardTick();
	}
	
	/**
	 * Redirects a {@link org.lwjgl.input.Keyboard#next()}. Starts running every tick and continues as long as there are {@link VirtualKeyboardEvent}s in {@link VirtualInput}
	 * @see VirtualInput.VirtualKeyboardInput#nextKeyboardSubtick()
	 * @return If {@link VirtualKeyboardEvent}s are present in {@link VirtualInput}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean playback_redirectKeyboardNext() {
		return TASmodClient.virtual.KEYBOARD.nextKeyboardSubtick();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardKey()}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int playback_redirectKeyboardGetEventKey() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardKey();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardState()}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean playback_redirectGetEventState() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardState();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardCharacter()}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char playback_redirectKeyboardGetEventCharacter() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardCharacter();
	}
	
	/**
	 * Runs everytime {@link #playback_redirectKeyboardNext()} has an event ready. Redirects {@link org.lwjgl.input.Keyboard#isKeyDown(int)}
	 * @see VirtualInput.VirtualKeyboardInput#isKeyDown(int)
	 * @return Whether the key is down in {@link VirtualInput}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	public boolean playback_redirectIsKeyDown(int keyCode) {
		return TASmodClient.virtual.KEYBOARD.isKeyDown(keyCode);
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardKey()}
	 */
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int playback_redirectGetEventKeyDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardKey();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardState()}
	 */
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean playback_redirectGetEventKeyStateDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardState();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualKeyboardInput#getEventKeyboardCharacter()}
	 */
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char playback_redirectGetEventCharacterDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardCharacter();
	}
	
	// ============================ Mouse
	
	/**
	 * Run at the start of run tick mouse. Runs every tick.
	 * @see VirtualInput.VirtualMouseInput#nextMouseTick()
	 * @param ci CBI
	 */
	@Inject(method = "runTickMouse", at = @At(value = "HEAD"))
	public void playback_injectRunTickMouse(CallbackInfo ci) {
		TASmodClient.virtual.MOUSE.nextMouseTick();
	}
	
	/**
	 * Redirects a {@link org.lwjgl.input.Mouse#next()}. Starts running every tick and continues as long as there are {@link VirtualMouseEvent}s in {@link VirtualInput}
	 * @see VirtualInput.VirtualMouseInput#nextMouseSubtick()
	 * @return If {@link VirtualMouseInput}s are present in {@link VirtualInput}
	 */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean playback_redirectMouseNext() {
		return TASmodClient.virtual.MOUSE.nextMouseSubtick();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualMouseInput#getEventMouseKey()}
	 */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int playback_redirectMouseGetEventButton() {
		return TASmodClient.virtual.MOUSE.getEventMouseKey() + 100;
	}
	
	/**
	 * @return {@link VirtualInput.VirtualMouseInput#getEventMouseState()}
	 */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean playback_redirectGetEventButtonState() {
		return TASmodClient.virtual.MOUSE.getEventMouseState();
	}
	
	/**
	 * @return {@link VirtualInput.VirtualMouseInput#getEventMouseScrollWheel()}
	 */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int playback_redirectGetEventDWheel() {
		return TASmodClient.virtual.MOUSE.getEventMouseScrollWheel();
	}
	
}
