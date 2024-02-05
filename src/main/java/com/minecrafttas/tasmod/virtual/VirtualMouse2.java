package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
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
	private int cursorX;
	/**
	 * Y coordinate of the on-screen cursor, used in GUI screens.<br>
	 * When null, no change to the cursor is applied.
	 */
	private int cursorY;

	/**
	 * Creates a mouse with no buttons pressed and no data
	 */
	public VirtualMouse2(){
		this(new HashSet<>(), 0, 0, 0, new ArrayList<>(), true);
	}

	/**
	 * Creates a subtick mouse with {@link VirtualPeripheral#subtickList} uninitialized
	 * @param pressedKeys The new list of pressed keycodes for this subtickMouse
	 * @param scrollWheel The scroll wheel direction for this subtickMouse
	 * @param cursorX The X coordinate of the cursor for this subtickMouse
	 * @param cursorY The Y coordinate of the cursor for this subtickMouse
	 */
	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, int cursorX, int cursorY) {
		this(pressedKeys, scrollWheel, cursorX, cursorY, null, false);
	}

	/**
	 * Creates a mouse from existing values with
	 * {@link VirtualPeripheral#ignoreFirstUpdate} set to false
	 * 
	 * @param pressedKeys	The list of {@link #pressedKeys}
	 * @param scrollWheel	The {@link #scrollWheel}
	 * @param cursorX		The {@link #cursorX}
	 * @param cursorY		The {@link #cursorY}
	 * @param subtick		The {@link VirtualPeripheral#subtickList}
	 */
	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse2> subtick) {
		this(pressedKeys, scrollWheel, cursorX, cursorY, subtick, false);
	}

	/**
	 * Creates a mouse from existing values
	 * 
	 * @param pressedKeys		The list of {@link #pressedKeys}
	 * @param scrollWheel		The {@link #scrollWheel}
	 * @param cursorX			The {@link #cursorX}
	 * @param cursorY			The {@link #cursorY}
	 * @param subtick			The {@link VirtualPeripheral#subtickList}
	 * @param ignoreFirstUpdate	Whether the first call to {@link #update(int, boolean, int, Integer, Integer)} should create a new subtick
	 */
	public VirtualMouse2(Set<Integer> pressedKeys, int scrollWheel, Integer cursorX, Integer cursorY, List<VirtualMouse2> subtick, boolean ignoreFirstUpdate) {
		super(pressedKeys, subtick, ignoreFirstUpdate);
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

	public void getVirtualEvents(VirtualMouse2 nextMouse, Queue<VirtualMouseEvent> reference) {
		if (isParent()) {
			VirtualMouse2 currentSubtick = this;
			for(VirtualMouse2 subtick : nextMouse.getAll()) {
				currentSubtick.getDifference(subtick, reference);
				currentSubtick = subtick;
			}
		}
	}

	public void getDifference(VirtualMouse2 nextPeripheral, Queue<VirtualMouseEvent> reference) {

		if(pressedKeys.equals(nextPeripheral.pressedKeys)){
			reference.add(new VirtualMouseEvent(VirtualKey2.MOUSEMOVED.getKeycode(), false, nextPeripheral.scrollWheel, nextPeripheral.cursorX, nextPeripheral.cursorY));
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
	
	private void clearMouseData() {
		scrollWheel = 0;
		cursorX = 0;
		cursorY = 0;
	}
	
	
	@Override
	public String toString() {
		if (isParent()) {
			return getSubticks().stream().map(VirtualMouse2::toString2).collect(Collectors.joining("\n"));
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
	public VirtualMouse2 clone() {
		return new VirtualMouse2(new HashSet<>(this.pressedKeys), scrollWheel, cursorX, cursorY, null, ignoreFirstUpdate());
	}

	@Override
	protected void copyFrom(VirtualMouse2 mouse) {
		super.copyFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
		mouse.clearMouseData();
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
	
	public int getScrollWheel() {
		return scrollWheel;
	}
	
	public int getCursorX() {
		return cursorX;
	}
	
	public int getCursorY() {
		return cursorY;
	}
}
