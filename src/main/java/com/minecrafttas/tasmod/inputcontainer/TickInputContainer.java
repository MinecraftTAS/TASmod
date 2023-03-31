package com.minecrafttas.tasmod.inputcontainer;

import java.io.Serializable;

import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;
import com.minecrafttas.tasmod.virtual.VirtualSubticks;

public class TickInputContainer implements Serializable {

	private static final long serialVersionUID = -3420565284438152474L;

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
		return tick + "|" + keyboard.toString() + "|" + mouse.toString() + "|" + subticks.toString();
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

	public void setTick(int tick) {
		this.tick = tick;
	}
	
	@Override
	public TickInputContainer clone() {
		return new TickInputContainer(tick, keyboard, mouse, subticks);
	}
}
