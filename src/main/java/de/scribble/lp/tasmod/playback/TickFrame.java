package de.scribble.lp.tasmod.playback;

import java.util.List;

import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import de.scribble.lp.tasmod.virtual.VirtualMouseEvent;

@Deprecated
public class TickFrame {
	private int tick;
	private List<VirtualMouseEvent> mouseevent;
	private List<VirtualKeyboardEvent> keyevent;
	public TickFrame(int tick, List<VirtualKeyboardEvent> keyevent, List<VirtualMouseEvent> mouseevent) {
		this.tick=tick;
		this.keyevent=keyevent;
		this.mouseevent=mouseevent;
	}
	public List<VirtualMouseEvent> getMouseEvent() {
		return mouseevent;
	}
	public List<VirtualKeyboardEvent> getKeyboarEvent(){
		return keyevent;
	}
}
