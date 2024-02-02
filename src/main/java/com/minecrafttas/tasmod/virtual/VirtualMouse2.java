package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualMouse2 extends VirtualPeripheral<VirtualMouse2> implements Serializable {

	/**
	 * The direction of the scrollWheel<br>
	 * <br>
	 * If the number is positive or negative depending on scroll direction.
	 */
	private int scrollWheel;
	/**
	 * X coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private Integer cursorX;
	/**
	 * Y coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private Integer cursorY;

	/**
	 * Creates a mouse with no buttons pressed and no data
	 */
	public VirtualMouse2(){
		this(new HashSet<>(), 0, null, null, null, true);
	}

	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse2> subtick){
		this(pressedKeys, scrollWheel, cursorX, cursorY, subtick, false);
	}

	/**
	 * Creates a mouse from existing values
	 * 
	 * @param pressedKeys The list o {@link #pressedKeys}
	 * @param scrollWheel The {@link #scrollWheel}
	 * @param cursorX     The {@link #cursorX}
	 * @param cursorY     The {@link #cursorY}
	 */
	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse2> subtick, boolean ignoreFirstUpdate) {
		super(pressedKeys, subtick, ignoreFirstUpdate);
		this.scrollWheel = scrollWheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	public void update(int keycode, boolean keystate, int scrollwheel, Integer cursorX, Integer cursorY) {
		setPressed(keycode, keystate);
		this.scrollWheel = scrollwheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;

		if(isParent()) {
			addSubtick(clone());
		}
	}

	@Override
	protected void setPressed(int keycode, boolean keystate) {
		if (keycode < 0) {
			super.setPressed(keycode, keystate);
		}
	}

	public void getDifference(VirtualMouse2 nextPeripheral, Queue<VirtualMouseEvent> reference) {

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
				reference.add(new VirtualMouseEvent(keycode, false, scrollWheelCopy, cursorXCopy, cursorYCopy));
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
				reference.add(new VirtualMouseEvent(keycode, true, scrollWheelCopy, cursorXCopy, cursorYCopy));
			}
		};
	}

	public void getVirtualEvents(VirtualMouse2 nextMouse, Queue<VirtualMouseEvent> reference) {
		if (isParent()) {
			VirtualMouse2 currentSubtick = this;
			for(VirtualMouse2 subtick : nextMouse.getAll()) {
				currentSubtick.getDifference(subtick, reference);
				currentSubtick = subtick;
			}
		}
	}

	@Override
	protected void clear() {
		super.clear();
		clearMouseData();
	}
	
	private void clearMouseData() {
		scrollWheel = 0;
		cursorX = null;
		cursorY = null;
	}
	
	
	@Override
	public String toString() {
		if (isParent()) {
			return getSubticks().stream().map(VirtualMouse2::toString).collect(Collectors.joining("\n"));
		} else {
			return String.format("%s;%s,%s,%s", super.toString(), scrollWheel, cursorX, cursorY);
		}
	}

	/**
	 * Clones this VirtualMouse <strong>without</strong> subticks
	 */
	@Override
	public VirtualMouse2 clone() {
		return new VirtualMouse2(new HashSet<>(this.pressedKeys), scrollWheel, cursorX, cursorY, null, ignoreFirstUpdate());
	}

	@Override
	protected void copyFrom(VirtualMouse2 mouse) {
		super.copyFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualMouse2) {
			VirtualMouse2 mouse = (VirtualMouse2) obj;
			return super.equals(obj) && 
					scrollWheel == mouse.scrollWheel &&
					cursorX == mouse.cursorX &&
					cursorY == mouse.cursorY;
		}
		return super.equals(obj);
	}
}
