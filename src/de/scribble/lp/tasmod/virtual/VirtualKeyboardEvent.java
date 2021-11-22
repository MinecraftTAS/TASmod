package de.scribble.lp.tasmod.virtual;

public class VirtualKeyboardEvent {
	private int keycode;
	private boolean keystate;
	private char character;
	public VirtualKeyboardEvent(int keycode, boolean keystate, char character) {
		this.keycode=keycode;
		this.keystate=keystate;
		this.character=character;
	}
	public int getKeyCode() {
		return keycode;
	}
	public boolean isState() {
		return keystate;
	}
	public char getCharacter() {
		return character;
	}
	
	@Override
	public String toString() {
		return keycode+", "+keystate+", "+character;
	}
}
