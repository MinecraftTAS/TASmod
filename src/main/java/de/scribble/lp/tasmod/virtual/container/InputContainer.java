package de.scribble.lp.tasmod.virtual.container;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;

public class InputContainer {
	private boolean playback=false;
	
	private boolean recording=false;
	
	private int index;
	
	private VirtualKeyboard keyboard = new VirtualKeyboard();
	
	private VirtualMouse mouse = new VirtualMouse();
	
	private VirtualSubticks subticks = new VirtualSubticks();
	
	private List<TickInputContainer> inputs= new ArrayList<TickInputContainer>();
	
	public boolean isPlayingback() {
		return playback;
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public VirtualKeyboard addKeyboardToContainer(VirtualKeyboard keyboard)  {
		this.keyboard=keyboard.clone();
		return keyboard;
	}
	
	public VirtualMouse addMouseToContainer(VirtualMouse mouse) {
		this.mouse=mouse.clone();
		return mouse;
	}
	
	public VirtualSubticks addSubticksToContainer(VirtualSubticks subticks) {
		this.subticks=subticks.clone();
		return subticks;
	}
	
	public void nextTick() {
		if(index<=20) {
			index++;
			inputs.add(new TickInputContainer(index, keyboard, mouse, subticks));
		}
	}
	
	public int size() {
		return inputs.size();
	}
	
	@Override
	public String toString() {
		if(inputs.isEmpty()) {
			return "null";
		}
		String out="";
		for (int i = 0; i < inputs.size(); i++) {
			out=out.concat(i+"|"+inputs.get(i).toString()+"\n");
		}
		return out;
	}
}
