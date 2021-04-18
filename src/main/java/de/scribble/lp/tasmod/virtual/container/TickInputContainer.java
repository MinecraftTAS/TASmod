package de.scribble.lp.tasmod.virtual.container;

import java.io.Serializable;

import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;

public class TickInputContainer implements Serializable {
	
	private int tick;
	
	private VirtualKeyboard keyboard;
	
	private VirtualMouse mouse;
	
	private VirtualSubticks subticks;

	public TickInputContainer(int tick, VirtualKeyboard keyboard, VirtualMouse mouse, VirtualSubticks subticks) {
		this.tick = tick;
		this.keyboard = keyboard;
		this.mouse = mouse;
		this.subticks = subticks;
	}
	
	public TickInputContainer(int tick) {
		this.tick = tick;
		this.keyboard = new VirtualKeyboard();
		this.mouse = new VirtualMouse();
		this.subticks = new VirtualSubticks(0, 0);
	}
	
	@Override
	public String toString() {
		return tick+"|"+keyboard.toString()+"|"+mouse.toString()+"|"+subticks.toString();
	}
	
	public VirtualKeyboard getKeyboard() {
		return keyboard;
	}
	
	public VirtualMouse getMouse() {
		return mouse;
	}
	
	public VirtualSubticks getSubticks() {
		return subticks;
	}
	
	public int getTick() {
		return tick;
	}
}
