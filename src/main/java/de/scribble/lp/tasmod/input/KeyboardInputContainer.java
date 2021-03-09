package de.scribble.lp.tasmod.input;

public class KeyboardInputContainer {
	
	private final int keycode;
	
	private boolean pressed;
	
	public KeyboardInputContainer(int keycode, boolean pressed) {
		this.keycode=keycode;
		this.pressed=pressed;
	}
}
