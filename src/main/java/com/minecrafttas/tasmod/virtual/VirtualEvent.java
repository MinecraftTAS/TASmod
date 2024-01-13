package com.minecrafttas.tasmod.virtual;

public class VirtualEvent {

	public static class VirtualButtonEvent extends VirtualEvent{

		protected final int keycode;
		protected final boolean keystate;
		
		public VirtualButtonEvent(int keycode, boolean keystate) {
			this.keycode = keycode;
			this.keystate = keystate;
		}

		public VirtualButtonEvent(VirtualButtonEvent event) {
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
}
