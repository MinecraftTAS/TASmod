package com.minecrafttas.tasmod.virtual;

import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class VirtualMouse extends VirtualPeripheral<VirtualMouse> implements Serializable {

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
	private int cursorX;
	/**
	 * Y coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private int cursorY;

	/**
	 * Creates a mouse with no buttons pressed and no data
	 */
	public VirtualMouse(){
		this(new LinkedHashSet<>(), 0, 0, 0, new ArrayList<>(), true);
	}

	/**
	 * Creates a subtick mouse with {@link Subtickable#subtickList} uninitialized
	 * @param pressedKeys The new list of pressed keycodes for this subtickMouse
	 * @param scrollWheel The scroll wheel direction for this subtickMouse
	 * @param cursorX The X coordinate of the cursor for this subtickMouse
	 * @param cursorY The Y coordinate of the cursor for this subtickMouse
	 */
	public VirtualMouse(Set<Integer> pressedKeys, int scrollWheel, int cursorX, int cursorY) {
		this(pressedKeys, scrollWheel, cursorX, cursorY, null);
	}

	/**
	 * Creates a mouse from existing values with
	 * {@link VirtualPeripheral#ignoreFirstUpdate} set to false
	 * 
	 * @param pressedKeys	The list of {@link #pressedKeys}
	 * @param scrollWheel	The {@link #scrollWheel}
	 * @param cursorX		The {@link #cursorX}
	 * @param cursorY		The {@link #cursorY}
	 * @param subtickList		The {@link VirtualPeripheral#subtickList}
	 */
	public VirtualMouse(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse> subtickList) {
		this(pressedKeys, scrollWheel, cursorX, cursorY, subtickList, false);
	}

	/**
	 * Creates a mouse from existing values
	 * 
	 * @param pressedKeys		The list of {@link #pressedKeys}
	 * @param scrollWheel		The {@link #scrollWheel}
	 * @param cursorX			The {@link #cursorX}
	 * @param cursorY			The {@link #cursorY}
	 * @param subtickList			The {@link VirtualPeripheral#subtickList}
	 * @param ignoreFirstUpdate	Whether the first call to {@link #update(int, boolean, int, Integer, Integer)} should create a new subtick
	 */
	public VirtualMouse(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse> subtickList, boolean ignoreFirstUpdate) {
		super(pressedKeys, subtickList, ignoreFirstUpdate);
		this.scrollWheel = scrollWheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	public void update(int keycode, boolean keystate, int scrollwheel, Integer cursorX, Integer cursorY) {
    	if(isParent() && !ignoreFirstUpdate()) {
    		addSubtick(clone());
    	}
		setPressed(keycode, keystate);
		this.scrollWheel = scrollwheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	@Override
	public void setPressed(int keycode, boolean keystate) {
		if (keycode < 0) {	// Mouse buttons always have a keycode smaller than 0
			super.setPressed(keycode, keystate);
		}
	}

	/**
	 * Calculates a list of {@link VirtualMouseEvent}s, when comparing this mouse to
	 * the next mouse in the sequence,<br>
	 * which also includes the subticks.
	 *
	 * @see VirtualMouse#getDifference(VirtualMouse, Queue)
	 *
	 * @param nextMouse The mouse that comes after this one.<br>
	 *                  If this one is loaded at tick 15, the nextMouse should be
	 *                  the one from tick 16
	 * @param reference The queue to fill. Passed in by reference.
	 */
	public void getVirtualEvents(VirtualMouse nextMouse, Queue<VirtualMouseEvent> reference) {
		if (isParent()) {
			VirtualMouse currentSubtick = this;
			for(VirtualMouse subtick : nextMouse.getAll()) {
				currentSubtick.getDifference(subtick, reference);
				currentSubtick = subtick;
			}
		}
	}

	/**
	 * Calculates the difference between 2 mice via symmetric difference <br>
	 * and returns a list of the changes between them in form of
	 * {@link VirtualMouseEvent}s
	 *
	 * @param nextMouse The mouse that comes after this one.<br>
	 *                  If this one is loaded at tick 15, the nextMouse should be
	 *                  the one from tick 16
	 * @param reference The queue to fill. Passed in by reference.
	 */
	public void getDifference(VirtualMouse nextPeripheral, Queue<VirtualMouseEvent> reference) {
		
		/*
		 * Checks if pressedKeys are the same...
		 */
		if(pressedKeys.equals(nextPeripheral.pressedKeys)){
			
			/**
			 * ...but scrollWheel, cursorX or cursorY are different.
			 * Without this, the scrollWheel would only work if a mouse button is pressed at the same time. 
			 */
			if(!equals(nextPeripheral)) {
				reference.add(new VirtualMouseEvent(VirtualKey.MOUSEMOVED.getKeycode(), false, nextPeripheral.scrollWheel, nextPeripheral.cursorX, nextPeripheral.cursorY));
			}
			return;
		}
		int scrollWheelCopy = nextPeripheral.scrollWheel;
		int cursorXCopy = nextPeripheral.cursorX;
		int cursorYCopy = nextPeripheral.cursorY;

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
				cursorXCopy = 0;
				cursorYCopy = 0;
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

	@Override
	protected void clear() {
		super.clear();
		clearMouseData();
	}
	
	/**
	 * Resets mouse specific data to it's defaults
	 */
	private void clearMouseData() {
		scrollWheel = 0;
		cursorX = 0;
		cursorY = 0;
	}
	
	
	@Override
	public String toString() {
		if (isParent()) {
			return getAll().stream().map(VirtualMouse::toString2).collect(Collectors.joining("\n"));
		} else {
			return toString2();
		}
	}
	
	private String toString2(){
		return String.format("%s;%s,%s,%s", super.toString(), scrollWheel, cursorX, cursorY);
	}

	/**
	 * Clones this VirtualMouse <strong>without</strong> subticks
	 */
	@Override
	public VirtualMouse clone() {
		return new VirtualMouse(new HashSet<>(this.pressedKeys), scrollWheel, cursorX, cursorY, null, ignoreFirstUpdate());
	}

	@Override
	public void copyFrom(VirtualMouse mouse) {
		super.copyFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
		mouse.clearMouseData();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualMouse) {
			VirtualMouse mouse = (VirtualMouse) obj;
			return super.equals(obj) && 
					scrollWheel == mouse.scrollWheel &&
					cursorX == mouse.cursorX &&
					cursorY == mouse.cursorY;
		}
		return super.equals(obj);
	}
	
	/**
	 * @return {@link #scrollWheel}
	 */
	public int getScrollWheel() {
		return scrollWheel;
	}
	
	/**
	 * @return {@link #cursorX}
	 */
	public int getCursorX() {
		return cursorX;
	}
	
	/**
	 * @return {@link #cursorY}
	 */
	public int getCursorY() {
		return cursorY;
	}
}
