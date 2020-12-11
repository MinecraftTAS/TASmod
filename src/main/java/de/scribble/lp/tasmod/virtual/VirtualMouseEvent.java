package de.scribble.lp.tasmod.virtual;

/**
 * Template for recording Mouse.next() events.
 * @author ScribbleLP
 *
 */
public class VirtualMouseEvent {
	private int keyCode;
	private boolean state;
	private int scrollwheel;
	private int mouseX;
	private int mouseY;
	private int slotidx;
	
	
	public VirtualMouseEvent(int keycode, boolean state, int scrollwheel, int mouseX, int mouseY, int slotidx) {
		this.keyCode=keycode;
		this.state=state;
		this.scrollwheel=scrollwheel;
		this.mouseX=mouseX;
		this.mouseY=mouseY;
		this.slotidx=slotidx;
	}
	public int getKeyCode() {
		return keyCode;
	}
	public boolean isState() {
		return state;
	}
	public int getScrollwheel() {
		return scrollwheel;
	}
	public int getMouseX() {
		return mouseX;
	}
	public int getMouseY() {
		return mouseY;
	}
	public int getSlotidx() {
		return slotidx;
	}
}
