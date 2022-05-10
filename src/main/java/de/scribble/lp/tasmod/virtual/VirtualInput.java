package de.scribble.lp.tasmod.virtual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.events.OpenGuiEvents;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.inputcontainer.TickInputContainer;
import de.scribble.lp.tasmod.mixin.accessors.AccessorRunStuff;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.Minecraft;

/**
 * One of the core classes of this mod <br>
 * <br>
 * This mimics peripherals used to control minecraft which are: The keyboard,
 * the mouse and the angle of the player, which is called "Subticks" in this
 * case <i>(this came from a time when the camera angle was actually updated on
 * a subtick level)</i>.<br>
 * <br>
 * For each peripheral there are 2 states. The "current" state <i>(e.g.
 * {@linkplain #currentKeyboard})</i>, which is the state of what the game
 * actually currently recognizes and the "next" state <i>(e.g.
 * {@linkplain #nextKeyboard})</i> which either the buttons pressed on the
 * keyboard, or the buttons pressed in the next playback tick.<br>
 * <br>
 * Outside of this class, there is a third state, which is the Vanilla Minecraft
 * keybindings, which, in the best case, should be a copy of the "current"
 * state. <br>
 * <h2>Events</h2> To update the vanilla keybindings you need something called
 * key events. An event for a keyboard might look like this <br>
 * <br>
 * <b>17,true,'w'</b><br>
 * <br>
 * Something like this is sent by the LWJGL methods to the vanilla methods to
 * update the keybindings. <br>
 * In this case it is the key with the keycode 17 (The 'W' key), in the state
 * true which means it is pressed down. And the 'w' is the character that is
 * associated with that key <br>
 * <br>
 * You can find a complete list of LWJGL keycodes in
 * {@link VirtualKeyboard}.<br>
 * <br>
 * If W is released, the key event for this would look something like this:
 * <b>17, false, NULL</b> <br>
 * From that, the vanilla keybindings know which key is currently pressed down.
 * As a bonus, the character 'w' is used when typing in a textfield.<br>
 * <h2>Emulating events</h2> With the key events from LWJGL, so from the
 * "physical keyboard", we can update the nextKeyboard in the
 * {@link #updateNextKeyboard(int, boolean, char)} method.<br>
 * This method is called every frame and also works in tickrate 0.<br>
 * <br>
 * And on every tick, we call {@link #updateCurrentKeyboard()}, which updates
 * the currentKeyboard with a copy of nextKeyboard. <br>
 * However, we still need to update the Vanilla Minecraft keybinding by using
 * key events.<br>
 * To solve this problem we can use
 * {@link VirtualKeyboard#getDifference(VirtualKeyboard)}, which compares 2
 * keyboards and extracts the key events from that.<br>
 * <br>
 * For instance if we have a keyboard, where nothing is pressed, then a keyboard
 * where only "W" is pressed, we can assume that the key event responsible for
 * that change 17,true,? is. <br>
 * But as indicated by the ? we actually don't know the character that is typed
 * there. And for that we need to store the characters seperately in the
 * keyboard ({@link VirtualKeyboard#getCharList()}).<br>
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
	 * The container where all inputs get stored during recording or stored and
	 * ready to be played back
	 */
	private InputContainer container = new InputContainer();

	// ===========================Keyboard=================================

	/**
	 * The state of the keyboard recognized by the game. Updated on a tick basis
	 * <br>
	 * See also: {@link VirtualInput}
	 */
	private VirtualKeyboard currentKeyboard = new VirtualKeyboard();

	/**
	 * The state of the keyboard which will replace
	 * {@linkplain VirtualInput#currentKeyboard} in the next tick. Updated every
	 * frame<br>
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
	
	/**
	 * Loads the inputs and starts a TAS on initialize
	 * @param fileToLoad (Nullable) Loads this filename and starts playing back the TAS
	 */
	public VirtualInput(String fileToLoad) {
		if (fileToLoad != null) {
			try {
				loadInputs(fileToLoad);
				OpenGuiEvents.stateWhenOpened = TASstate.PLAYBACK;
			} catch (IOException e) {
				TASmod.logger.error("Cannot load inputs from the start of the TAS: {}", e.getMessage());
			}
		}
	}

	public void updateNextKeyboard(int keycode, boolean keystate, char character) {

//		System.out.println(keycode+" "+keystate+" "+character);

		if (keystate) {
			character = nextKeyboard.encodeUnicode(keycode, character);
		} else {
			if (keycode == 15) {
				character = '\u2907';
			}
		}
		nextKeyboard.addChar(character);
		if (VirtualKeybindings.isKeyCodeAlwaysBlocked(keycode)) {
			return;
		}
		VirtualKey key = nextKeyboard.get(keycode);
		
		key.setPressed(keystate);
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

		if (VirtualKeybindings.isKeyCodeAlwaysBlocked(keycode - 100)) {
			key.setPressed(false);
			return;
		}
		
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
		container.unpressContainer();
		unpressNext();
	}
	
	public void unpressNext() {
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
	 * Updates the input container and the {@link #nextKeyboard} as well as
	 * {@link #nextMouse}<br>
	 * Gets executed each game tick
	 */
	public void updateContainer() {
		nextKeyboard = container.addKeyboardToContainer(nextKeyboard);
		nextMouse = container.addMouseToContainer(nextMouse);
	}

	/**
	 * Replaces the {@link #container}, used in
	 * 
	 * @param container to replace the current one
	 */
	public void setContainer(InputContainer container) {
		this.container = container;
	}

	// =====================================Savestates===========================================

	/**
	 * Loads and preloads the inputs from the new InputContainer to
	 * {@link #container}
	 * 
	 * Saving a savestate is done via {@linkplain de.scribble.lp.tasmod.util.ContainerSerialiser#saveToFileV1(File, InputContainer)} in {@linkplain de.scribble.lp.tasmod.savestates.client.InputSavestatesHandler#savestate(String)}
	 * 
	 * @param savestatecontainer The container that should be loaded.
	 */
	public void loadClientSavestate(InputContainer savestatecontainer) {

		if (container.isPlayingback()) {
			preloadInput(container, savestatecontainer.size() - 1); // Preloading from the current container and
																			// from the second to last index of
																			// the savestatecontainer. Since this is
																			// executed during playback,
																			// we will only load the position of the
																			// savestate container and not replace the
																			// container itself. This is due to the fact
																			// that the playback would immediately end
																			// when you replace the container.
			
			if (container.size() >= savestatecontainer.size()) { // Check if the current container is bigger than the
																	// savestated one.

				try {
					container.setIndex(savestatecontainer.size()); // Set the "playback" index of the current
																	// container to the latest index of the
																	// savestatecontainer. Meaning this index will
																	// be played next
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			} else {
				String start = savestatecontainer.getStartLocation();
				savestatecontainer.setStartLocation("");

				try {
					savestatecontainer.setIndex(savestatecontainer.size() - 1);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				savestatecontainer.setTASState(TASstate.PLAYBACK);
				savestatecontainer.setStartLocation(start);
				container = savestatecontainer;
			}

		} else if (container.isRecording()) {
			String start = savestatecontainer.getStartLocation();
			preloadInput(savestatecontainer, savestatecontainer.size() - 1); // Preload the input of the savestate

			nextKeyboard = new VirtualKeyboard(); // Unpress the nextKeyboard and mouse to get rid of the preloaded inputs in the
													// next keyboard. Note that these inputs are still loaded in the current
													// keyboard
			nextMouse = new VirtualMouse();

			try {
				savestatecontainer.setIndex(savestatecontainer.size());
			} catch(IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			
			savestatecontainer.setTASState(TASstate.RECORDING);
			savestatecontainer.setStartLocation(start);
			container = savestatecontainer; // Replace the current container with the savestated container
		}
	}

	/**
	 * Preloads the specified index from, the container to {@link #nextMouse} and
	 * {@link #nextKeyboard}
	 * 
	 * @param container The container from which the inputs should be pre loaded
	 * @param index     The index of the container from which the inputs should be
	 *                  loaded
	 */
	private void preloadInput(InputContainer container, int index) {
		TickInputContainer tickcontainer = container.get(index);

		if (tickcontainer != null) {
			tickcontainer = tickcontainer.clone();
			nextKeyboard = tickcontainer.getKeyboard().clone();
			nextMouse = tickcontainer.getMouse().clone();

			((AccessorRunStuff) Minecraft.getMinecraft()).runTickKeyboardAccessor(); // Letting mouse and keyboard tick once to load inputs into the
																						// "currentKeyboard"
			((AccessorRunStuff) Minecraft.getMinecraft()).runTickMouseAccessor();
		} else {
			TASmod.logger.warn("Can't preload inputs, specified inputs are null!");
		}
	}
	// ================================Load/Save Inputs=====================================
	
	public void loadInputs(String filename) throws IOException {
		setContainer(ClientProxy.serialiser.fromEntireFileV1(new File(ClientProxy.tasdirectory + "/" + filename + ".tas")));
		getContainer().fixTicks();
	}
	
	public void saveInputs(String filename) throws IOException {
		ClientProxy.createTASDir();
		ClientProxy.serialiser.saveToFileV1(new File(ClientProxy.tasdirectory + "/" + filename + ".tas"), ClientProxy.virtual.getContainer());
	}

	// =====================================Debug===========================================

	public class InputEvent {
		public int tick;
		public List<VirtualKeyboardEvent> keyboardevent;
		public List<VirtualMouseEvent> mouseEvent;
		public VirtualSubticks subticks;

		public InputEvent(int tick, List<VirtualKeyboardEvent> keyboardevent, List<VirtualMouseEvent> mouseEvent, VirtualSubticks subticks) {
			this.tick = tick;
			this.keyboardevent = keyboardevent;
			this.mouseEvent = mouseEvent;
			this.subticks = subticks;
		}
	}

	/**
	 * Gets all InputEvents from the current container.<br>
	 * <br>
	 * Container and input events differ in that input events are the events that
	 * get accepted by Minecraft in the runTickKeyboard.<br>
	 * The container however stores the current inputs and can calculate the
	 * corresponding input events from that, but it does it only when you are
	 * playing back or recording.<br>
	 * <br>
	 * This however runs through the {@link VirtualInput#container} and generates
	 * the input events on for debug purposes.
	 * 
	 * @return
	 */
	public List<InputEvent> getAllInputEvents() {

		List<InputEvent> main = new ArrayList<>();

		for (int i = 0; i < container.size(); i++) {

			TickInputContainer tick = container.get(i);
			TickInputContainer nextTick = container.get(i + 1);

			if (nextTick == null) {
				nextTick = new TickInputContainer(i + 1); // Fills the last tick in the container with an empty TickInputContainer
			}

			VirtualKeyboard keyboard = tick.getKeyboard();
			List<VirtualKeyboardEvent> keyboardEventsList = keyboard.getDifference(nextTick.getKeyboard());

			VirtualMouse mouse = tick.getMouse();
			List<VirtualMouseEvent> mouseEventsList = mouse.getDifference(nextTick.getMouse());

			main.add(new InputEvent(tick.getTick(), keyboardEventsList, mouseEventsList, tick.getSubticks()));
		}
		return main;
	}
}
