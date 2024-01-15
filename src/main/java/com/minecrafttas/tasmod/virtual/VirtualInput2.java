package com.minecrafttas.tasmod.virtual;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VirtualInput2 {
	
	private VirtualKeyboard2 currentKeyboard;

	private VirtualKeyboard2 nextKeyboard = new VirtualKeyboard2();
	
	private Queue<VirtualKeyboardEvent> keyboardEventQueue = new ConcurrentLinkedQueue<VirtualKeyboardEvent>();
	
	private VirtualMouse2 currentMouse;

	private VirtualMouse2 nextMouse = new VirtualMouse2();
	
	private Queue<VirtualKeyboardEvent> mouseEventQueue = new ConcurrentLinkedQueue<VirtualKeyboardEvent>();
	
	private VirtualCameraAngle cameraAngle;
	
	public VirtualInput2() {
		this(new VirtualKeyboard2(), new VirtualMouse2(), new VirtualCameraAngle(0, 0));
	}
	
	public VirtualInput2(VirtualKeyboard2 preloadedKeyboard, VirtualMouse2 preloadedMouse, VirtualCameraAngle preloadedCamera) {
		this.currentKeyboard = preloadedKeyboard;
		this.currentMouse = preloadedMouse;
		this.cameraAngle = preloadedCamera;
	}

	public void updateNextKeyboard(int keycode, boolean keystate, char character) {
		
	}
	
	public void updateCurrentKeyboard() {
		
	}
	
	
}
