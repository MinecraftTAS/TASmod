package com.minecrafttas.tasmod.virtual;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.events.EventVirtualInput;
import com.minecrafttas.tasmod.mixin.playbackhooks.MixinEntityRenderer;
import com.minecrafttas.tasmod.util.Ducks;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.PointerNormalizer;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Main component for redirecting inputs.<br>
 * <br>
 * This class mimics the LWJGL classes {@link org.lwjgl.glfw.GLFW} and
 * {@link org.lwjgl.glfw.GLFW} and redirects the camera angle and player rotation<br>
 *
 * @author Scribble
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

    public final VirtualInterpolationHandler interpolationHandler = new VirtualInterpolationHandler();

    private boolean useVanillaIsKeyDown;

    /**
     * Creates a new virtual input with an empty {@link VirtualKeyboardInput}, {@link VirtualMouseInput} and {@link VirtualCameraAngleInput}
     * @param logger The logger instance
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
    public void update(Screen currentScreen) {
        // TODO: Rewrite for LWJGL3/GLFW input system
        // The old LWJGL2 Keyboard and Mouse classes are not available in Minecraft 1.17+
        // Input events are now handled through GLFW callbacks in KeyboardHandler and MouseHandler
        
        // Stub implementation - actual input handling needs to be rewritten
        // using the new input system (GLFW, KeyboardHandler, MouseHandler)
    }

    /**
     * Update the {@link SubtickScreen#keyTyped(char, int)}, if the current screen is a {@link SubtickScreen}
     * @param currentScreen The current screen to update
     * @return True if the next keyboard should not be updated
     */
    private boolean updateSubtickScreenKeyboard(Screen currentScreen) {
        // TODO: Rewrite for new input system
        return false;
    }

    /**
     * Update the {@link SubtickScreen#mouseClicked(int, int, int)}, if the current screen is a {@link SubtickScreen}
     * @param currentScreen The current screen to update
     * @return True if the next mouse should not be updated
     */
    private boolean updateSubtickScreenMouse(Screen currentScreen) {
        // TODO: Rewrite for new input system
        return false;
    }

    public void setUseVanillaIsKeyDown(boolean isVanilla) {
        this.useVanillaIsKeyDown = isVanilla;
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
        if (keycode >= 0) {
            if (!useVanillaIsKeyDown)
                return KEYBOARD.isKeyDown(keycode);
            else {
                long windowHandle = 0;
                try {
                    windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getMethod("getWindow").invoke(Minecraft.getInstance().getWindow());
                } catch (Exception e) {
                    try {
                        windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getField("window").get(Minecraft.getInstance().getWindow());
                    } catch (Exception ignored) {}
                }
                return windowHandle != 0 && GLFW.glfwGetKey(windowHandle, keycode) == GLFW.GLFW_PRESS;
            }
        } else {
            if (!useVanillaIsKeyDown)
                return MOUSE.isKeyDown(keycode);
            else {
                long windowHandle = 0;
                try {
                    windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getMethod("getWindow").invoke(Minecraft.getInstance().getWindow());
                } catch (Exception e) {
                    try {
                        windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getField("window").get(Minecraft.getInstance().getWindow());
                    } catch (Exception ignored) {}
                }
                return windowHandle != 0 && GLFW.glfwGetMouseButton(windowHandle, -keycode - 1) == GLFW.GLFW_PRESS;
            }
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
        if (keycode >= 0) {
            if (!useVanillaIsKeyDown)
                return KEYBOARD.willKeyBeDown(keycode);
            else {
                long windowHandle = 0;
                try {
                    windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getMethod("getWindow").invoke(Minecraft.getInstance().getWindow());
                } catch (Exception e) {
                    try {
                        windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getField("window").get(Minecraft.getInstance().getWindow());
                    } catch (Exception ignored) {}
                }
                return windowHandle != 0 && GLFW.glfwGetKey(windowHandle, keycode) == GLFW.GLFW_PRESS;
            }
        } else {
            if (!useVanillaIsKeyDown)
                return MOUSE.willKeyBeDown(keycode);
            else {
                long windowHandle = 0;
                try {
                    windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getMethod("getWindow").invoke(Minecraft.getInstance().getWindow());
                } catch (Exception e) {
                    try {
                        windowHandle = (long) Minecraft.getInstance().getWindow().getClass().getField("window").get(Minecraft.getInstance().getWindow());
                    } catch (Exception ignored) {}
                }
                return windowHandle != 0 && GLFW.glfwGetMouseButton(windowHandle, -keycode - 1) == GLFW.GLFW_PRESS;
            }
        }
    }

    /**
     * Unpresses all keys in {@link VirtualKeyboardInput#nextKeyboard} and {@link VirtualMouseInput#nextMouse}
     */
    public void clearNext() {
        KEYBOARD.clearNext();
        MOUSE.clearNext();
    }

    /**
     * Unpresses all keys in {@link VirtualKeyboardInput#currentKeyboard} and {@link VirtualMouseInput#currentMouse}
     */
    public void clearCurrent() {
        KEYBOARD.clearCurrent();
        MOUSE.clearCurrent();
    }

    public void preloadInput(VirtualKeyboard keyboardToPreload, VirtualMouse mouseToPreload, VirtualCameraAngle angleToPreload) {
        // Preload the nextKeyboard
        KEYBOARD.nextKeyboard.deepCopyFrom(keyboardToPreload);
        MOUSE.nextMouse.deepCopyFrom(mouseToPreload);
        CAMERA_ANGLE.nextCameraAngle.deepCopyFrom(angleToPreload);

        // Preload the currentKeyboard
        KEYBOARD.nextKeyboardTick();
        MOUSE.nextMouseTick();

        // Preload vanilla inputs
        // Minecraft.getMinecraft().runTickKeyboard(); // Letting mouse and keyboard tick once to load inputs into the "currentKeyboard"
        // Minecraft.getMinecraft().runTickMouse();

        SubtickDuck entityRenderer = null;
        try {
            entityRenderer = (SubtickDuck) Minecraft.getInstance().getClass().getField("entityRenderer").get(Minecraft.getInstance());
        } catch (Exception e) {
            try {
                entityRenderer = (SubtickDuck) Minecraft.getInstance().getClass().getMethod("getEntityRenderer").invoke(Minecraft.getInstance());
            } catch (Exception ignored) {}
        }
        if (entityRenderer != null) {
            entityRenderer.runUpdate(0);
        }
    }

    public List<String> getCurrentMousePresses() {
        return MOUSE.currentMouse.getCurrentPresses();
    }

    public List<String> getNextMousePresses() {
        return MOUSE.nextMouse.getCurrentPresses();
    }

    public List<String> getCurrentKeyboardPresses() {
        return KEYBOARD.currentKeyboard.getCurrentPresses();
    }

    public List<String> getNextKeyboardPresses() {
        return KEYBOARD.nextKeyboard.getCurrentPresses();
    }

    /**
     * Subclass of {@link VirtualInput} handling keyboard logic.<br>
     * <br>
     * Vanilla keyboard handling looks something like this:
     * 
     * <pre>
     * 	public void runTickKeyboard()  { // Executed every tick in runTick()
     * 		while({@linkplain Keyboard#next()}) {
     * 			int keycode = {@linkplain Keyboard#getEventKey()};
     * 			boolean keystate = {@linkplain Keyboard#getEventKey()};
     * 			char character = {@linkplain Keyboard#getEventCharacter()}
     *
     * 			Keybindings.updateKeybind(keycode, keystate, character)
     * 		}
     * 	}
     * </pre>
     * 
     * After redirecting the calls in {@link MixinMinecraft}, the resulting logic
     * now looks like this:
     * 
     * <pre>
     * 	public void runTickKeyboard()  {
     * 		{@linkplain #nextKeyboardTick()}
     * 		while({@linkplain #nextKeyboardSubtick()}) {
     * 			int keycode = {@linkplain #getEventKeyboardKey()}};
     * 			boolean keystate = {@linkplain #getEventKeyboardState()};
     * 			char character = {@linkplain #getEventKeyboardCharacter()}
     *
     * 			Keybindings.updateKeybind(keycode, keystate, character)
     * 		}
     * 	}
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
         * @see VirtualInput#update(Screen)
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
         * @see VirtualInput#update(Screen)
         * @param keycode   The keycode of this event
         * @param keystate  The keystate of this event
         * @param character The character of this event
         * @param repeatEventsEnabled If repeat events are enabled
         */
        public void updateNextKeyboard(int keycode, boolean keystate, char character, boolean repeatEventsEnabled) {
            LOGGER.debug(LoggerMarkers.Keyboard, "Update: {}, {}, {}", keycode, keystate, character); // Activate with -Dtasmod.marker.keyboard=ACCEPT in VM arguments (and -Dtasmod.log.level=debug)
            nextKeyboard.updateFromEvent(keycode, keystate, character, repeatEventsEnabled);
        }

        /**
         * Runs when the next keyboard tick is about to occur.<br>
         * Used to load {@link #nextKeyboard} into {@link #currentKeyboard}, creating
         * {@link VirtualKeyboardEvent}s in the process.
         * 
         * @see MixinMinecraft#playback_injectRunTick(CallbackInfo)
         */
        public void nextKeyboardTick() {
            nextKeyboard.deepCopyFrom((VirtualKeyboard) EventListenerRegistry.fireEvent(EventVirtualInput.EventVirtualKeyboardTick.class, nextKeyboard));
            currentKeyboard.getVirtualEvents(nextKeyboard, keyboardEventQueue);
            currentKeyboard.moveFrom(nextKeyboard);
            LOGGER.debug(LoggerMarkers.Keyboard, "KeyboardTick: {}", currentKeyboard); // Activate with -Dtasmod.marker.keyboard=ACCEPT in VM arguments (and -Dtasmod.log.level=debug)
        }

        /**
         * Runs in a while loop. Used for updating {@link #currentKeyboardEvent} and
         * ending the while loop.
         * 
         * @see MixinMinecraft#playback_redirectKeyboardNext()
         * @return If a keyboard event is in {@link #keyboardEventQueue}
         */
        public boolean nextKeyboardSubtick() {
            VirtualKeyboardEvent newKeyboardEvent = keyboardEventQueue.poll();
            boolean isPolled = newKeyboardEvent != null;
            if (isPolled) {
                currentKeyboardEvent = newKeyboardEvent;
            }
            EventListenerRegistry.fireEvent(EventVirtualInput.EventVirtualKeyboardSubtick.class, currentKeyboardEvent);
            return isPolled;
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

        /**
         * Clears the {@link #nextKeyboard}
         */
        public void clearNext() {
            nextKeyboard.clear();
        }

        /**
         * Clears the {@link #currentKeyboard}
         */
        public void clearCurrent() {
            currentKeyboard.clear();
        }
    }

    /**
     * Subclass of {@link VirtualInput} handling mouse logic.<br>
     * <br>
     * Vanilla mouse handling looks something like this:
     * 
     * <pre>
     * 	public void runTickMouse()  { // Executed every tick in runTick()
     * 		while({@linkplain Mouse#next()}) {
     * 			int keycode = {@linkplain Mouse#getEventButton()};
     * 			boolean keystate = {@linkplain Mouse#getEventButtonState()};
     * 			int scrollWheel = {@linkplain Mouse#getEventDWheel()}
     * 			int cursorX = {@linkplain Mouse#getEventX()} // Important in GUIs
     * 			int cursorY = {@linkplain Mouse#getEventY()}
     *
     * 			Keybindings.updateKeybind(keycode, keystate, etc...)
     * 		}
     * 	}
     * </pre>
     * 
     * After redirecting the calls in {@link MixinMinecraft}, the resulting logic
     * now looks like this:
     * 
     * <pre>
     * 	public void runTickMouse()  { // Executed every tick in runTick()
     * 		{@linkplain #nextMouseTick()}
     * 		while({@linkplain #nextMouseSubtick}) {
     * 			int keycode = {@linkplain #getEventMouseKey};
     * 			boolean keystate = {@linkplain #getEventMouseState()} ()};
     * 			int scrollWheel = {@linkplain #getEventMouseScrollWheel()}
     * 			int cursorX = {@linkplain #getEventCursorX()} // Important in GUIs
     * 			int cursorY = {@linkplain #getEventCursorY()}
     *
     * 			Keybindings.updateKeybind(keycode, keystate, etc...)
     * 		}
     * 	}
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
         * @see VirtualInput#update(Screen)
         * @param keycode   The keycode of this event
         * @param keystate  The keystate of this event
         * @param scrollwheel The scrollwheel direction of this event
         * @param cursorX The x coordinate of the cursor of this event
         * @param cursorY The y coordinate of the cursot of this event
         */
        public void updateNextMouse(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
            LOGGER.debug(LoggerMarkers.Mouse, "Update: {} ({}), {}, {}, {}, {}", keycode, VirtualKey.getName(keycode), keystate, scrollwheel, cursorX, cursorY); // Activate with -Dtasmod.marker.mouse=ACCEPT in VM arguments (and -Dtasmod.log.level=debug)
            nextMouse.updateFromEvent(keycode, keystate, scrollwheel, cursorX, cursorY);
        }

        /**
         * Runs when the next mouse tick is about to occur.<br>
         * Used to load {@link #nextMouse} into {@link #currentMouse}, creating
         * {@link VirtualMouseEvent}s in the process.
         * 
         * @see MixinMinecraft#playback_injectRunTick(CallbackInfo)
         */
        public void nextMouseTick() {
            nextMouse.deepCopyFrom((VirtualMouse) EventListenerRegistry.fireEvent(EventVirtualInput.EventVirtualMouseTick.class, nextMouse));
            currentMouse.getVirtualEvents(nextMouse, mouseEventQueue);
            currentMouse.moveFrom(nextMouse);
        }

        /**
         * Runs in a while loop. Used for updating {@link #currentMouseEvent} and
         * ending the while loop.
         * 
         * @see MixinMinecraft#playback_redirectMouseNext()
         * @return If a mouse event is in {@link #mouseEventQueue}
         */
        public boolean nextMouseSubtick() {
            VirtualMouseEvent newMouseEvent = mouseEventQueue.poll();
            boolean isPolled = newMouseEvent != null;
            if (isPolled) {
                currentMouseEvent = newMouseEvent;
            }
            EventListenerRegistry.fireEvent(EventVirtualInput.EventVirtualMouseSubtick.class, currentMouseEvent);
            return isPolled;
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
         * @return The scaled x coordinate of the cursor of {@link #currentMouseEvent}
         */
        public int getEventCursorX() {
            return PointerNormalizer.reapplyScalingX(getNormalizedCursorX());
        }

        /**
         * @return The x coordinate of the cursor of {@link #currentMouseEvent}
         */
        public int getNormalizedCursorX() {
            return currentMouseEvent.getCursorX();
        }

        /**
         * @return The scaled y coordinate of the cursor of {@link #currentMouseEvent}
         */
        public int getEventCursorY() {
            return PointerNormalizer.reapplyScalingY(getNormalizedCursorY());
        }

        /**
         * @return The y coordinate of the cursor of {@link #currentMouseEvent}
         */
        public int getNormalizedCursorY() {
            return currentMouseEvent.getCursorY();
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
         * @return Whether the key of the {@link #nextMouse} is down3
         */
        public boolean willKeyBeDown(int keycode) {
            return nextMouse.isKeyDown(keycode);
        }

        /**
         * Clears the {@link #nextMouse}
         */
        public void clearNext() {
            nextMouse.clear();
        }

        /**
         * Clears the {@link #currentMouse}
         */
        public void clearCurrent() {
            currentMouse.clear();
        }
    }

    /**
     * Subclass of {@link VirtualInput} handling camera angle logic.<br>
     * <br>
     * Normally, the camera angle is updated every <em>frame</em>.<br>
     * This meant that inputs on mouse and keyboard, updated every <em>tick</em>,<br>
     * had to sync with the camera angle, updated every frame, which desynced in some edgecases.<br>
     * <br>
     * After extensive testing, the decision was made to conform the camera to update every <em>tick</em>,<br>
     * instead of every frame. By itself, this meant that moving the camera felt really laggy<br>
     * <br>
     * Therefore, an interpolation system was created that seperated the camera angle from the player head rotation,<br>
     * which are usually the synced.
     * <br>
     * While the camera is the angle displayed on screen, the player rotation is responsible for the logic,<br>
     * e.g. at which block the player is currently aiming.<br>
     * This calls for a similar architecture as the {@link VirtualKeyboardInput} and the {@link VirtualMouseInput}.
     * <br>
     * The {@link VirtualCameraAngleInput#currentCameraAngle} represents the player rotation, updated every tick<br>
     * and the {@link VirtualCameraAngleInput#nextCameraAngle} represents the camera angle, updated every frame.
     * <br>
     * In pseudocode, the tick conformed camera looks like this:
     * <pre>
     * public void runGameLoop() {// The main update loop, every frame
     * 	for(int i;i<timer.toTickCount;i++){ // For loop to enable ticking
     * 		entityRenderer.updatePlayerRotation();
     * 		runTick()
     * 	}
     * 
     * 	entityRenderer.updateCamera();	// Update the camera
     * }
     * </pre>
     * Note that the camera is updated <em>after</em> the tick function.
     * Now in this pseudocode, the following methods from this class are injected like so:
     * <pre>
     * public void runGameLoop() {// The main update loop, every frame
     * 	for(int i;i<timer.toTickCount;i++){ // For loop to enable ticking
     * 		{@linkplain VirtualCameraAngleInput#nextCameraTick()};
     * 		entityRenderer.updatePlayerRotation({@linkplain VirtualCameraAngleInput#getCurrentPitch()}, {@linkplain VirtualCameraAngleInput#getCurrentYaw()});
     * 		runTick()
     * 	}
     *  {@linkplain VirtualCameraAngleInput#updateNextCameraAngle(float, float)};
     * 	entityRenderer.updateCamera();	// Update the camera
     * }
     * </pre>
     * 
     * While there is "subtick" behavior in this implementation,<br>
     * it is only used for the interpolation of the camera angle and not for the player rotation.<br>
     * This way you can customize the interpolated frames during playback.
     */
    public class VirtualCameraAngleInput {
        /**
         * The current camera angle in game.<br>
         * <br>
         * Updated every tick in {@link #nextCameraTick()}
         */
        private final VirtualCameraAngle currentCameraAngle;
        /**
         * The new camera angle<br>
         * <br>
         * updated every frame in {@link #updateNextCameraAngle(float, float)}<br>
         * and updates {@link #currentCameraAngle} in {@link #nextCameraTick()}
         */
        private final VirtualCameraAngle nextCameraAngle = new VirtualCameraAngle();

        /**
         * Constructor to preload the {@link #currentCameraAngle} with an existing
         * camera angle
         * 
         * @param preloadedCamera The new {@link #currentCameraAngle}
         */
        public VirtualCameraAngleInput(VirtualCameraAngle preloadedCamera) {
            currentCameraAngle = preloadedCamera;
        }

        /**
         * Update the camera angle.<br>
         * <br>
         * Runs every frame
         * 
         * @see com.minecrafttas.tasmod.mixin.playbackhooks.MixinEntityRenderer#runUpdate(float);
         * @param pitchDelta Relative rotationPitch delta from LWJGLs mouse delta.
         * @param yawDelta   Relative rotationYaw delta from LWJGLs mouse delta.
         */
        public void updateNextCameraAngle(float pitchDelta, float yawDelta) {
            updateNextCameraAngle(pitchDelta, yawDelta, true);
        }

        /**
         * Update the camera angle.<br>
         * <br>
         * Runs every frame
         * 
         * @param pitchDelta Relative rotationPitch delta from LWJGLs mouse delta.
         * @param yawDelta Relative rotationYaw delta from LWJGLs mouse delta.
         * @param updateSubtick Whether to add the previous camera angle to the {@link Subtickable#subtickList}
         * @see MixinEntityRenderer#runUpdate(float)
         */
        public void updateNextCameraAngle(float pitchDelta, float yawDelta, boolean updateSubtick) {
            //			LOGGER.debug("Pitch: {}, Yaw: {}", pitch, yaw);
            nextCameraAngle.updateFromEvent(pitchDelta, yawDelta, updateSubtick);
        }

        /**
         * Updates the {@link #currentCameraAngle} and {@link #cameraAngleInterpolationStates}.<br>
         * Runs every tick.
         * 
         * @see MixinEntityRenderer#runUpdate(float)
         */
        public void nextCameraTick() {
            nextCameraAngle.deepCopyFrom((VirtualCameraAngle) EventListenerRegistry.fireEvent(EventVirtualInput.EventVirtualCameraAngleTick.class, nextCameraAngle));
            currentCameraAngle.moveFrom(nextCameraAngle);
        }

        /**
         * Sets the camera coordinates directly.<br>
         * <br>
         * The camera angle is stored in absolute coordinates,<br>
         * which should match the vanilla coordinates displayed in F3<br>
         * <br>
         * This creates a problem when initializing the world, we don't know the camera angle the player has.<br>
         * Without this, the player would always start facing the 0;0 coordinate.
         * <br>
         * To fix this, the camera is initialized with pitch and yaw being null. If that is the case,<br>
         * then the current player rotation is set with this method in {@link MixinEntityRenderer#runUpdate(float)}
         * @param pitch The absolute player pitch
         * @param yaw The absolute player yaw
         */
        public void setCamera(Float pitch, Float yaw) {
            nextCameraAngle.set(pitch, yaw);
        }

        /**
         * @return The current pitch coordinate of the player. May be null when it's initialized
         */
        public Float getCurrentPitch() {
            return currentCameraAngle.getPitch();
        }

        /**
         * @return The current yaw coordinate of the player. May be null when it's initialized
         */
        public Float getCurrentYaw() {
            return currentCameraAngle.getYaw();
        }

        /**
         * @return The pitch coordinate of the player in the next tick. May be null when it's initialized
         */
        public Float getNextPitch() {
            return nextCameraAngle.getPitch();
        }

        /**
         * @return The yaw coordinate of the player in the next tick. May be null when it's initialized
         */
        public Float getNextYaw() {
            return nextCameraAngle.getYaw();
        }

        /**
         * Clears the {@link #nextCameraAngle}
         */
        public void clearNext() {
            nextCameraAngle.clear();
        }

        /**
         * Clears the {@link #currentCameraAngle}
         */
        public void clearCurrent() {
            currentCameraAngle.clear();
        }
    }

    // ======================================================================
    // Data classes: VirtualKeyboard, VirtualMouse, VirtualCameraAngle
    // ======================================================================

    /**
     * Represents the state of the keyboard.
     */
    public static class VirtualKeyboard {
        private boolean[] keys = new boolean[256];
        private boolean[] nextKeys = new boolean[256];

        public void updateFromEvent(int keycode, boolean keystate, char character, boolean repeatEventsEnabled) {
            if (keycode >= 0 && keycode < keys.length) {
                nextKeys[keycode] = keystate;
            }
        }

        public void deepCopyFrom(VirtualKeyboard other) {
            System.arraycopy(other.nextKeys, 0, this.nextKeys, 0, nextKeys.length);
        }

        public void getVirtualEvents(VirtualKeyboard next, Queue<VirtualKeyboardEvent> queue) {
            // Generate events for key changes
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] != next.nextKeys[i]) {
                    queue.add(new VirtualKeyboardEvent(i, next.nextKeys[i], (char) 0));
                }
            }
        }

        public void moveFrom(VirtualKeyboard other) {
            System.arraycopy(other.nextKeys, 0, this.keys, 0, keys.length);
        }

        public boolean isKeyDown(int keycode) {
            return keycode >= 0 && keycode < keys.length && keys[keycode];
        }

        public void clear() {
            java.util.Arrays.fill(keys, false);
            java.util.Arrays.fill(nextKeys, false);
        }

        public List<String> getCurrentPresses() {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                if (keys[i]) {
                    list.add(String.valueOf(i));
                }
            }
            return list;
        }
    }

    /**
     * Represents the state of the mouse.
     */
    public static class VirtualMouse {
        private boolean[] buttons = new boolean[8];
        private boolean[] nextButtons = new boolean[8];
        private int scrollWheel = 0;
        private int nextScrollWheel = 0;
        private int cursorX = 0;
        private int cursorY = 0;
        private int nextCursorX = 0;
        private int nextCursorY = 0;

        public void updateFromEvent(int keycode, boolean keystate, int scrollwheel, int cursorX, int cursorY) {
            if (keycode >= 0 && keycode < buttons.length) {
                nextButtons[keycode] = keystate;
            }
            nextScrollWheel = scrollwheel;
            nextCursorX = cursorX;
            nextCursorY = cursorY;
        }

        public void deepCopyFrom(VirtualMouse other) {
            System.arraycopy(other.nextButtons, 0, this.nextButtons, 0, buttons.length);
            this.nextScrollWheel = other.nextScrollWheel;
            this.nextCursorX = other.nextCursorX;
            this.nextCursorY = other.nextCursorY;
        }

        public void getVirtualEvents(VirtualMouse next, Queue<VirtualMouseEvent> queue) {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] != next.nextButtons[i]) {
                    queue.add(new VirtualMouseEvent(i, next.nextButtons[i], 0, 0, 0));
                }
            }
            if (scrollWheel != next.nextScrollWheel) {
                queue.add(new VirtualMouseEvent(-1, false, next.nextScrollWheel, 0, 0));
            }
        }

        public void moveFrom(VirtualMouse other) {
            System.arraycopy(other.nextButtons, 0, this.buttons, 0, buttons.length);
            this.scrollWheel = other.nextScrollWheel;
            this.cursorX = other.nextCursorX;
            this.cursorY = other.nextCursorY;
        }

        public boolean isKeyDown(int keycode) {
            return keycode >= 0 && keycode < buttons.length && buttons[keycode];
        }

        public void clear() {
            java.util.Arrays.fill(buttons, false);
            java.util.Arrays.fill(nextButtons, false);
            scrollWheel = 0;
            nextScrollWheel = 0;
            cursorX = 0;
            cursorY = 0;
            nextCursorX = 0;
            nextCursorY = 0;
        }

        public List<String> getCurrentPresses() {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i]) {
                    list.add(String.valueOf(i));
                }
            }
            return list;
        }
    }

    /**
     * Represents the camera angle (pitch and yaw).
     */
    public static class VirtualCameraAngle {
        private Float pitch;
        private Float yaw;
        private Float nextPitch;
        private Float nextYaw;

        public void updateFromEvent(float pitchDelta, float yawDelta, boolean updateSubtick) {
            if (nextPitch == null) nextPitch = 0f;
            if (nextYaw == null) nextYaw = 0f;
            nextPitch += pitchDelta;
            nextYaw += yawDelta;
        }

        public void deepCopyFrom(VirtualCameraAngle other) {
            this.nextPitch = other.nextPitch;
            this.nextYaw = other.nextYaw;
        }

        public void moveFrom(VirtualCameraAngle other) {
            this.pitch = other.nextPitch;
            this.yaw = other.nextYaw;
        }

        public void set(Float pitch, Float yaw) {
            this.nextPitch = pitch;
            this.nextYaw = yaw;
        }

        public Float getPitch() {
            return pitch;
        }

        public Float getYaw() {
            return yaw;
        }

        public void clear() {
            this.pitch = null;
            this.yaw = null;
            this.nextPitch = null;
            this.nextYaw = null;
        }
    }

    /**
     * Handles interpolation for camera angle.
     */
    public class VirtualInterpolationHandler {
        private boolean interpolationEnabled = true;

        public void setInterpolation(boolean interpolate) {
            this.interpolationEnabled = interpolate;
        }

        public boolean isInterpolationEnabled() {
            return interpolationEnabled;
        }
    }
}