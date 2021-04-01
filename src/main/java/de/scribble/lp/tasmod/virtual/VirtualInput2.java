package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualInput2 {

	private VirtualKeyboard currentKeyboard = new VirtualKeyboard();

	private VirtualKeyboard nextKeyboard = new VirtualKeyboard();

	private List<VirtualKeyboardEvent> currentKeyboardEvents = null;
	private Iterator<VirtualKeyboardEvent> currentKeyboardEventIterator = null;

	private VirtualKeyboardEvent currentKeyboardEvent = null;

	public VirtualKeyboard getCurrentKeyboard() {
		return currentKeyboard;
	}

	public VirtualKeyboard getNextKeyboard() {
		return nextKeyboard;
	}

	public void updateNextKeyboard(int keycode, boolean keystate, char character) {
		VirtualKey key = nextKeyboard.get(keycode);
		key.setPressed(keystate);
		nextKeyboard.addChar(character);
	}

	public List<VirtualKeyboardEvent> getCurrentKeyboardEvents() {
		return currentKeyboard.getDifference(nextKeyboard);
	}

	public void updateCurrentKeyboardEvents() {
		currentKeyboardEvents = getCurrentKeyboardEvents();
		currentKeyboardEventIterator = currentKeyboardEvents.iterator();

		nextKeyboard.clearCharList();

		try {
			currentKeyboard = nextKeyboard.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public boolean nextKeyboardEvent() {
		boolean hasnext = currentKeyboardEventIterator.hasNext();
		if (hasnext) {
			currentKeyboardEvent = currentKeyboardEventIterator.next();
		}
		return hasnext;
	}

	public int getEventKey() {
		return currentKeyboardEvent.getKeyCode();
	}

	public boolean getEventState() {
		return currentKeyboardEvent.isState();
	}

	public char getEventCharacter() {
		return currentKeyboardEvent.getCharacter();
	}

	public void clearNextKeyboard() {
		nextKeyboard.clear();
	}

	public boolean isKeyDown(int keycode) {
		return currentKeyboard.get(keycode).isKeyDown();
	}

	public boolean isKeyDown(String keyname) {
		return currentKeyboard.get(keyname).isKeyDown();
	}

	public boolean willKeyBeDown(int keycode) {
		return nextKeyboard.get(keycode).isKeyDown();
	}

	public boolean willKeyBeDown(String keyname) {
		return nextKeyboard.get(keyname).isKeyDown();
	}

	public List<String> getCurrentKeyboardPresses() {
		List<String> out = new ArrayList<String>();

		currentKeyboard.getKeyList().forEach((keycodes, virtualkeys) -> {
			if (keycodes >= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	public List<String> getNextKeyboardPresses() {
		List<String> out = new ArrayList<String>();

		nextKeyboard.getKeyList().forEach((keycodes, virtualkeys) -> {
			if (keycodes >= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	// =======================================================================================

	private VirtualMouse currentMouse = new VirtualMouse();

	private VirtualMouse nextMouse = new VirtualMouse();

	public VirtualMouse getCurrentMouse() {
		return currentMouse;
	}

	public VirtualMouse getNextMouse() {
		return nextMouse;
	}

	public void updateNextMouse(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY, boolean filter) {
		if(!filter) {
			VirtualKey key = nextMouse.get(keycode);
	
			if (keystate) {
				key.setTimesPressed(key.getTimesPressed() + 1);
			}
			key.setPressed(keystate);
	
			nextMouse.setScrollWheel(scrollwheel);
	
			nextMouse.addCursor(cursorX, cursorY);
		}

	}
	
	public List<VirtualMouseEvent> getCurrentMouseEvents(){
		return currentMouse.getDifference(nextMouse);
	}

	public void resetNextMouseLists() {
		nextMouse.getKeyList().forEach((keycodes, virtualkeys) -> {
			virtualkeys.resetTimesPressed();
		});
	}
}
