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
	private int timesPressed=0;
	
	public VirtualKey(String name, int keycode) {
		this.name = name;
		this.keycode = keycode;
		this.timesPressed=0;
	}
	
	public VirtualKey(String name, int keycode, int timesPressed) {
		this.name = name;
		this.keycode = keycode;
		this.timesPressed=timesPressed;
	}
	
	private VirtualKey(String name, int keycode, boolean isKeyDown, int timesPressed) {
		this.name = name;
		this.keycode = keycode;
		this.isKeyDown = isKeyDown;
		this.timesPressed=timesPressed;
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
	
	public void setTimesPressed(int timesPressed) {
		this.timesPressed = timesPressed;
	}
	
	public int getTimesPressed() {
		return timesPressed;
	}
	
	public void resetTimesPressed() {
		timesPressed=0;
	}
	
	@Override
	public boolean equals(Object obj) {
		VirtualKey key = (VirtualKey) obj;
		if(key.isKeyDown!=isKeyDown) return false;
		if(key.keycode!=keycode) return false;
		return true;
	}
	
	@Override
	public VirtualKey clone() {
		return new VirtualKey(name, keycode, isKeyDown, timesPressed);
	}
}
