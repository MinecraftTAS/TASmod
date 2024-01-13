package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VirtualMouse2 extends VirtualPeripheral implements Serializable {

	/**
	 * The direction of the scrollWheel<br>
	 * <br>
	 * If the number is positive or negative depending on scroll direction.
	 */
	private final int scrollWheel;
	/**
	 * X coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private final Integer cursorX;
	/**
	 * Y coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private final Integer cursorY;

	/**
	 * Creates a mouse with no buttons pressed and no data
	 */
	public VirtualMouse2(){
		this(new HashSet<>(), 0, null, null);
	}

	/**
	 * Creates a mouse from existing values
	 * @param pressedKeys The list o {@link #pressedKeys}
	 * @param scrollWheel The {@link #scrollWheel}
	 * @param cursorX The {@link #cursorX}
	 * @param cursorY The {@link #cursorY}
	 */
	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY) {
		super(pressedKeys);
		this.scrollWheel = scrollWheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	@Override
	protected void setPressed(int keycode, boolean keystate) {
		if(keycode < 0){
			super.setPressed(keycode, keystate);
		}
	}

	@Override
	protected <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral) {
		List<VirtualMouseEvent> eventList = new ArrayList<>();

		int scrollWheelCopy = scrollWheel;
		Integer cursorXCopy = cursorX;
		Integer cursorYCopy = cursorY;

		/* Calculate symmetric difference of keycodes */

        /*
            Calculate unpressed keys
            this: LC RC
            next: LC    MC
            -------------
                     RC     <- unpressed
         */
		for(int keycode : pressedKeys) {
			if (!nextPeripheral.getPressedKeys().contains(keycode)) {
				eventList.add(new VirtualMouseEvent(keycode, false, scrollWheelCopy, cursorXCopy, cursorYCopy));
				scrollWheelCopy = 0;
				cursorXCopy = null;
				cursorYCopy = null;
			}
		};

		/*
		 	Calculate pressed keys
		 	next: LC    MC
		 	this: LC RC
		 	-------------
		 	            MC <- pressed
		 */
		for(int keycode : nextPeripheral.getPressedKeys()) {
			if (!this.pressedKeys.contains(keycode)) {
				eventList.add(new VirtualMouseEvent(keycode, true, scrollWheelCopy, cursorXCopy, cursorYCopy));
			}
		};

		return eventList;
	}

	@Override
	public String toString() {
		return String.format("%s;%s,%s,%s", super.toString(), scrollWheel, cursorX, cursorY);
	}

	@Override
	protected VirtualMouse2 clone() {
		return new VirtualMouse2(this.pressedKeys, scrollWheel, cursorX, cursorY);
	}
}
