package de.scribble.lp.tasmod.input;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.virtual.VirtualKeyboard;

public class InputContainer {
	private static List<VirtualKeyboard> keyboardContainer=new ArrayList<VirtualKeyboard>();
	
	public static void add(VirtualKeyboard keyboard) throws CloneNotSupportedException {
		keyboardContainer.add((VirtualKeyboard) keyboard.clone());
	}
	
	public static void print() {
		keyboardContainer.forEach(keyboard->{
			System.out.println(keyboard.getCurrentPresses()+"\n\n");
		});
	}
}
