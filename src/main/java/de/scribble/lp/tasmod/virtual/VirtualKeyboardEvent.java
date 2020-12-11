package de.scribble.lp.tasmod.virtual;

public class VirtualKeyboardEvent {
	private int keyCode;
	private boolean keystate;
	private char character;
	public VirtualKeyboardEvent(int keycode, boolean keystate, char character) {
		this.keyCode=keycode;
		this.keystate=keystate;
		this.character=character;
	}
	public int getKeyCode() {
		return keyCode;
	}
	public boolean isState() {
		return keystate;
	}
	public char getCharacter() {
		return character;
	}
}
