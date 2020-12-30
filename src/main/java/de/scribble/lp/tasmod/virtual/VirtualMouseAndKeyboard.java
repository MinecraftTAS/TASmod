package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.playback.InputPlayback;

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
public class VirtualMouseAndKeyboard {
	static VirtualKeys KEY_NONE=new VirtualKeys("0", 0);
	static VirtualKeys KEY_ESC=new VirtualKeys("ESC", 1);
	static VirtualKeys KEY_1=new VirtualKeys("KEY_1", 2);
	static VirtualKeys KEY_2=new VirtualKeys("KEY_2", 3);
	static VirtualKeys KEY_3=new VirtualKeys("KEY_3", 4);
	static VirtualKeys KEY_4=new VirtualKeys("KEY_4", 5);
	static VirtualKeys KEY_5=new VirtualKeys("KEY_5", 6);
	static VirtualKeys KEY_6=new VirtualKeys("KEY_6", 7);
	static VirtualKeys KEY_7=new VirtualKeys("KEY_7", 8);
	static VirtualKeys KEY_8=new VirtualKeys("KEY_8", 9);
	static VirtualKeys KEY_9=new VirtualKeys("KEY_9", 10);
	static VirtualKeys KEY_0=new VirtualKeys("KEY_0", 11);
	static VirtualKeys KEY_MINUS=new VirtualKeys("MINUS", 12);
	static VirtualKeys KEY_EQUALS=new VirtualKeys("EQUALS", 13);
	static VirtualKeys KEY_BACK=new VirtualKeys("BACK", 14);
	static VirtualKeys KEY_TAB=new VirtualKeys("TAB", 15);
	static VirtualKeys KEY_Q=new VirtualKeys("Q", 16);
	static VirtualKeys KEY_W=new VirtualKeys("W", 17);
	static VirtualKeys KEY_E=new VirtualKeys("E", 18);
	static VirtualKeys KEY_R=new VirtualKeys("R", 19);
	static VirtualKeys KEY_T=new VirtualKeys("T", 20);
	static VirtualKeys KEY_Y=new VirtualKeys("Y", 21);
	static VirtualKeys KEY_U=new VirtualKeys("U", 22);
	static VirtualKeys KEY_I=new VirtualKeys("I", 23);
	static VirtualKeys KEY_O=new VirtualKeys("O", 24);
	static VirtualKeys KEY_P=new VirtualKeys("P", 25);
	static VirtualKeys KEY_LBRACKET=new VirtualKeys("LBRACKET", 26);
	static VirtualKeys KEY_RBRACKET=new VirtualKeys("RBRACKET", 27);
	static VirtualKeys KEY_RETURN=new VirtualKeys("RETURN", 28);
	static VirtualKeys KEY_LCONTROL=new VirtualKeys("LCONTROL", 29);
	static VirtualKeys KEY_A=new VirtualKeys("A", 30);
	static VirtualKeys KEY_S=new VirtualKeys("S", 31);
	static VirtualKeys KEY_D=new VirtualKeys("D", 32);
	static VirtualKeys KEY_F=new VirtualKeys("F", 33);
	static VirtualKeys KEY_G=new VirtualKeys("G", 34);
	static VirtualKeys KEY_H=new VirtualKeys("H", 35);
	static VirtualKeys KEY_J=new VirtualKeys("J", 36);
	static VirtualKeys KEY_K=new VirtualKeys("K", 37);
	static VirtualKeys KEY_L=new VirtualKeys("L", 38);
	static VirtualKeys KEY_SEMICOLON=new VirtualKeys("SEMICOLON", 39);
	static VirtualKeys KEY_APOSTROPHE=new VirtualKeys("APOSTROPHE", 40);
	static VirtualKeys KEY_GRAVE=new VirtualKeys("GRAVE", 41);
	static VirtualKeys KEY_LSHIFT=new VirtualKeys("LSHIFT", 42);
	static VirtualKeys KEY_BACKSLASH=new VirtualKeys("BACKSLASH", 43);
	static VirtualKeys KEY_Z=new VirtualKeys("Z", 44);
	static VirtualKeys KEY_X=new VirtualKeys("X", 45);
	static VirtualKeys KEY_C=new VirtualKeys("C", 46);
	static VirtualKeys KEY_V=new VirtualKeys("V", 47);
	static VirtualKeys KEY_B=new VirtualKeys("B", 48);
	static VirtualKeys KEY_N=new VirtualKeys("N", 49);
	static VirtualKeys KEY_M=new VirtualKeys("M", 50);
	static VirtualKeys KEY_COMMA=new VirtualKeys("COMMA", 51);
	static VirtualKeys KEY_PERIOS=new VirtualKeys("PERIOS", 52);
	static VirtualKeys KEY_SLASH=new VirtualKeys("SLASH", 53);
	static VirtualKeys KEY_RSHIFT=new VirtualKeys("RSHIFT", 54);
	static VirtualKeys KEY_MULTIPLY=new VirtualKeys("MULTIPLY", 55);
	static VirtualKeys KEY_LMENU=new VirtualKeys("LMENU", 56);
	static VirtualKeys KEY_SPACE=new VirtualKeys("SPACE", 57);
	static VirtualKeys KEY_CAPITAL=new VirtualKeys("CAPSLOCK", 58);
	static VirtualKeys KEY_F1=new VirtualKeys("F1", 59);
	static VirtualKeys KEY_F2=new VirtualKeys("F2", 60);
	static VirtualKeys KEY_F3=new VirtualKeys("F3", 61);
	static VirtualKeys KEY_F4=new VirtualKeys("F4", 62);
	static VirtualKeys KEY_F5=new VirtualKeys("F5", 63);
	static VirtualKeys KEY_F6=new VirtualKeys("F6", 64);
	static VirtualKeys KEY_F7=new VirtualKeys("F7", 65);
	static VirtualKeys KEY_F8=new VirtualKeys("F8", 66);
	static VirtualKeys KEY_F9=new VirtualKeys("F9", 67);
	static VirtualKeys KEY_F10=new VirtualKeys("F10", 68);
	static VirtualKeys KEY_NUMLOCK=new VirtualKeys("NUMLOCK", 69);
	static VirtualKeys KEY_SCROLL=new VirtualKeys("SCROLL", 70);
	static VirtualKeys KEY_NUMPAD7=new VirtualKeys("NUMPAD7", 71);
	static VirtualKeys KEY_NUMPAD8=new VirtualKeys("NUMPAD8", 72);
	static VirtualKeys KEY_NUMPAD9=new VirtualKeys("NUMPAD9", 73);
	static VirtualKeys KEY_SUBTRACT=new VirtualKeys("SUBTRACT", 74);
	static VirtualKeys KEY_NUMPAD4=new VirtualKeys("NUMPAD4", 75);
	static VirtualKeys KEY_NUMPAD5=new VirtualKeys("NUMPAD5", 76);
	static VirtualKeys KEY_NUMPAD6=new VirtualKeys("NUMPAD6", 77);
	static VirtualKeys KEY_ADD=new VirtualKeys("ADD", 78);
	static VirtualKeys KEY_NUMPAD1=new VirtualKeys("NUMPAD1", 79);
	static VirtualKeys KEY_NUMPAD2=new VirtualKeys("NUMPAD2", 80);
	static VirtualKeys KEY_NUMPAD3=new VirtualKeys("NUMPAD3", 81);
	static VirtualKeys KEY_NUMPAD0=new VirtualKeys("NUMPAD0", 82);
	static VirtualKeys KEY_DECIMAL=new VirtualKeys("DECIMAL", 83);
	static VirtualKeys KEY_F11=new VirtualKeys("F11", 87);
	static VirtualKeys KEY_F12=new VirtualKeys("F12", 88);
	static VirtualKeys KEY_F13=new VirtualKeys("F13", 100);
	static VirtualKeys KEY_F14=new VirtualKeys("F14", 101);
	static VirtualKeys KEY_F15=new VirtualKeys("F15", 102);
	static VirtualKeys KEY_F16=new VirtualKeys("F16", 103);
	static VirtualKeys KEY_F17=new VirtualKeys("F17", 104);
	static VirtualKeys KEY_F18=new VirtualKeys("F18", 105);
	static VirtualKeys KEY_KANA=new VirtualKeys("KANA", 112);
	static VirtualKeys KEY_F19=new VirtualKeys("F19", 113);
	static VirtualKeys KEY_CONVERT=new VirtualKeys("CONVERT", 121);
	static VirtualKeys KEY_NOCONVERT=new VirtualKeys("NOCONVERT", 123);
	static VirtualKeys KEY_YEN=new VirtualKeys("YEN", 125);
	static VirtualKeys KEY_NUMPADEQUALS=new VirtualKeys("NUMPADEQUALS", 141);
	static VirtualKeys KEY_CIRCUMFLEX=new VirtualKeys("CIRCUMFLEX", 144);
	static VirtualKeys KEY_AT=new VirtualKeys("AT", 145);
	static VirtualKeys KEY_COLON=new VirtualKeys("COLON", 146);
	static VirtualKeys KEY_UNDERLINE=new VirtualKeys("UNDERLINE", 147);
	static VirtualKeys KEY_KANJI=new VirtualKeys("KANJI", 148);
	static VirtualKeys KEY_STOP=new VirtualKeys("STOP", 149);
	static VirtualKeys KEY_NUMPADENTER=new VirtualKeys("NUMPADENTER", 156);
	static VirtualKeys KEY_RCONTROL=new VirtualKeys("RCONTROL", 157);
	static VirtualKeys KEY_NUMPADCOMMA=new VirtualKeys("NUMPADCOMMA", 179);
	static VirtualKeys KEY_DIVIDE=new VirtualKeys("DIVIDE", 181);
	static VirtualKeys KEY_PRINT=new VirtualKeys("PRINT", 183);
	static VirtualKeys KEY_PAUSE=new VirtualKeys("PAUSE", 197);
	static VirtualKeys KEY_HOME=new VirtualKeys("HOME", 199);
	static VirtualKeys KEY_UP=new VirtualKeys("UP", 200);
	static VirtualKeys KEY_PRIOR=new VirtualKeys("PRIOR", 201);
	static VirtualKeys KEY_LEFT=new VirtualKeys("LEFT", 203);
	static VirtualKeys KEY_RIGHT=new VirtualKeys("RIGHT", 205);
	static VirtualKeys KEY_END=new VirtualKeys("END", 207);
	static VirtualKeys KEY_DOWN=new VirtualKeys("DOWN", 208);
	static VirtualKeys KEY_NEXT=new VirtualKeys("NEXT", 209);
	static VirtualKeys KEY_INSERT=new VirtualKeys("INSERT", 210);
	static VirtualKeys KEY_DELETE=new VirtualKeys("DELETE", 211);
	static List<VirtualChar> charList= new ArrayList<VirtualChar>();
	
	static VirtualKeys KEY_MOUSEMOVED=new VirtualKeys("MOUSEMOVED", -101);
	static VirtualKeys KEY_LK=new VirtualKeys("LC", -100);
	static VirtualKeys KEY_RK=new VirtualKeys("RC", -99);
	static VirtualKeys KEY_MK=new VirtualKeys("MC", -98);
	static VirtualKeys KEY_MBUTTON3=new VirtualKeys("MBUTTON3", -97);
	static VirtualKeys KEY_MBUTTON4=new VirtualKeys("MBUTTON4", -96);
	static VirtualKeys KEY_MBUTTON5=new VirtualKeys("MBUTTON5", -95);
	static VirtualKeys KEY_MBUTTON6=new VirtualKeys("MBUTTON6", -94);
	static VirtualKeys KEY_MBUTTON7=new VirtualKeys("MBUTTON7", -93);
	static VirtualKeys KEY_MBUTTON8=new VirtualKeys("MBUTTON8", -92);
	static VirtualKeys KEY_MBUTTON9=new VirtualKeys("MBUTTON9", -91);
	static VirtualKeys KEY_MBUTTON10=new VirtualKeys("MBUTTON10", -90);
	static VirtualKeys KEY_MBUTTON11=new VirtualKeys("MBUTTON11", -89);
	static VirtualKeys KEY_MBUTTON12=new VirtualKeys("MBUTTON12", -88);
	static VirtualKeys KEY_MBUTTON13=new VirtualKeys("MBUTTON13", -87);
	static VirtualKeys KEY_MBUTTON14=new VirtualKeys("MBUTTON14", -86);
	static VirtualKeys KEY_MBUTTON15=new VirtualKeys("MBUTTON15", -85);
	
	
	private static List<String> keyList= new ArrayList<String>();
	private static List<Character> charsListOut= new ArrayList<Character>();
	
	private static List<VirtualMouseEvent> mouseEventList= new ArrayList<VirtualMouseEvent>();
	
	private static List<VirtualKeyboardEvent> keyboardEventList= new ArrayList<VirtualKeyboardEvent>();
	
	private static int keyboardIndex=-1;
	private static int mouseIndex=-1;
	
	private static VirtualSubticks subtick;
	
	private static int timeSinceLastTick=0;
	
	/*================================Code similar to keybidings================================*/
	/**
	 * Picks up keycodes, stores their state and adds keycodes not listed
	 * @param keyCode
	 * @param pressed
	 * @return changedKeycode
	 */
	public static int runThroughKeyboard(int keyCode, boolean pressed) {
		VirtualKeys.keyCodes.get(getKeyCodesFromKeyCode(keyCode)).setPressed(pressed);
		return keyCode;
	}
	/**
	 * Checks if the given keycode is pressed on the Virtual keyboard/mouse
	 * @param keyCode
	 * @return boolean
	 */
	public static boolean isKeyDown(int keyCode) {
		if(VirtualKeys.keyCodes.get(keyCode)!=null) {
			return VirtualKeys.keyCodes.get(keyCode).isKeyDown();
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
	/**
	 * Checks if the given character is pressed on the Virtual keyboard/mouse
	 * @param character
	 * @return boolean
	 */
	public static boolean isKeyDown(char character) {
		return VirtualChar.keyChars.get(character).isPressed();
	}
	
	public static void resetCharlist() {
		charList.clear();
	}
	/**
	 * Checks if the keycode is in the list and adds it if necessary
	 * @param keyCode
	 * @return
	 */
	public static int getKeyCodesFromKeyCode(int keyCode) {
		if(VirtualKeys.keyCodes.get(keyCode)==null) {
			updateMissingKeyCode(keyCode);
			return VirtualKeys.keyCodes.get(keyCode).getKeycode();
		}else return VirtualKeys.keyCodes.get(keyCode).getKeycode();
	}
	/**
	 * Updates the given misssing keycode. The name will be the keycode to string
	 * @param keyCode
	 */
	private static void updateMissingKeyCode(int keyCode) {
		VirtualKeys.keyCodes.put(keyCode, new VirtualKeys(Integer.toString(keyCode), keyCode));
		VirtualKeys.keyNames.put(Integer.toString(keyCode), new VirtualKeys(Integer.toString(keyCode), keyCode));
	}
	/**
	 * Looks through the list of names and returns it's keycode returns -1 if it can't find anything
	 * @param name
	 * @return int keycode, else -1
	 */
	public static int getKeyCodeFromKeyName(String name) {
		if(VirtualKeys.keyNames.get(name) != null) {
			return VirtualKeys.keyNames.get(name).getKeycode();
		}else return -1;
	}
	/**
	 * Looks through the list of keycodes and returns it's name.
	 * @param keycode
	 * @return
	 */
	public static String getNameFromKeyCode(int keycode) {
		return VirtualKeys.keyCodes.get(keycode).getName();
	}
	/**
	 * Resets every key in the list to be not pressed
	 */
	public static void unpressEverything() {
		VirtualKeys.keyCodes.forEach((key,virtual)->{
			virtual.setPressed(false);
		});
	}
	/**
	 * Reset things after a tick has passed
	 */
	public static void reset() {
		resetCharlist();
	}
	/**
	 * Debug method to return a list of keys currently being pressed. Could be used for a keystroke display
	 * @return List of keynames currently being pressed
	 */
	public static List<String> getCurrentKeyboardPresses() {
		keyList.clear();
		VirtualKeys.keyCodes.forEach((keycodes, virtualkeys)->{
			if(keycodes>=0) {
				if(virtualkeys.isKeyDown()) {
					keyList.add(virtualkeys.getName());
				}
			}
		});
		return keyList;
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
	/*==========================Emulating keyboard events==========================*/
	/**
	 * Resets the keyboard event list, so it can be recorded. Usually happens right before they are filled
	 */
	public static void prepareKeyboardEvents() {
		keyboardEventList.clear();
		keyboardIndex=-1;
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
	 * keycode: The key in question<br>
	 * keystate: What state it has, true for pressed, false for released<br>
	 * character: Every key on the keyboard has a character associated with it (even if it's a null character)<br>
	 */
	public static void fillKeyboardEvents(int keycode, boolean keystate, char character) {
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
	 * Get the complete list of keyboard events, used by InputRecorder.
	 */
	public static List<VirtualKeyboardEvent> getKeyboardEvents(){
		return keyboardEventList;
	}
	/*Getters for the keaboard events*/
	public static int getEventKeyboardButton() {
		return keyboardEventList.get(keyboardIndex).getKeyCode();
	}
	public static boolean getEventKeyboardButtonState() {
		return keyboardEventList.get(keyboardIndex).isState();
	}
	public static char getEventChar() {
		return keyboardEventList.get(keyboardIndex).getCharacter();
	}
	/*==============================Emulating mouse events==============================*/
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
			mouseEventList.add(new VirtualMouseEvent(keycode, state, scrollwheel, mouseX, mouseY, slotidx));
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
	public static void fillSubtick(int tick, float pitch, float yaw) {
		if(!InputPlayback.isPlayingback()) {
			subtick=new VirtualSubticks(tick, pitch, yaw);
		}
	}
	public static void fillSubtickWithPlayback() {
		if(InputPlayback.isPlayingback()) {
			subtick=InputPlayback.getCurrentSubtick();
		}
	}
	public static int getSubtickTick() {
		return subtick.getTick();
	}
	public static float getSubtickPitch() {
		return subtick.getPitch();
	}
	public static float getSubtickYaw() {
		return subtick.getYaw();
	}
	public static VirtualSubticks getSubtick() {
		return subtick;
	}
}
