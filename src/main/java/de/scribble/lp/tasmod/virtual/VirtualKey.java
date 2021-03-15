package de.scribble.lp.tasmod.virtual;

/**
 * Class to store which keys on the keyboard have been pressed, similar to how Keybindings work.<br>
 * 
 * @author ScribbleLP
 *
 */
public class VirtualKey {
	
	private String name;
	private int keycode;
	private boolean isKeyDown=false;
	public VirtualKey(String name, int keycode) {
		this.name=name;
		this.keycode=keycode;
	}
	public String getName() {
		return name;
	}
	public int getKeycode() {
		return keycode;
	}
	public boolean isKeyDown() {
		return isKeyDown;
	}
	public void setPressed(boolean pressed) {
		isKeyDown=pressed;
	}
}
