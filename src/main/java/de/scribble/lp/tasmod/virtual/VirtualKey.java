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
		this.name = name;
		this.keycode = keycode;
	}
	
	private VirtualKey(String name, int keycode, boolean isKeyDown) {
		this.name = name;
		this.keycode = keycode;
		this.isKeyDown = isKeyDown;
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
	
	@Override
	public VirtualKey clone() {
		return new VirtualKey(name, keycode, isKeyDown);
	}
}
