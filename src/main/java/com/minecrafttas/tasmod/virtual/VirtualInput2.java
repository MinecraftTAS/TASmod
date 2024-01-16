package com.minecrafttas.tasmod.virtual;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.minecrafttas.tasmod.mixin.playbackhooks.MixinMinecraft;

public class VirtualInput2 {
	
	private VirtualKeyboard2 currentKeyboard;
	private VirtualKeyboard2 nextKeyboard = new VirtualKeyboard2();
	private Queue<VirtualKeyboardEvent> keyboardEventQueue = new ConcurrentLinkedQueue<VirtualKeyboardEvent>();
	private VirtualKeyboardEvent currentKeyboardEvent;
	
	
	public VirtualInput2() {
		this(new VirtualKeyboard2(), new VirtualMouse2(), new VirtualCameraAngle(0, 0));
	}
	
	public VirtualInput2(VirtualKeyboard2 preloadedKeyboard, VirtualMouse2 preloadedMouse, VirtualCameraAngle preloadedCamera) {
		this.currentKeyboard = preloadedKeyboard;
		this.currentMouse = preloadedMouse;
		this.cameraAngle = preloadedCamera;
	}
	
	public void updateNextKeyboard(int keycode, boolean keystate, char character) {
		nextKeyboard.update(keycode, keystate, character);
	}
	
	/**
	 * Runs when the next keyboard tick is about to occur.<br>
	 * Used to load {@link #nextKeyboard} into {@link #currentKeyboard}, creating {@link VirtualKeyboardEvent}s in the process.
	 * @see MixinMinecraft#playback_injectRunTickKeyboard(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public void nextKeyboardTick() {
		currentKeyboard.getVirtualEvents(nextKeyboard, keyboardEventQueue);
		currentKeyboard.moveFrom(nextKeyboard);
	}
	
	public boolean nextKeyboardSubtick() {
		return (currentKeyboardEvent = keyboardEventQueue.poll()) != null;
	}

	public int getEventKeyboardKey() {
		return currentKeyboardEvent.getKeyCode();
	}

	public boolean getEventKeyboardState() {
		return currentKeyboardEvent.isState();
	}

	public char getEventKeyboardCharacter() {
		return currentKeyboardEvent.getCharacter();
	}
	
	private VirtualMouse2 currentMouse;
	private VirtualMouse2 nextMouse = new VirtualMouse2();
	private Queue<VirtualKeyboardEvent> mouseEventQueue = new ConcurrentLinkedQueue<VirtualKeyboardEvent>();
	private VirtualCameraAngle cameraAngle;
}
