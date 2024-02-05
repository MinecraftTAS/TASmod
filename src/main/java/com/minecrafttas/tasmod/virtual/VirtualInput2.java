package com.minecrafttas.tasmod.virtual;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mixin.playbackhooks.MixinMinecraft;
import com.minecrafttas.tasmod.util.Ducks;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.PointerNormalizer;

import net.minecraft.client.gui.GuiScreen;

/**
 * Main component for redirecting inputs.<br>
 * <br>
 * This class mimics the LWJGL classes {@link org.lwjgl.input.Keyboard} and
 * {@link org.lwjgl.input.Mouse}.<br>
 * <br>
 */
public class VirtualInput2 {
	private final Logger LOGGER;
	public final VirtualKeyboardInput KEYBOARD;
	public final VirtualMouseInput MOUSE;
	public final VirtualCameraAngleInput CAMERA_ANGLE;

	public VirtualInput2(Logger logger) {
		this(logger, new VirtualKeyboard2(), new VirtualMouse2(), new VirtualCameraAngle2());
	}

	/**
	 * Creates a virtual input with pre-loaded values
	 * 
	 * @param preloadedKeyboard A keyboard loaded at the beginning
	 * @param preloadedMouse A mouse loaded at the beginning
	 * @param preloadedCamera A camera loaded at the beginning
	 */
	public VirtualInput2(Logger logger, VirtualKeyboard2 preloadedKeyboard, VirtualMouse2 preloadedMouse, VirtualCameraAngle2 preloadedCamera) {
		this.LOGGER = logger;
		KEYBOARD = new VirtualKeyboardInput(preloadedKeyboard);
		MOUSE = new VirtualMouseInput(preloadedMouse);
		CAMERA_ANGLE = new VirtualCameraAngleInput(preloadedCamera);
	}

	/**
	 * Updates the logic for {@link #KEYBOARD}, {@link #MOUSE} and
	 * {@link #CAMERA_ANGLE}<br>
	 * Runs every frame
	 * 
	 * @see MixinMinecraft#playback_injectRunGameLoop(CallbackInfo)
	 * @param currentScreen The current screen from Minecraft.class. Used for
	 *                      checking if the mouse logic should be adapted to
	 *                      GUIScreens
	 */
	public void update(GuiScreen currentScreen) {
		while (Keyboard.next()) {
			KEYBOARD.updateNextKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter(), Keyboard.areRepeatEventsEnabled());
		}
		while (Mouse.next()) {
			if (currentScreen == null) {
				MOUSE.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), 0, 0);
			} else {
				Ducks.GuiScreenDuck screen = (Ducks.GuiScreenDuck) currentScreen;
				int eventX = screen.unscaleX(Mouse.getEventX());
				int eventY = screen.unscaleY(Mouse.getEventY());
				eventX = PointerNormalizer.getNormalizedX(eventX);
				eventY = PointerNormalizer.getNormalizedY(eventY);
				MOUSE.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), eventX, eventY);
			}
		}
	}

	/**
	 * If the keyboard or mouse key is currently down.
	 * If keycode >= 0 then {@link VirtualKeyboardInput#isKeyDown(int)} will be called,<br>
	 * otherwise {@link VirtualMouseInput#isKeyDown(int)}
	 * 
	 * @param keycode The keycode in question
	 * @return If the key is down either on mouse or keyboard
	 */
	public boolean isKeyDown(int keycode) {
		if(keycode >= 0) {
			return KEYBOARD.isKeyDown(keycode);
		} else {
			return MOUSE.isKeyDown(keycode);
		}
	}
	
	/**
	 * If the keyboard or mouse key is will be down in the next tick.
	 * If keycode >= 0 then {@link VirtualKeyboardInput#willKeyBeDown(int)} will be called,<br>
	 * otherwise {@link VirtualMouseInput#willKeyBeDown(int)}
	 * 
	 * @param keycode The keycode in question
	 * @return If the key will be down either on mouse or keyboard
	 */
	public boolean willKeyBeDown(int keycode) {
		if(keycode >= 0) {
			return KEYBOARD.willKeyBeDown(keycode);
		} else {
			return MOUSE.willKeyBeDown(keycode);
		}
	}
	
	/**
	 * Unpresses all keys in {@link VirtualKeyboardInput#nextKeyboard} and {@link VirtualMouseInput#nextMouse}
	 */
	public void unpress() {
		KEYBOARD.nextKeyboard.clear();
		MOUSE.nextMouse.clear();
	}
	
	/**
	 * Subclass of {@link VirtualInput2} handling keyboard logic.<br>
	 * <br>
	 * Vanilla keyboard handling looks something like this:
	 * 
	 * <pre>
	 *	public void runTickKeyboard()  { // Executed every tick in runTick()
	 *		while({@linkplain Keyboard#next()}){
	 *			int keycode = {@linkplain Keyboard#getEventKey()};
	 *			boolean keystate = {@linkplain Keyboard#getEventKey()};
	 *			char character = {@linkplain Keyboard#getEventCharacter()}
	 *
	 *			Keybindings.updateKeybind(keycode, keystate, character)
	 *		}
	 *	}
	 * </pre>
	 * 
	 * After redirecting the calls in {@link MixinMinecraft}, the resulting logic
	 * now looks like this:
	 * 
	 * <pre>
	 *	public void runTickKeyboard()  {
	 *		{@linkplain #nextKeyboardTick()}
	 *		while({@linkplain #nextKeyboardSubtick()}){
	 *			int keycode = {@linkplain #getEventKeyboardKey()}};
	 *			boolean keystate = {@linkplain #getEventKeyboardState()};
	 *			char character = {@linkplain #getEventKeyboardCharacter()}
	 *
	 *			Keybindings.updateKeybind(keycode, keystate, character)
	 *		}
	 *	}
	 * </pre>
	 * @see com.minecrafttas.tasmod.virtual.VirtualKeyboard2
	 */
	public class VirtualKeyboardInput {
		
		/**
		 * The keyboard "state" that is currently recognized by the game,<br>
		 * meaning it is a direct copy of the vanilla keybindings. Updated every
		 * <em>tick</em>.<br>
		 * Updated in {@link #nextKeyboardTick()}
		 */
		private final VirtualKeyboard2 currentKeyboard;
		
		/**
		 * The "state" of the real physical keyboard.<br>
		 * This is updated every <em>frame</em>.<br>
		 * Updates {@link #currentKeyboard} in {@link #nextKeyboardTick()}
		 */
		private final VirtualKeyboard2 nextKeyboard = new VirtualKeyboard2();
		
		/**
		 * Queue for keyboard events.<br>
		 * Is filled in {@link #nextKeyboardTick()} and read in
		 * {@link #nextKeyboardSubtick()}
		 */
		private final Queue<VirtualKeyboardEvent> keyboardEventQueue = new ConcurrentLinkedQueue<VirtualKeyboardEvent>();
		
		/**
		 * The current keyboard event where the vanilla keybindings are reading
		 * from.<br>
		 * Updated in {@link #nextKeyboardSubtick()} and read out in
		 * {@link #getEventKeyboardKey()}, {@link #getEventKeyboardState()} and
		 * {@link #getEventKeyboardCharacter()}
		 */
		private VirtualKeyboardEvent currentKeyboardEvent = new VirtualKeyboardEvent();

		/**
		 * Constructor to preload the {@link #currentKeyboard} with an existing keyboard
		 * @param preloadedKeyboard
		 */
		public VirtualKeyboardInput(VirtualKeyboard2 preloadedKeyboard) {
			currentKeyboard = preloadedKeyboard;
		}

		/**
		 * Updates the next keyboard
		 * 
		 * @see VirtualInput2#update(GuiScreen)
		 * @param keycode   The keycode of this event
		 * @param keystate  The keystate of this event
		 * @param character The character of this event
		 */
		public void updateNextKeyboard(int keycode, boolean keystate, char character) {
			updateNextKeyboard(keycode, keystate, character, false);
		}
		
		/**
		 * Updates the next keyboard
		 * 
		 * @see VirtualInput2#update(GuiScreen)
		 * @param keycode   The keycode of this event
		 * @param keystate  The keystate of this event
		 * @param character The character of this event
		 * @param repeatEventsEnabled If repeat events are enabled
		 */
		public void updateNextKeyboard(int keycode, boolean keystate, char character, boolean repeatEventsEnabled) {
			LOGGER.debug(LoggerMarkers.Keyboard, "Update: {}, {}, {}, {}", keycode, keystate, character);
			nextKeyboard.update(keycode, keystate, character, repeatEventsEnabled);
		}
		
		/**
		 * Runs when the next keyboard tick is about to occur.<br>
		 * Used to load {@link #nextKeyboard} into {@link #currentKeyboard}, creating
		 * {@link VirtualKeyboardEvent}s in the process.
		 * 
		 * @see MixinMinecraft#playback_injectRunTickKeyboard(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
		 */
		public void nextKeyboardTick() {
			currentKeyboard.getVirtualEvents(nextKeyboard, keyboardEventQueue);
			currentKeyboard.copyFrom(nextKeyboard);
		}

		/**
		 * Runs in a while loop. Used for updating {@link #currentKeyboardEvent} and
		 * ending the while loop.
		 * 
		 * @see MixinMinecraft#playback_redirectKeyboardNext()
		 * @return If a keyboard event is in {@link #keyboardEventQueue}
		 */
		public boolean nextKeyboardSubtick() {
			return (currentKeyboardEvent = keyboardEventQueue.poll()) != null;
		}

		/**
		 * @return The keycode of {@link #currentKeyboardEvent}
		 */
		public int getEventKeyboardKey() {
			return currentKeyboardEvent.getKeyCode();
		}

		/**
		 * @return The keystate of {@link #currentKeyboardEvent}
		 */
		public boolean getEventKeyboardState() {
			return currentKeyboardEvent.isState();
		}

		/**
		 * @return The character(s) of {@link #currentKeyboardEvent}
		 */
		public char getEventKeyboardCharacter() {
			return currentKeyboardEvent.getCharacter();
		}
		
		/**
		 * If the key is currently down and recognised by Minecraft
		 * @param keycode The keycode of the key in question
		 * @return Whether the key of the {@link #currentKeyboard} is down
		 */
		public boolean isKeyDown(int keycode) {
			return currentKeyboard.isKeyDown(keycode);
		}
		
		/**
		 * If the key will be down and recognised in the next tick by Minecraft.<br>
		 * This is equal to checking if a key on the physical keyboard is pressed
		 * @param keycode The keycode of the key in question
		 * @return Whether the key of the {@link #nextKeyboard} is down
		 */
		public boolean willKeyBeDown(int keycode) {
			return nextKeyboard.isKeyDown(keycode);
		}
	}

	/**
	 * Subclass of {@link VirtualInput2} handling mouse logic.<br>
	 * <br>
	 * Vanilla mouse handling looks something like this:
	 * 
	 * <pre>
	 *	public void runTickMouse()  { // Executed every tick in runTick()
	 *		while({@linkplain Mouse#next()}){
	 *			int keycode = {@linkplain Mouse#getEventButton()};
	 *			boolean keystate = {@linkplain Mouse#getEventButtonState()};
	 *			int scrollWheel = {@linkplain Mouse#getEventDWheel()}
	 *			int cursorX = {@linkplain Mouse#getEventX()} // Important in GUIs
	 *			int cursorY = {@linkplain Mouse#getEventY()}
	 *
	 *			Keybindings.updateKeybind(keycode, keystate, etc...)
	 *		}
	 *	}
	 * </pre>
	 * 
	 * After redirecting the calls in {@link MixinMinecraft}, the resulting logic
	 * now looks like this:
	 * 
	 * <pre>
	 *	public void runTickMouse()  { // Executed every tick in runTick()
	 *		{@linkplain #nextMouseTick()}
	 *		while({@linkplain #nextMouseSubtick}){
	 *			int keycode = {@linkplain #getEventMouseKey};
	 *			boolean keystate = {@linkplain #getEventMouseState()} ()};
	 *			int scrollWheel = {@linkplain #getEventMouseScrollWheel()}
	 *			int cursorX = {@linkplain #getEventCursorX()} // Important in GUIs
	 *			int cursorY = {@linkplain #getEventCursorY()}
	 *
	 *			Keybindings.updateKeybind(keycode, keystate, etc...)
	 *		}
	 *	}
	 * </pre>
	 * @see com.minecrafttas.tasmod.virtual.VirtualMouse2
	 */
	public class VirtualMouseInput {
		private final VirtualMouse2 currentMouse;
		private final VirtualMouse2 nextMouse = new VirtualMouse2();
		private final Queue<VirtualMouseEvent> mouseEventQueue = new ConcurrentLinkedQueue<>();
		private VirtualMouseEvent currentMouseEvent = new VirtualMouseEvent();

		public VirtualMouseInput() {
			this(new VirtualMouse2());
		}

		public VirtualMouseInput(VirtualMouse2 preloadedMouse) {
			currentMouse = preloadedMouse;
		}

		public void updateNextMouse(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
			keycode-=100;
			LOGGER.debug(LoggerMarkers.Mouse,"Update: {} ({}), {}, {}, {}, {}", keycode, VirtualKey2.getName(keycode), keystate, scrollwheel, cursorX, cursorY);
			nextMouse.update(keycode, keystate, scrollwheel, cursorX, cursorY);
		}

		public void nextMouseTick() {
			currentMouse.getVirtualEvents(nextMouse, mouseEventQueue);
			currentMouse.copyFrom(nextMouse);
		}

		public boolean nextMouseSubtick() {
			return (currentMouseEvent = mouseEventQueue.poll()) != null;
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
			return PointerNormalizer.getCoordsX(currentMouseEvent.getCursorX());
		}

		public int getEventCursorY() {
			return PointerNormalizer.getCoordsY(currentMouseEvent.getCursorY());
		}

		public boolean isKeyDown(int keycode) {
			return currentMouse.isKeyDown(keycode);
		}
		
		public boolean willKeyBeDown(int keycode) {
			return nextMouse.isKeyDown(keycode);
		}
		
	}

	public class VirtualCameraAngleInput {
		private final VirtualCameraAngle2 cameraAngle;

		public VirtualCameraAngleInput(VirtualCameraAngle2 preloadedCamera) {
			cameraAngle = preloadedCamera;
		}
		
		public void updateCameraAngle(float pitch, float yaw) {
			cameraAngle.update(pitch, yaw);
		}
		
		public float getPitch() {
			return cameraAngle.getPitch();
		}
		
		public float getYaw() {
			return cameraAngle.getYaw();
		}
	}
}
