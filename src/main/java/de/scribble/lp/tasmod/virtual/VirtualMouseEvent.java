package de.scribble.lp.tasmod.virtual;

/**
 * Template for recording Mouse.next() events.
 * 
 * @author ScribbleLP
 *
 */
public class VirtualMouseEvent {
	private int keycode;
	private boolean state;
	private int scrollwheel;
	private int mouseX;
	private int mouseY;

	public VirtualMouseEvent(int keycode, boolean state, int scrollwheel, int mouseX, int mouseY) {
		this.keycode = keycode;
		this.state = state;
		this.scrollwheel = scrollwheel;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public int getKeyCode() {
		return keycode;
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
	
	@Override
		public String toString() {
			// TODO Auto-generated method stub
			return keycode+", "+state+", "+scrollwheel+", "+mouseX+", "+mouseY;
		}
}
