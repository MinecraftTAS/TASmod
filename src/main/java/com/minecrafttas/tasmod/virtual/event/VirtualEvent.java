package com.minecrafttas.tasmod.virtual.event;

public class VirtualEvent {
	protected final int keycode;
	protected final boolean keystate;

	public VirtualEvent(int keycode, boolean keystate) {
		this.keycode = keycode;
		this.keystate = keystate;
	}

	public VirtualEvent(VirtualEvent event) {
		this.keycode = event.keycode;
		this.keystate = event.keystate;
	}

	public int getKeyCode() {
		return keycode;
	}

	public boolean isState() {
		return keystate;
	}

	@Override
	public String toString() {
		return String.format("%s, %s", keycode, keystate);
	}
}
