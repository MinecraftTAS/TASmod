package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualInput2 {

	private VirtualKeyboard currentKeyboard = new VirtualKeyboard();

	private VirtualKeyboard nextKeyboard = new VirtualKeyboard();

	private List<VirtualKeyboardEvent> currentKeyboardEvents = new ArrayList<VirtualKeyboardEvent>();
	private Iterator<VirtualKeyboardEvent> currentKeyboardEventIterator = currentKeyboardEvents.iterator();

	private VirtualKeyboardEvent currentKeyboardEvent = null;

	public VirtualKeyboard getCurrentKeyboard() {
		return currentKeyboard;
	}

	public VirtualKeyboard getNextKeyboard() {
		return nextKeyboard;
	}

	public void updateNextKeyboard(int keycode, boolean keystate, char character) {
		
		System.out.println(keycode+" "+keystate+" "+character);
		
		VirtualKey key = nextKeyboard.get(keycode);
		key.setPressed(keystate);
		if(keystate) {
			character=nextKeyboard.encodeUnicode(keycode, character);
		}
		nextKeyboard.addChar(character);
	}

	public List<VirtualKeyboardEvent> getCurrentKeyboardEvents() {
		return currentKeyboard.getDifference(nextKeyboard);
	}

	public void updateCurrentKeyboardEvents() {
		currentKeyboardEvents = getCurrentKeyboardEvents();
		currentKeyboardEventIterator = currentKeyboardEvents.iterator();

		currentKeyboardEvents.forEach(action->{
			System.out.println(action.toString());
		});
		
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

	public int getEventKeyboardKey() {
		return currentKeyboardEvent.getKeyCode();
	}

	public boolean getEventKeyboardState() {
		return currentKeyboardEvent.isState();
	}

	public char getEventKeyboardCharacter() {
		return currentKeyboardEvent.getCharacter();
	}

	public void clearNextKeyboard() {
		nextKeyboard.clear();
	}

	public boolean isKeyDown(int keycode) {
		if (keycode >= 0)
			return currentKeyboard.get(keycode).isKeyDown();

		else
			return currentMouse.get(keycode).isKeyDown();
	}

	public boolean isKeyDown(String keyname) {
		if (currentKeyboard.get(keyname).isKeyDown()) {
			return true;
		} else if (currentMouse.get(keyname).isKeyDown()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean willKeyBeDown(int keycode) {
		if (keycode >= 0)
			return nextKeyboard.get(keycode).isKeyDown();

		else
			return nextMouse.get(keycode).isKeyDown();
	}

	public boolean willKeyBeDown(String keyname) {
		if (nextKeyboard.get(keyname).isKeyDown()) {
			return true;
		} else if (nextMouse.get(keyname).isKeyDown()) {
			return true;
		} else {
			return false;
		}
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

	private List<VirtualMouseEvent> currentMouseEvents = new ArrayList<VirtualMouseEvent>();
	private Iterator<VirtualMouseEvent> currentMouseEventIterator = currentMouseEvents.iterator();

	private VirtualMouseEvent currentMouseEvent = null;

	public VirtualMouse getCurrentMouse() {
		return currentMouse;
	}

	public VirtualMouse getNextMouse() {
		return nextMouse;
	}

	public void updateNextMouse(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY, boolean filter) {

		boolean flag = true;
		if (filter) {
			flag = nextMouse.isSomethingDown() || scrollwheel != 0 || keycode != -1;
		}
		VirtualKey key = nextMouse.get(keycode - 100);

		key.setPressed(keystate);

		nextMouse.setScrollWheel(scrollwheel);

		nextMouse.setCursor(cursorX, cursorY);

		if (flag == true)
			nextMouse.addPathNode();
	}

	public List<VirtualMouseEvent> getCurrentMouseEvents() {
		return currentMouse.getDifference(nextMouse);
	}

	public void updateCurrentMouseEvents() {
		currentMouseEvents = getCurrentMouseEvents();
		currentMouseEventIterator = currentMouseEvents.iterator();

		// Prints the mouse events given to the keybindings... very useful
//		currentMouseEvents.forEach(action->{
//			System.out.println(action.toString());
//		});

		resetNextMouseLists();

		try {
			currentMouse = nextMouse.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void resetNextMouseLists() {
		nextMouse.resetPath();
	}

	public boolean nextMouseEvent() {
		boolean hasnext = currentMouseEventIterator.hasNext();
		if (hasnext) {
			currentMouseEvent = currentMouseEventIterator.next();
		}
		return hasnext;
	}

	public int getEventMouseKey() {
		return currentMouseEvent.getKeyCode();
	}

	public boolean getEventMouseState() {
		return currentMouseEvent.isState();
	}

	public int getEventMouseScrollWheel() {
		return currentMouseEvent.getScrollwheel();
	}

	public int getEventCursorX() {
		return currentMouseEvent.getMouseX();
	}

	public int getEventCursorY() {
		return currentMouseEvent.getMouseY();
	}

	public void clearNextMouse() {
		nextMouse.clear();
	}

	public List<String> getCurrentMousePresses() {
		List<String> out = new ArrayList<String>();

		currentMouse.getKeyList().forEach((keycodes, virtualkeys) -> {
			if (keycodes <= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	public List<String> getNextMousePresses() {
		List<String> out = new ArrayList<String>();

		nextMouse.getKeyList().forEach((keycodes, virtualkeys) -> {
			if (keycodes <= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	public void unpressEverything() {
		clearNextKeyboard();
		clearNextMouse();
	}
	
	// =======================================================================================
	
	VirtualSubticks currentSubtick= new VirtualSubticks(0, 0);
	
	public void updateSubtick(float pitch, float yaw) {
		currentSubtick=new VirtualSubticks(pitch, yaw);
	}
	
	public float getSubtickPitch() {
		return currentSubtick.getPitch();
	}
	
	public float getSubtickYaw() {
		return currentSubtick.getYaw();
	}
}
