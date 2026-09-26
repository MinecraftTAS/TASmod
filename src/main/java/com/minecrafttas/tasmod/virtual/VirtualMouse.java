package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

/**
 * Stores the mouse specific values in a given timeframe<br>
 * <br>
 * Similar to {@link VirtualKeyboard}, but instead of a list of characters,<br>
 * it stores the state of the scroll wheel and the cursors x and y coordinate on screen.
 *
 * @author Scribble
 * @see VirtualInput.VirtualMouseInput
 */
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
	public VirtualMouse() {
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
	public VirtualMouse(Set<Integer> pressedKeys, int scrollWheel, int cursorX, int cursorY, List<VirtualMouse> subtickList) {
		this(pressedKeys, scrollWheel, cursorX, cursorY, subtickList, false);
	}

	/**
	 * Creates a mouse from existing values
	 * 
	 * @param pressedKeys		The list of {@link #pressedKeys}
	 * @param scrollWheel		The {@link #scrollWheel}
	 * @param cursorX			The {@link #cursorX}
	 * @param cursorY			The {@link #cursorY}
	 * @param subtickList		The {@link VirtualPeripheral#subtickList}
	 * @param ignoreFirstUpdate	Whether the first call to {@link #updateFromEvent(int, boolean, int, Integer, Integer)} should create a new subtick
	 */
	public VirtualMouse(Set<Integer> pressedKeys, int scrollWheel, int cursorX, int cursorY, List<VirtualMouse> subtickList, boolean ignoreFirstUpdate) {
		super(pressedKeys, subtickList, ignoreFirstUpdate);
		this.scrollWheel = scrollWheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	/**
	 * Updates the mouse, adds a new subtick to this mouse<br>
	 * <br>
	 * An event updates one key at a time.
	 * @param keycode The keycode of this button
	 * @param keystate The keystate of this button, true for pressed
	 * @param scrollwheel The scroll wheel for this mouse
	 * @param cursorX The pointer location in the x axis
	 * @param cursorY The pointer location in the y axis
	 */
	public void updateFromEvent(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
		createSubtick();
		setPressed(keycode, keystate);
		this.scrollWheel = scrollwheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	/**
	 * Updates the mouse, adds a new subtick to this mouse<br>
	 * <br>
	 * An event updates one key at a time.
	 * 
	 * @param key The key
	 * @param keystate The keystate of this button, true for pressed
	 * @param scrollwheel The scroll wheel for this mouse
	 * @param cursorX The pointer location in the x axis
	 * @param cursorY The pointer location in the y axis
	 */
	public void updateFromEvent(VirtualKey key, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
		updateFromEvent(key.getKeycode(), keystate, scrollwheel, cursorX, cursorY);
	}

	/**
	 * Updates this mouse from a state, and adds a new subtick.<br>
	 * <br>
	 * The difference to {@link #updateFromEvent(int, boolean, int, Integer, Integer) updateFromEvent} is,<br>
	 * that a state may update multiple pressed keys at once.<br>
	 * <br>
	 * While update fromEvent is used when the player inputs something on the mouse,<br>
	 * updateFromState is used when creating a VirtualMouse by deserialising the TASfile,<br>
	 * as the inputs in the TASfile are stored in states.
	 * 
	 * @param keycodes A set of keycodes, that replaces {@link Subtickable#pressedKeys pressedKeys}
	 * @param scrollwheel The scroll wheel of this mouse state
	 * @param cursorX The pointer location in the x axis
	 * @param cursorY The pointer location in the y axis
	 * @see SerialiserFlavorBase#deserialiseMouse 
	 */
	public void updateFromState(Set<Integer> keycodes, int scrollwheel, int cursorX, int cursorY) {
		createSubtick();

		this.pressedKeys.clear();
		this.pressedKeys.addAll(keycodes);

		this.scrollWheel = scrollwheel;
		this.cursorX = cursorX;
		this.cursorY = cursorY;
	}

	@Override
	public void createSubtick() {
		if (isParent() && !ignoreFirstUpdate()) {
			addSubtick(shallowClone());
		}
	}

	@Override
	public void setPressed(int keycode, boolean keystate) {
		if (keycode < 0) { // Mouse buttons always have a keycode smaller than 0
			super.setPressed(keycode, keystate);
		}
	}

	/**
	 * Calculates a list of {@link VirtualMouseEvent VirtualMouseEvents}, when comparing this mouse to
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
			for (VirtualMouse subtick : nextMouse.getAll()) {
				currentSubtick.getDifference(subtick, reference);
				currentSubtick = subtick;
			}
		}
	}

	/**
	 * Calculates the difference between 2 mice via symmetric difference <br>
	 * and returns a list of the changes between them in form of
	 * {@link VirtualMouseEvent VirtualMouseEvents}
	 *
	 * @param nextMouse The mouse that comes after this one.<br>
	 *                  If this one is loaded at tick 15, the nextMouse should be
	 *                  the one from tick 16
	 * @param reference The queue to fill. Passed in by reference.
	 */
	public void getDifference(VirtualMouse nextMouse, Queue<VirtualMouseEvent> reference) {

		/*
		 * Checks if pressedKeys are the same...
		 */
		if (pressedKeys.equals(nextMouse.pressedKeys)) {

			/*
			 * ...but scrollWheel, cursorX or cursorY are different.
			 * Without this, the scrollWheel would only work if a mouse button is pressed at the same time. 
			 * 
			 * (#198) Additionally, we also need to check if the scroll wheel is not 0.
			 * Otherwise, repeated usage of the scrollWheel will result in #equals being true,
			 * which doesn't trigger the if clause like it should.
			 */
			if (!equals(nextMouse) || scrollWheel != 0) {
				reference.add(new VirtualMouseEvent(VirtualKey.MOUSEMOVED.getKeycode(), false, nextMouse.scrollWheel, nextMouse.cursorX, nextMouse.cursorY));
			}
			return;
		}
		int scrollWheelCopy = nextMouse.scrollWheel;
		int cursorXCopy = nextMouse.cursorX;
		int cursorYCopy = nextMouse.cursorY;

		/* Calculate symmetric difference of keycodes */

		/*
		    Calculate unpressed keys
		    this: LC RC
		    next: LC    MC
		    -------------
		             RC     <- unpressed
		 */
		for (int keycode : pressedKeys) {
			if (!nextMouse.getPressedKeys().contains(keycode)) {
				reference.add(new VirtualMouseEvent(keycode, false, scrollWheelCopy, cursorXCopy, cursorYCopy));
				scrollWheelCopy = 0;
				cursorXCopy = 0;
				cursorYCopy = 0;
			}
		}
		;

		/*
		 	Calculate pressed keys
		 	next: LC    MC
		 	this: LC RC
		 	-------------
		 	            MC <- pressed
		 */
		for (int keycode : nextMouse.getPressedKeys()) {
			if (!this.pressedKeys.contains(keycode)) {
				reference.add(new VirtualMouseEvent(keycode, true, scrollWheelCopy, cursorXCopy, cursorYCopy));
			}
		}
		;
	}

	@Override
	public void clear() {
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

	public String toString2() {
		return String.format("%s;%s,%s,%s", super.toString(), scrollWheel, cursorX, cursorY);
	}

	/**
	 * Clones this VirtualMouse <strong>without</strong> subticks
	 */
	public VirtualMouse shallowClone() {
		return new VirtualMouse(new HashSet<>(this.pressedKeys), scrollWheel, cursorX, cursorY, null, ignoreFirstUpdate());
	}

	@Override
	public VirtualMouse clone() {
		return new VirtualMouse(new HashSet<>(this.pressedKeys), scrollWheel, cursorX, cursorY, new ArrayList<>(subtickList), isIgnoreFirstUpdate());
	}

	@Override
	public void moveFrom(VirtualMouse mouse) {
		if (mouse == null)
			return;
		super.moveFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
		mouse.scrollWheel = 0;
	}

	@Override
	public void copyFrom(VirtualMouse mouse) {
		if (mouse == null)
			return;
		super.copyFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
	}

	@Override
	public void deepCopyFrom(VirtualMouse mouse) {
		if (mouse == null)
			return;
		super.deepCopyFrom(mouse);
		this.scrollWheel = mouse.scrollWheel;
		this.cursorX = mouse.cursorX;
		this.cursorY = mouse.cursorY;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualMouse) {
			VirtualMouse mouse = (VirtualMouse) obj;
			return super.equals(obj) && scrollWheel == mouse.scrollWheel && cursorX == mouse.cursorX && cursorY == mouse.cursorY;
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

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && scrollWheel == 0 && cursorX == 0 && cursorY == 0;
	}

	public void getStates(List<VirtualMouse> reference) {
		if (isParent()) {
			reference.addAll(subtickList);
			reference.add(this.shallowClone());
		}
	}
}

