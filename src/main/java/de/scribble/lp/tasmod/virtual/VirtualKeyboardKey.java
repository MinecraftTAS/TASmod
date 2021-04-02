package de.scribble.lp.tasmod.virtual;

/**
 * Class to store which keys on the keyboard have been pressed, similar to how Keybindings work.<br>
 * 
 * @author ScribbleLP
 *
 */
public class VirtualKeyboardKey {
	
	private String name;
	private int keycode;
	private boolean isKeyDown=false;
	
	public VirtualKeyboardKey(String name, int keycode) {
		this.name = name;
		this.keycode = keycode;
	}
	
	private VirtualKeyboardKey(String name, int keycode, boolean isKeyDown) {
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
	public boolean equals(Object obj) {
		VirtualKeyboardKey key = (VirtualKeyboardKey) obj;
		if(key.isKeyDown!=isKeyDown) return false;
		if(key.keycode!=keycode) return false;
		return true;
	}
	
	@Override
	public VirtualKeyboardKey clone() {
		return new VirtualKeyboardKey(name, keycode, isKeyDown);
	}
}
