package de.scribble.lp.tasmod.virtual;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

/**
 * Class to store which keys on the keyboard have been pressed, similar to how Keybindings work.<br>
 * Also stores the keynames and it's keycodes.
 * @author ScribbleLP
 *
 */
public class VirtualKeys {
	static Map<String, VirtualKeys> keyNames= Maps.<String, VirtualKeys>newHashMap();
	static Map<Integer, VirtualKeys> keyCodes= Maps.<Integer, VirtualKeys>newHashMap();
	private String name;
	private int keycode;
	private boolean isKeyDown=false;
	public VirtualKeys(String name, int keycode) {
		this.name=name;
		this.keycode=keycode;
		this.keyNames.put(name, this);
		this.keyCodes.put(keycode, this);
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
