package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.virtual.VirtualInput2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboardEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Shadow
	private GuiScreen currentScreen;
	
	/**
	 * Runs every frame.
	 * @see VirtualInput2#update(GuiScreen)
	 * @param ci CBI
	 */
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void playback_injectRunGameLoop(CallbackInfo ci) {
		TASmodClient.virtual.update(currentScreen);
	}
	
	/**
	 * Run at the start of run tick keyboard. Runs every tick.
	 * @param ci CBI
	 */
	@Inject(method = "runTickKeyboard", at = @At(value = "HEAD"))
	public void playback_injectRunTickKeyboard(CallbackInfo ci) {
		TASmodClient.virtual.KEYBOARD.nextKeyboardTick();
	}
	
	/**
	 * Redirects a {@link Keyboard#next()}. Starts running every tick and continues as long as there are {@link VirtualKeyboardEvent}s in {@link VirtualInput}
	 * @see com.minecrafttas.tasmod.virtual.VirtualInput2.VirtualKeyboardInput#nextKeyboardSubtick()
	 * @return If {@link VirtualKeyboardEvent}s are present in {@link VirtualInput2}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean playback_redirectKeyboardNext() {
		return TASmodClient.virtual.KEYBOARD.nextKeyboardSubtick();
	}
	
	/**
	 * Runs everytime {@link #playback_redirectKeyboardNext()} has an event ready. Redirects {@link Keyboard#getEventKeyState()}
	 * @return The keycode for the current event in {@link VirtualInput2.VirtualKeyboardInput}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int playback_redirectKeyboardGetEventKey() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardKey();
	}
	
	/**
	 * Runs everytime {@link #playback_redirectKeyboardNext()} has an event ready. Redirects {@link Keyboard#getEventKeyState()}
	 * @return Whether the key is down in {@link VirtualInput2}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean playback_redirectGetEventState() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardState();
	}
	
	/**
	 * Runs everytime {@link #playback_redirectKeyboardNext()} has an event ready. Redirects {@link Keyboard#getEventCharacter()}
	 * @return The character for the current event in {@link VirtualInput2}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char playback_redirectKeyboardGetEventCharacter() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardCharacter();
	}
	
	/**
	 * Runs everytime {@link #playback_redirectKeyboardNext()} has an event ready. Redirects {@link Keyboard#isKeyDown(int)}
	 * @return Whether the key is down in {@link VirtualInput2}
	 */
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	public boolean playback_redirectIsKeyDown(int keyCode) {
		return TASmodClient.virtual.KEYBOARD.isKeyDown(keyCode);
	}
	
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int playback_redirectGetEventKeyDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardKey();
	}
	
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean playback_redirectGetEventKeyStateDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardState();
	}
	
	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char playback_redirectGetEventCharacterDPK() {
		return TASmodClient.virtual.KEYBOARD.getEventKeyboardCharacter();
	}
	
	// ============================ Mouse
	
	@Inject(method = "runTickMouse", at = @At(value = "HEAD"))
	public void playback_injectRunTickMouse(CallbackInfo ci) {
		TASmodClient.virtual.MOUSE.nextMouseTick();
	}
	
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean playback_redirectMouseNext() {
		return TASmodClient.virtual.MOUSE.nextMouseSubtick();
	}
	
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int playback_redirectMouseGetEventButton() {
		
//		if(!VirtualKeybindings.isKeyCodeAlwaysBlocked(ClientProxy.virtual.getEventMouseKey()-100)) {
//			TASmod.ktrngHandler.nextPlayerInput(); // Advance ktrng seed on player input
//		}
		return TASmodClient.virtual.MOUSE.getEventMouseKey() + 100;
	}
	
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean playback_redirectGetEventButtonState() {
		return TASmodClient.virtual.MOUSE.getEventMouseState();
	}
	
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int playback_redirectGetEventDWheel() {
		return TASmodClient.virtual.MOUSE.getEventMouseScrollWheel();
	}
	
}
