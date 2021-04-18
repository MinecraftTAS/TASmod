package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.recording.InputRecorder;
import net.minecraft.client.settings.KeyBinding;

/**
 * Emulates a virtual keyboard that is set between the lwjgl.input.Keyboard & lwjgl.input.Mouse and the keybindings.<br>
 * After each tick, it loads every keyboard/mouse event from lwjgl, then passes this to the keybindings.<br>
 * That way, it is possible to read out that information and save it to a file, or load other events into the slot where the lwjgl would put it's events, creating a playback functionality.<br>
 * This also stores keynames and it's keycodes, since lwjgl only has a handful of keynames saved via the Keyboard#getKeyName method<br>
 * Also records behaviour that is executed outside of tickbased methods.<br>
 * 
 * @author ScribbleLP
 *
 */
@Deprecated
public class VirtualInput {
	
	public static VirtualKeyboard keyboard=new VirtualKeyboard();
	
	private static VirtualMouse mouse=new VirtualMouse();
	
	static List<VirtualChar> charList= new ArrayList<VirtualChar>();
	
	private static List<Character> charsListOut= new ArrayList<Character>();
	
	private static VirtualSubticks subtick;
	
	private static int timeSinceLastTick=0;
	
	
	/*==========================Emulating keyboard events==========================*/
	
	/**
	 * The index of the keyboard signalising which event should be returned from the {@linkplain #keyboardEventList}
	 */
	private static int keyboardIndex=-1;
	
	/**
	 * A list of keyboard events that will be excecuted in the next tick.<br>
	 * <br>
	 * These events will get filled in {@linkplain #fillKeyboardEvents(int, boolean, char)} or in {@linkplain #fillKeyboardEventsWithPlayback()}<br>
	 * <br>
	 * The current index is saved in {@linkplain #keyboardIndex} and the list will be cleared in {@linkplain #prepareKeyboardEvents()} at the start of the tick <br>
	 */
	private static List<VirtualKeyboardEvent> keyboardEventList= new ArrayList<VirtualKeyboardEvent>();
	
	/**
	 * Resets the keyboard event list, so it can be recorded. Usually happens right before they are filled
	 */
	public static void prepareKeyboardEvents() {
		keyboardEventList.clear();
		keyboardIndex=-1;
		resetCharList();
	}
	
	/**
	 * Records a keyboard event and adds it to a list.<br>
	 * <br>
	 * <i>Example:</i><br>
	 * If you press and release the "W" key in a tick, 2 events will be created:<br>
	 * <i>17,true,'w'</i><br>
	 * and<br>
	 * <i>17,false,'w'</i><br>
	 * These events will all be recorded and put into a list.<br>
	 * <br>
	 * keycode The key in question<br>
	 * keystate What state it has, true for pressed, false for released<br>
	 * character Every key on the keyboard has a character associated with it (even if it's a null character)<br>
	 */
	public static void fillKeyboardEvents(int keycode, boolean keystate, char character) {
		if(VirtualKeybindings.isKeyCodeAlwaysBlocked(keycode))return;
		
		keyboardEventList.add(new VirtualKeyboardEvent(keycode, keystate, character));
	}
	
	/**
	 * Only active when InputPlayback.isPlayingback()<br>
	 * This fills the keyboard events list with events from a file<br>
	 * <br>
	 * In contrast to the mouse methods, I am allowing manual keyboard inputs while a playback is executing, to stop the playback or something.<br>
	 * <br>
	 */
	public static void fillKeyboardEventsWithPlayback() {
		if(InputPlayback.isPlayingback()) {
			List<VirtualKeyboardEvent>events=InputPlayback.getCurrentKeyEvent();
			keyboardEventList.addAll(events);
		}
	}
	
	/**
	 * Switches o the next keyboard event in the list similar to how Keyboard.next() would function
	 */
	public static boolean nextKeyboardEvent() {
		if (keyboardIndex<keyboardEventList.size()-1&&!keyboardEventList.isEmpty()) {
			keyboardIndex++;
			return true;
		}else return false;
	}
	
	/**
	 * Get the complete list of keyboard events, used by {@link InputRecorder}.
	 */
	public static List<VirtualKeyboardEvent> getKeyboardEvents(){
		return keyboardEventList;
	}
	
	/**
	 * Get's the current keycode from the {@link #keyboardEventList}
	 * 
	 * @return Current keycode
	 */
	public static int getEventKeyboardButton() {
		return keyboardEventList.get(keyboardIndex).getKeyCode();
	}
	
	/**
	 * Get's the current keystate from the {@link #keyboardEventList}
	 * 
	 * @return Current keystate, true for pressed, false for unpressed
	 */
	public static boolean getEventKeyboardButtonState() {
		return keyboardEventList.get(keyboardIndex).isState();
	}
	
	/**
	 * Returns the current character from the {@link #keyboardEventList}
	 * 
	 * @return Character associated with the keyboard key
	 */
	public static char getEventChar() {
		return keyboardEventList.get(keyboardIndex).getCharacter();
	}
	
	/*==============================Emulating mouse events==============================*/

	/**
	 * The index of the mouse signalising which event should be returned from the {@linkplain #mouseEventList}
	 */
	private static int mouseIndex=-1;
	
	/**
	 * A list of mouse events that will be excecuted in the next tick.<br>
	 * <br>
	 * These events will get filled in {@linkplain #fillMouseEvents(int, boolean, char)} or in {@linkplain #fillMouseEventsWithPlayback()}<br>
	 * <br>
	 * The current index is saved in {@linkplain #mouseIndex} and the list will be cleared in {@linkplain #prepareMouseEvents()} at the start of the tick <br>
	 */
	private static List<VirtualMouseEvent> mouseEventList= new ArrayList<VirtualMouseEvent>();
	
	/**
	 * Resets the mouse event list, so it can be recorded. Usually happens right before they are filled
	 */
	public static void prepareMouseEvents() {
		mouseEventList.clear();
		mouseIndex=-1;
	}
	
	/**
	 * Records a mouse event and adds it to a list.<br>
	 * <br>
	 * <i>Example:</i><br>
	 * If you press and release the "KEY_LC" key in a tick, 2 events will be created:<br>
	 * <i>17,true,'w'</i><br>
	 * and<br>
	 * <i>17,false,'w'</i><br>
	 * These events will all be recorded and put into a list.<br>
	 * <br>
	 * keycode: The key in question<br>
	 * keystate: What state it has, true for pressed, false for released<br>
	 * character: Every key on the keyboard has a character associated with it (even if it's a null character)<br>
	 */
	public static void fillMouseEvents(int keycode, boolean state, int scrollwheel, int mouseX, int mouseY, int slotidx) {
		if (!InputPlayback.isPlayingback()) {
			mouseEventList.add(new VirtualMouseEvent(keycode, state, scrollwheel, mouseX, mouseY));
		}
	}
	
	public static void fillMouseEventsWithPlayback() {
		if(InputPlayback.isPlayingback()) {
			List<VirtualMouseEvent>events=InputPlayback.getCurrentMouseEvent();
			mouseEventList.addAll(events);
		}
	}
	
	public static boolean nextMouseEvent() {
		if (mouseIndex<mouseEventList.size()-1&&!mouseEventList.isEmpty()) {
			mouseIndex++;
			return true;
		}else return false;
	}
	
	public static List<VirtualMouseEvent> getMouseEvents(){
		return mouseEventList;
	}
	
	public static int getEventMouseButton() {
		return mouseEventList.get(mouseIndex).getKeyCode();
	}
	
	public static boolean getEventMouseButtonState() {
		return mouseEventList.get(mouseIndex).isState();
	}
	
	public static int getEventDWheel() {
		return mouseEventList.get(mouseIndex).getScrollwheel();
	}
	
	public static int getEventX() {
		return mouseEventList.get(mouseIndex).getMouseX();
	}
	
	public static int getEventY() {
		return mouseEventList.get(mouseIndex).getMouseY();
	}
	
	/*==========================Tick Independant Behaviour=================================*/
	/*					This section will execute every game loop						   */
	
	public static void resetTimeSinceLastTick() {
		timeSinceLastTick=0;
	}
	
	public static void incrementTimeSinceLastTick() {
		timeSinceLastTick++;
	}
	
	/*Unused*/
	public static int getTimeSinceLastTick() {
		return timeSinceLastTick;
	}
	
//	public static void fillSubtick(int tick, float pitch, float yaw) {
//		if(!InputPlayback.isPlayingback()) {
//			subtick=new VirtualSubticks(tick, pitch, yaw);
//		}
//	}
	
	public static void fillSubtickWithPlayback() {
		if(InputPlayback.isPlayingback()) {
			subtick=InputPlayback.getCurrentSubtick();
		}
	}
	
//	public static int getSubtickTick() {
//		return subtick.getTick();
//	}
	
	public static float getSubtickPitch() {
		return subtick.getPitch();
	}
	
	public static float getSubtickYaw() {
		return subtick.getYaw();
	}
	
	public static VirtualSubticks getSubtick() {
		return subtick;
	}
	
	/*================================Code similar to keybidings================================*/
	/**
	 * Picks up keycodes, stores their state and adds keycodes not listed
	 * @param keyCode
	 * @param pressed
	 * @return changedKeycode
	 */
	public static int runThroughKeyboard(int keyCode, boolean pressed) {
		if(VirtualKeybindings.isKeyCodeAlwaysBlocked(keyCode)) {
			return keyCode;
		}
		keyboard.get(addKeyCodeIfNecessary(keyCode)).setPressed(pressed);
		return keyCode;
	}
	
	/**
	 * Checks if the given keycode is pressed on the Virtual keyboard/mouse
	 * @param keyCode
	 * @return boolean
	 */
	public static boolean isKeyDown(int keyCode) {
		if(keyboard.get(keyCode)!=null) {
			return keyboard.get(keyCode).isKeyDown();
		}else return false;
	}
	
	/**
	 * Picks up chars from the keyboard and stores their state. Used in guiscreens
	 * @param character
	 * @param pressed
	 * @return boolean
	 */
	public static char runCharThroughKeyboard(char character, boolean pressed) {
		charList.add(new VirtualChar(character, pressed));
		return character;
	}
	
	public static void resetCharList() {
		charList.clear();
	}
	
	/**
	 * Checks if the given character is pressed on the Virtual keyboard/mouse
	 * @param character
	 * @return boolean
	 */
	public static boolean isKeyDown(char character) {
		return VirtualChar.keyChars.get(character).isPressed();
	}
	
	/**
	 * Checks if the keycode is in the list and adds it if necessary
	 * @param keyCode
	 * @return
	 */
	public static int addKeyCodeIfNecessary(int keyCode) {
		if(keyboard.get(keyCode)==null) {
			updateMissingKeyCode(keyCode);
			return keyboard.get(keyCode).getKeycode();
		}else return keyboard.get(keyCode).getKeycode();
	}
	
	/**
	 * Updates the given misssing keycode. The name will be the keycode to string
	 * @param keyCode
	 */
	private static void updateMissingKeyCode(int keyCode) {
		keyboard.add(keyCode);
	}
	
	/**
	 * Looks through the list of names and returns it's keycode returns -1 if it can't find anything
	 * @param name Name of the key
	 * @return int keycode, else -1
	 */
	public static int getKeyCodeFromKeyName(String name) {
		if(keyboard.get(name) != null) {
			return keyboard.get(name).getKeycode();
		}else return -1;
	}
	
	/**
	 * Looks through the list of keycodes and returns it's name.
	 * @param keycode
	 * @return Name of the keycode
	 */
	public static String getNameFromKeyCode(int keycode) {
		return keyboard.get(keycode).getName();
	}
	
	/**
	 * Resets every key in the list to be not pressed
	 */
	public static void unpressEverything() {
		
		KeyBinding.unPressAllKeys();
	}
	
	
	/**
	 * Debug method to return a list of keys currently being pressed. Could be used for a keystroke display
	 * @return List of keynames currently being pressed
	 */
	public static List<String> getCurrentKeyboardPresses() {
		return keyboard.getCurrentPresses();
	}
	
	/**
	 * Debug method to return a list of characters currently being pressed. Could be used for a keystroke display
	 * @return List of characters
	 */
	public static List<Character> getCurrentCharList() {
		charsListOut.clear();
		if(charList.isEmpty()) {
			return charsListOut;
		}
		charList.forEach((virtual)->{
			if(virtual.isPressed()) {
				charsListOut.add(virtual.getName());
			}
		});
		return charsListOut;
	}
	
	/**
	 * Debug method to return a list of mouse keys currently being pressed. Could be used for a keystroke display
	 * @return List of keynames currently being pressed
	 */
	public static List<String> getCurrentMousePresses() {
		return mouse.getCurrentPresses();
	}
	
	/*================================Code for tickrate 0================================*/
	
	/*Different things need to happen during tickrate 0. As mentioned in https://github.com/ScribbleLP/TASmod/issues/44,
	 *the lwjgl buffer keeps running full*/
	
	
	private static void printKeyList() {
		keyboardEventList.forEach(action->{
			System.out.println(action.getKeyCode()+" "+action.isState());
		});
	}
}
