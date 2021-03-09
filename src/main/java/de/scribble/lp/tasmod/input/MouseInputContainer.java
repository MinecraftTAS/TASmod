package de.scribble.lp.tasmod.input;

public class MouseInputContainer {
	
	private final int keycode;
	
	private boolean pressed;
	
	/**
	 * If the key was already pressed one rotation
	 * 
	 */
	private int timespressed;
	
	public MouseInputContainer(int keycode, boolean pressed, int timespressed) {
		this.keycode=keycode;
		
		this.pressed=pressed;
		
		this.timespressed=timespressed;
	}
	
}
