package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.inputcontainer.TickInputContainer;
import de.scribble.lp.tasmod.mixin.AccessorRunStuff;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import net.minecraft.client.Minecraft;

/**
 * One of the core classes of this mod <br>
 * <br>
 * This mimics peripherals used to control minecraft which are: The keyboard, the mouse and the angle of the player, which is called "Subticks" in this case
 * <i>(this came from a time when the camera angle was actually updated on a subtick level)</i>.<br>
 * <br>
 * For each peripheral there are 2 states. The "current" state <i>(e.g. {@linkplain #currentKeyboard})</i>, which is the state of what the game actually currently recognizes and the "next" state <i>(e.g. {@linkplain #nextKeyboard})</i> which either the buttons pressed on the keyboard, or the buttons pressed in the next playback tick.<br>
 * <br>
 * Outside of this class, there is a third state, which is the Vanilla Minecraft keybindings, which, in the best case, should be a copy of the "current" state. <br>
 * <h2>Events</h2>
 * To update the vanilla keybindings you need something called key events. An event for a keyboard might look like this <br>
 * <br>
 * <b>17,true,'w'</b><br>
 * <br>
 * Something like this is sent by the LWJGL methods to the vanilla methods to update the keybindings. <br>
 * In this case it is the key with the keycode 17 (The 'W' key), in the state true which means it is pressed down. And the 'w' is the character that is associated with that key <br>
 * <br>
 * You can find a complete list of LWJGL keycodes in {@link VirtualKeyboard}.<br>
 * <br>
 * If W is released, the key event for this would look something like this: <b>17, false, NULL</b>
 * <br>
 * From that, the vanilla keybindings know which key is currently pressed down. As a bonus, the character 'w' is used when typing in a textfield.<br>
 * <h2>Emulating events</h2>
 * With the key events from LWJGL, so from the "physical keyboard", we can update the nextKeyboard in the {@link #updateNextKeyboard(int, boolean, char)} method.<br>
 * This method is called every frame and also works in tickrate 0.<br>
 * <br>
 * And on every tick, we call {@link #updateCurrentKeyboard()}, which updates the currentKeyboard with a copy of nextKeyboard. <br>
 * However, we still need to update the Vanilla Minecraft keybinding by using key events.<br>
 * To solve this problem we can use {@link VirtualKeyboard#getDifference(VirtualKeyboard)}, which compares 2 keyboards and extracts the key events from that.<br>
 * <br>
 * For instance if we have a keyboard, where nothing is pressed, then a keyboard where only "W" is pressed, we can assume that the key event responsible for that change 17,true,? is. <br>
 * But as indicated by the ? we actually don't know the character that is typed there. And for that we need to store the characters seperately in the keyboard ({@link VirtualKeyboard#getCharList()}).<br>
 * <br>
 * The advantage of this system is:
 * <ul>
 * <li>Better support for savestates</li>
 * <li>Better support for tickrate 0</li>
 * <li>Less cluttering in the resulting files</li>
 * <li>Recording support for the full keyboard/eventual modding support</li>
 * </ul>
 * 
 * @author ScribbleLP
 *
 */
public class VirtualInput {

	/**
	 * The container where all inputs get stored during recording or stored and ready to be played back
	 */
	private InputContainer container = new InputContainer();

	// ===========================Keyboard=================================

	/**
	 * The state of the keyboard recognized by the game. Updated on a tick basis <br>
	 * See also: {@link VirtualInput}
	 */
	private VirtualKeyboard currentKeyboard = new VirtualKeyboard();

	/**
	 * The state of the keyboard which will replace {@linkplain VirtualInput#currentKeyboard} in the next tick. Updated every frame<br>
	 * See also: {@link VirtualInput}
	 */
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

	public void updateCurrentKeyboard() {
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
			if (virtualkeys.isKeyDown()) {
				out.add(virtualkeys.getName());
			}
		});

		return out;
	}

	public List<String> getNextKeyboardPresses() {

		List<String> out = new ArrayList<String>();
		if (container.isPlayingback() && container.get(container.index()) != null) {
			container.get(container.index()).getKeyboard().getKeyList().forEach((keycodes, virtualkeys) -> {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			});
		} else {
			nextKeyboard.getKeyList().forEach((keycodes, virtualkeys) -> {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			});
		}
		return out;
	}

	// =======================================Mouse============================================

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
			if (virtualkeys.isKeyDown()) {
				out.add(virtualkeys.getName());
			}
		});

		return out;
	}

	public List<String> getNextMousePresses() {
		List<String> out = new ArrayList<String>();

		if (container.isPlayingback() && container.get(container.index()) != null) {
			container.get(container.index()).getMouse().getKeyList().forEach((keycodes, virtualkeys) -> {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			});
		} else {
			nextMouse.getKeyList().forEach((keycodes, virtualkeys) -> {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			});
		}

		return out;
	}

	public void unpressEverything() {
		clearNextKeyboard();
		clearNextMouse();
	}

	// ======================================Subticks===========================================

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

	// =====================================Container===========================================
	
	public InputContainer getContainer() {
		return container;
	}

	/**
	 * Updates the input container and the {@link #nextKeyboard} as well as {@link #nextMouse}<br>
	 * Gets executed each game tick
	 */
	public void updateContainer() {
		nextKeyboard = container.addKeyboardToContainer(nextKeyboard);
		nextMouse = container.addMouseToContainer(nextMouse);
	}

	/**
	 * Replaces the {@link #container}, used in 
	 * @param container to replace the current one
	 */
	public void setContainer(InputContainer container) {
		this.container = container;
	}

	// =====================================Savestates===========================================
	
	public void loadSavestate(InputContainer container) {
		if (this.container.isPlayingback()) {
			preloadInput(this.container, container.size() - 1);
			this.container.setIndex(container.size());

		} else if (this.container.isRecording()) {
			String start = container.getStartLocation();
			preloadInput(container, container.size() - 1);

			nextKeyboard = new VirtualKeyboard();
			nextMouse = new VirtualMouse();

			container.setIndex(container.size());
			container.setRecording(true);
			container.setStartLocation(start);
			this.container = container;
		}
	}

	private void preloadInput(InputContainer container, int index) {
		TickInputContainer tickcontainer = container.get(index).clone();

		nextKeyboard = tickcontainer.getKeyboard().clone();
		nextMouse = tickcontainer.getMouse().clone();

		((AccessorRunStuff) Minecraft.getMinecraft()).runTickKeyboardAccessor();
		((AccessorRunStuff) Minecraft.getMinecraft()).runTickMouseAccessor();

	}
}
