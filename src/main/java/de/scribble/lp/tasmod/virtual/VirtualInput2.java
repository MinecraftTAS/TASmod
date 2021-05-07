package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.scribble.lp.tasmod.mixin.AccessorRunStuff;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import de.scribble.lp.tasmod.virtual.container.InputContainer;
import de.scribble.lp.tasmod.virtual.container.TickInputContainer;
import net.minecraft.client.Minecraft;

public class VirtualInput2 {

	private InputContainer container = new InputContainer();

	// ============================================================

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

//		System.out.println(keycode+" "+keystate+" "+character);

		if (VirtualKeybindings.isKeyCodeAlwaysBlocked(keycode)) {
			return;
		}
		VirtualKey key = nextKeyboard.get(keycode);
		key.setPressed(keystate);
		if (keystate) {
			character = nextKeyboard.encodeUnicode(keycode, character);
		} else {
			if (keycode == 15) {
				character = '\u2907';
			}
		}
		nextKeyboard.addChar(character);
	}

	public List<VirtualKeyboardEvent> getCurrentKeyboardEvents() {
		return currentKeyboard.getDifference(nextKeyboard);
	}

	public void updateCurrentKeyboardEvents() {
		currentKeyboardEvents = getCurrentKeyboardEvents();
		currentKeyboardEventIterator = currentKeyboardEvents.iterator();

//		currentKeyboardEvents.forEach(action->{
//			System.out.println(action.toString());
//		});

		nextKeyboard.clearCharList();

		currentKeyboard = nextKeyboard.clone();
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

	public VirtualMouse nextMouse = new VirtualMouse();

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

		nextMouse.setCursor(PointerNormalizer.getNormalizedX(cursorX), PointerNormalizer.getNormalizedY(cursorY));

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

		currentMouse = nextMouse.clone();
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
		return PointerNormalizer.getCoordsX(currentMouseEvent.getMouseX());
	}

	public int getEventCursorY() {
		return PointerNormalizer.getCoordsY(currentMouseEvent.getMouseY());
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

	VirtualSubticks currentSubtick = new VirtualSubticks(0, 0);

	public void updateSubtick(float pitch, float yaw) {
		currentSubtick = container.addSubticksToContainer(new VirtualSubticks(pitch, yaw));
	}

	public float getSubtickPitch() {
		return currentSubtick.getPitch();
	}

	public float getSubtickYaw() {
		return currentSubtick.getYaw();
	}

	public InputContainer getContainer() {
		return container;
	}

	public void updateContainer() {
		nextKeyboard = container.addKeyboardToContainer(nextKeyboard);
		nextMouse = container.addMouseToContainer(nextMouse);
	}

	public void setContainer(InputContainer container) {
		this.container = container;
	}

	public void loadSavestate(InputContainer container) {
		if (this.container.isPlayingback()) {
			this.container.setIndex(container.size());
			preloadInput(this.container, container.size() - 1);
		} else {
			String start = container.getStartLocation();
			preloadInput(container, container.size() - 1);
			
			container.setIndex(container.size());
			container.setRecording(true);
			container.setStartLocation(start);
			this.container = container;
		}
	}

	private void preloadInput(InputContainer container, int index) {
		TickInputContainer tickcontainer = container.get(index);

		nextKeyboard = tickcontainer.getKeyboard();
		nextMouse = tickcontainer.getMouse();
		currentSubtick = tickcontainer.getSubticks();

		((AccessorRunStuff) Minecraft.getMinecraft()).runTickKeyboardAccessor();
		((AccessorRunStuff) Minecraft.getMinecraft()).runTickMouseAccessor();

		nextKeyboard = new VirtualKeyboard();
		nextMouse = new VirtualMouse();
	}
}
