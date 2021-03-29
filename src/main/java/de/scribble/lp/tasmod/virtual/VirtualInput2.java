package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;

public class VirtualInput2 {
	
	private VirtualKeyboard currentKeyboard=new VirtualKeyboard();
	
	private VirtualKeyboard nextKeyboard=new VirtualKeyboard();
	
	public VirtualKeyboard getPreviousKeyboard() {
		return currentKeyboard;
	}
	
	public VirtualKeyboard getCurrentKeyboard() {
		return nextKeyboard;
	}
	
	public void updateNextKeyboard(int keycode, boolean keystate, char character) {
		VirtualKey key=nextKeyboard.get(keycode);
		key.setPressed(keystate);
		nextKeyboard.addChar(character);
	}
	
	public List<VirtualKeyboardEvent> getCurrentKeyboardEvents(){
		List<VirtualKeyboardEvent> out=currentKeyboard.getDifference(nextKeyboard);
		nextKeyboard.clearCharList();
		try {
			currentKeyboard=nextKeyboard.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	public void clearNextKeyboard() {
		nextKeyboard.clear();
	}
	
	public boolean isKeyDown(int keycode) {
		return currentKeyboard.get(keycode).isKeyDown();
	}
	
	public boolean isKeyDown(String keyname) {
		return currentKeyboard.get(keyname).isKeyDown();
	}
	
	public boolean willKeyBeDown(int keycode) {
		return nextKeyboard.get(keycode).isKeyDown();
	}
	
	public boolean willKeyBeDown(String keyname) {
		return nextKeyboard.get(keyname).isKeyDown();
	}
}
