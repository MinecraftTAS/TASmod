package de.scribble.lp.tasmod.virtual;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Class to store which keys on the keyboard have been pressed, similar to how Keybindings work.<br>
 * Also stores the keynames and it's keycodes.
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