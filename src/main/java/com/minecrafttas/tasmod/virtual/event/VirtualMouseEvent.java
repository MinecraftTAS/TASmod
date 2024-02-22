package com.minecrafttas.tasmod.virtual.event;

/**
 * Template for recording {@link org.lwjgl.input.Mouse#next()} events.
 *
 * @author Scribble
 */
public class VirtualMouseEvent extends VirtualEvent {
	private final int scrollwheel;
	private final int cursorX;
	private final int cursorY;

	public VirtualMouseEvent() {
		this(0, false, 0, 0, 0);
	}

	public VirtualMouseEvent(int keycode, boolean state, int scrollwheel, int cursorX, int cursorY) {
		super(keycode, state);
		this.scrollwheel = scrollwheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	public int getScrollwheel() {
		return scrollwheel;
	}

	public Integer getCursorX() {
		return cursorX;
	}

	public Integer getCursorY() {
		return cursorY;
	}

	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s", super.toString(), scrollwheel, cursorX, cursorY);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualMouseEvent) {
			VirtualMouseEvent e = (VirtualMouseEvent) obj;
			return keycode == e.keycode && keystate == e.keystate && scrollwheel == e.scrollwheel && cursorX == e.cursorX && cursorY == e.cursorY;
		}
		return super.equals(obj);
	}
}
