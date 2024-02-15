package com.minecrafttas.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mixin.playbackhooks.MixinMinecraft;
import com.minecrafttas.tasmod.util.Ducks;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.PointerNormalizer;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;

/**
 * Main component for redirecting inputs.<br>
 * <br>
 * This class mimics the LWJGL classes {@link org.lwjgl.input.Keyboard} and
 * {@link org.lwjgl.input.Mouse}.<br>
 * <br>
 */
public class VirtualInput {
	private final Logger LOGGER;
	/**
	 * Instance of the {@link VirtualKeyboardInput} subclass, intended to improve readability.
	 */
	public final VirtualKeyboardInput KEYBOARD;
	/**
	 * Instance of the {@link VirtualMouseInput} subclass, intended to improve readability.
	 */
	public final VirtualMouseInput MOUSE;
	/**
	 * Instance of the {@link VirtualCameraAngleInput} subclass, intended to improve readability.
	 */
	public final VirtualCameraAngleInput CAMERA_ANGLE;

	/**
	 * Creates a new virtual input with an empty {@link VirtualKeyboardInput}, {@link VirtualMouseInput} and {@link VirtualCameraAngleInput}
	 * @param logger
	 */
	public VirtualInput(Logger logger) {
		this(logger, new VirtualKeyboard(), new VirtualMouse(), new VirtualCameraAngle());
	}

	/**
	 * Creates a virtual input with pre-loaded values
	 * 
	 * @param preloadedKeyboard A keyboard loaded when creating {@link VirtualKeyboardInput}
	 * @param preloadedMouse A mouse loaded when creating {@link VirtualMouseInput}
	 * @param preloadedCamera A camera loaded when creating {@link VirtualCameraAngleInput}
	 */
	public VirtualInput(Logger logger, VirtualKeyboard preloadedKeyboard, VirtualMouse preloadedMouse, VirtualCameraAngle preloadedCamera) {
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
	public void clear() {
		KEYBOARD.nextKeyboard.clear();
		MOUSE.nextMouse.clear();
		CAMERA_ANGLE.nextCameraAngle.clear();
	}
	
	/**
	 * Subclass of {@link VirtualInput} handling keyboard logic.<br>
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
	 * @see VirtualKeyboard
	 */
	public class VirtualKeyboardInput {
		/**
		 * The keyboard "state" that is currently recognized by the game,<br>
		 * meaning it is a direct copy of the vanilla keybindings. Updated every
		 * <em>tick</em>.<br>
		 * Updated in {@link #nextKeyboardTick()}
		 */
		private final VirtualKeyboard currentKeyboard;
		/**
		 * The "state" of the real physical keyboard.<br>
		 * This is updated every <em>frame</em>.<br>
		 * Updates {@link #currentKeyboard} in {@link #nextKeyboardTick()}
		 */
		private final VirtualKeyboard nextKeyboard = new VirtualKeyboard();
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
		 * <ul>
		 * 	<li>{@link #getEventKeyboardKey()}</li>
		 * 	<li>{@link #getEventKeyboardState()}</li>
		 * 	<li>{@link #getEventKeyboardCharacter()}</li>
		 * </ul>
		 */
		private VirtualKeyboardEvent currentKeyboardEvent = new VirtualKeyboardEvent();

		/**
		 * Constructor to preload the {@link #currentKeyboard} with an existing keyboard
		 * @param preloadedKeyboard The new {@link #currentKeyboard}
		 */
		public VirtualKeyboardInput(VirtualKeyboard preloadedKeyboard) {
			currentKeyboard = preloadedKeyboard;
		}

		/**
		 * Updates the next keyboard
		 * 
		 * @see VirtualInput#update(GuiScreen)
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
		 * @see VirtualInput#update(GuiScreen)
		 * @param keycode   The keycode of this event
		 * @param keystate  The keystate of this event
		 * @param character The character of this event
		 * @param repeatEventsEnabled If repeat events are enabled
		 */
		public void updateNextKeyboard(int keycode, boolean keystate, char character, boolean repeatEventsEnabled) {
			LOGGER.debug(LoggerMarkers.Keyboard, "Update: {}, {}, {}, {}", keycode, keystate, character); 	// Activate with -Dtasmod.marker.keyboard=ACCEPT in VM arguments (and -Dtasmod.log.level=debug)
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
	 * Subclass of {@link VirtualInput} handling mouse logic.<br>
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
	 * @see VirtualMouse
	 */
	public class VirtualMouseInput {
		/**
		 * The mouse "state" that is currently recognized by the game,<br>
		 * meaning it is a direct copy of the vanilla mouse. Updated every
		 * <em>tick</em>.<br>
		 * Updated in {@link #nextMouseTick()}
		 */
		private final VirtualMouse currentMouse;
		/**
		 * The "state" of the real physical mouse.<br>
		 * This is updated every <em>frame</em>.<br>
		 * Updates {@link #currentMouse} in {@link #nextMouseTick()}
		 */
		private final VirtualMouse nextMouse = new VirtualMouse();
		/**
		 * Queue for keyboard events.<br>
		 * Is filled in {@link #nextMouseTick()} and read in
		 * {@link #nextMouseSubtick()}
		 */
		private final Queue<VirtualMouseEvent> mouseEventQueue = new ConcurrentLinkedQueue<>();
		/**
		 * The current mouse event where the vanilla mouse is reading
		 * from.<br>
		 * Updated in {@link #nextMouseSubtick()} and read out in
		 * <ul>
		 * 	<li>{@link #getEventMouseKey()}</li>
		 * 	<li>{@link #getEventMouseState()}</li>
		 * 	<li>{@link #getEventMouseScrollWheel()}</li>
		 * 	<li>{@link #getEventCursorX()}</li>
		 * 	<li>{@link #getEventCursorY()}</li>
		 * </ul>
		 */
		private VirtualMouseEvent currentMouseEvent = new VirtualMouseEvent();

		/**
		 * Constructor to preload the {@link #currentMouse} with an existing mouse
		 * @param preloadedMouse The new {@link #currentMouse}
		 */
		public VirtualMouseInput(VirtualMouse preloadedMouse) {
			currentMouse = preloadedMouse;
		}

		/**
		 * Updates the next keyboard
		 * 
		 * @see VirtualInput#update(GuiScreen)
		 * @param keycode   The keycode of this event
		 * @param keystate  The keystate of this event
		 * @param scrollwheel The scrollwheel direction of this event
		 * @param cursorX The x coordinate of the cursor of this event
		 * @param cursorY The y coordinate of the cursot of this event
		 */
		public void updateNextMouse(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
			keycode-=100;
			LOGGER.debug(LoggerMarkers.Mouse,"Update: {} ({}), {}, {}, {}, {}", keycode, VirtualKey.getName(keycode), keystate, scrollwheel, cursorX, cursorY); 	// Activate with -Dtasmod.marker.mouse=ACCEPT in VM arguments (and -Dtasmod.log.level=debug)
			nextMouse.update(keycode, keystate, scrollwheel, cursorX, cursorY);
		}

		/**
		 * Runs when the next mouse tick is about to occur.<br>
		 * Used to load {@link #nextMouse} into {@link #currentMouse}, creating
		 * {@link VirtualMouseEvent}s in the process.
		 * 
		 * @see MixinMinecraft#playback_injectRunTickMouse(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
		 */
		public void nextMouseTick() {
			currentMouse.getVirtualEvents(nextMouse, mouseEventQueue);
			currentMouse.copyFrom(nextMouse);
		}

		/**
		 * Runs in a while loop. Used for updating {@link #currentMouseEvent} and
		 * ending the while loop.
		 * 
		 * @see MixinMinecraft#playback_redirectMouseNext()
		 * @return If a mouse event is in {@link #mouseEventQueue}
		 */
		public boolean nextMouseSubtick() {
			return (currentMouseEvent = mouseEventQueue.poll()) != null;
		}

		/**
		 * @return The keycode of {@link #currentMouseEvent}
		 */
		public int getEventMouseKey() {
			return currentMouseEvent.getKeyCode();
		}

		/**
		 * @return The keystate of {@link #currentMouseEvent}
		 */
		public boolean getEventMouseState() {
			return currentMouseEvent.isState();
		}

		/**
		 * @return The scroll wheel of {@link #currentMouseEvent}
		 */
		public int getEventMouseScrollWheel() {
			return currentMouseEvent.getScrollwheel();
		}

		/**
		 * @return The x coordinate of the cursor of {@link #currentMouseEvent}
		 */
		public int getEventCursorX() {
			return PointerNormalizer.reapplyScalingX(currentMouseEvent.getCursorX());
		}

		/**
		 * @return The y coordinate of the cursor of {@link #currentMouseEvent}
		 */
		public int getEventCursorY() {
			return PointerNormalizer.reapplyScalingY(currentMouseEvent.getCursorY());
		}

		/**
		 * If the key is currently down and recognised by Minecraft
		 * @param keycode The keycode of the key in question
		 * @return Whether the key of the {@link #currentMouse} is down
		 */
		public boolean isKeyDown(int keycode) {
			return currentMouse.isKeyDown(keycode);
		}
		
		/**
		 * If the key will be down and recognised in the next tick by Minecraft.<br>
		 * This is equal to checking if a key on the physical mouse is pressed
		 * @param keycode The keycode of the key in question
		 * @return Whether the key of the {@link #nextMouse} is down
		 */
		public boolean willKeyBeDown(int keycode) {
			return nextMouse.isKeyDown(keycode);
		}
		
	}

	/**
	 * Subclass of {@link VirtualInput} handling camera angle logic.<br>
	 * <br>
	 * Unlike {@link VirtualKeyboardInput} or {@link VirtualMouseInput} no subtick behaviour is implemented,<br>
	 * making this a simple pitch and yaw storing class, allowing for redirection.
	 * <br>
	 * In theory, subtick behaviour is possible, but only useful for interpolation,<br>
	 * as the camera angle is only updated every tick (see {@link com.minecrafttas.tasmod.mixin.playbackhooks.MixinEntityRenderer}).
	 */
	public class VirtualCameraAngleInput {
		/**
		 * The current camera angle
		 */
		private final VirtualCameraAngle currentCameraAngle;
		private final VirtualCameraAngle nextCameraAngle = new VirtualCameraAngle();
		private final List<VirtualCameraAngle> cameraAngleInterpolationStates = new ArrayList<>();
        
		/**
		 * Constructor to preload the {@link #currentCameraAngle} with an existing camera angle
		 * @param preloadedCamera The new {@link #currentCameraAngle}
		 */
		public VirtualCameraAngleInput(VirtualCameraAngle preloadedCamera) {
			currentCameraAngle = preloadedCamera;
		}

		/**
		 * Update the camera angle
		 * @see com.minecrafttas.tasmod.mixin.playbackhooks.MixinEntityRenderer#runUpdate(float);
		 * @param pitch Absolute rotationPitch of the player
		 * @param yaw Absolute rotationYaw of the player
		 */
		public void updateNextCameraAngle(float pitch, float yaw) {
//			LOGGER.debug("Pitch: {}, Yaw: {}", pitch, yaw);
			nextCameraAngle.update(pitch, yaw);
		}
		
		public void nextCameraTick() {
			nextCameraAngle.getStates(cameraAngleInterpolationStates);
			currentCameraAngle.copyFrom(nextCameraAngle);
		}
		
		public void setCamera(Float pitch, Float yaw) {
			nextCameraAngle.set(pitch, yaw);
		}
		
		public Float getCurrentPitch() {
			return currentCameraAngle.getPitch();
		}
		
		public Float getCurrentYaw() {
			return currentCameraAngle.getYaw();
		}
		
		public Triple<Float, Float, Float> getInterpolatedState(float partialTick, float pitch, float yaw, boolean enable){
			if(!enable) {
				return Triple.of(nextCameraAngle.getPitch()==null ? pitch : nextCameraAngle.getPitch(), nextCameraAngle.getYaw()==null? pitch : nextCameraAngle.getYaw()+180, 0f);
			}
			
			float interpolatedPitch = 0f;
			float interpolatedYaw = 0f;
			
			if(cameraAngleInterpolationStates.size()==1) { // If no interpolation data was specified, interpolate over 2 values
				interpolatedPitch = (float) MathHelper.clampedLerp(currentCameraAngle.getPitch(), cameraAngleInterpolationStates.get(0).getPitch(), partialTick);
				interpolatedYaw = (float) MathHelper.clampedLerp(currentCameraAngle.getYaw(), cameraAngleInterpolationStates.get(0).getYaw() + 180, partialTick);
			} else {
				
				int index = (int)MathHelper.clampedLerp(0, cameraAngleInterpolationStates.size(), partialTick);	// Get interpolate index 
				
				interpolatedPitch = cameraAngleInterpolationStates.get(index).getPitch();
				interpolatedYaw = cameraAngleInterpolationStates.get(index).getYaw();
			}
			
			return Triple.of(interpolatedPitch, interpolatedYaw, 0f);
		}
	}
}
