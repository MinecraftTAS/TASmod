package de.scribble.lp.tasmod.virtual;

import java.util.Map;

import com.google.common.collect.Maps;

@Deprecated
public class VirtualChar {
	private char name;
	private boolean pressed;
	static Map<Character, VirtualChar> keyChars= Maps.<Character, VirtualChar>newHashMap();
	
	public VirtualChar(char name, boolean pressed) {
		this.name=name;
		this.pressed=pressed;
		keyChars.put(name, this);
	}
	public void setPressed(boolean pressed) {
		this.pressed=pressed;
	}
	public char getName() {
		return name;
	}
	public boolean isPressed() {
		return pressed;
	}
}
