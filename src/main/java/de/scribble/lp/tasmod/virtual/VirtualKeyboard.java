package de.scribble.lp.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

public class VirtualKeyboard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694772261313303998L;

	private Map<Integer, VirtualKey> keyList;

	private List<Character> charList;

	/**
	 * Creates a copy of the virtual keyboard with the given key list
	 * 
	 * @param keyListIn
	 */
	public VirtualKeyboard(Map<Integer, VirtualKey> keyListIn, List<Character> charListIn) {
		Map<Integer, VirtualKey> copy = new HashMap<Integer, VirtualKey>();

		keyListIn.forEach((key, value) -> {
			copy.put(key, value.clone());
		});
		keyList = copy;

		List<Character> charCopy = new ArrayList<Character>();

		charListIn.forEach(charAction -> {
			charCopy.add(charAction);
		});
		charList = charCopy;
	}

	/**
	 * Creates a Keyboard, where the keys are all unpressed
	 */
	public VirtualKeyboard() {
		charList = new ArrayList<Character>();

		keyList = Maps.<Integer, VirtualKey>newHashMap();
		keyList.put(0, new VirtualKey("0", 0));
		keyList.put(1, new VirtualKey("ESC", 1));
		keyList.put(2, new VirtualKey("KEY_1", 2));
		keyList.put(3, new VirtualKey("KEY_2", 3));
		keyList.put(4, new VirtualKey("KEY_3", 4));
		keyList.put(5, new VirtualKey("KEY_4", 5));
		keyList.put(6, new VirtualKey("KEY_5", 6));
		keyList.put(7, new VirtualKey("KEY_6", 7));
		keyList.put(8, new VirtualKey("KEY_7", 8));
		keyList.put(9, new VirtualKey("KEY_8", 9));
		keyList.put(10, new VirtualKey("KEY_9", 10));
		keyList.put(11, new VirtualKey("KEY_0", 11));
		keyList.put(12, new VirtualKey("MINUS", 12));
		keyList.put(13, new VirtualKey("EQUALS", 13));
		keyList.put(14, new VirtualKey("BACK", 14));
		keyList.put(15, new VirtualKey("TAB", 15));
		keyList.put(16, new VirtualKey("Q", 16));
		keyList.put(17, new VirtualKey("W", 17));
		keyList.put(18, new VirtualKey("E", 18));
		keyList.put(19, new VirtualKey("R", 19));
		keyList.put(20, new VirtualKey("T", 20));
		keyList.put(21, new VirtualKey("Y", 21));
		keyList.put(22, new VirtualKey("U", 22));
		keyList.put(23, new VirtualKey("I", 23));
		keyList.put(24, new VirtualKey("O", 24));
		keyList.put(25, new VirtualKey("P", 25));
		keyList.put(26, new VirtualKey("LBRACKET", 26));
		keyList.put(27, new VirtualKey("RBRACKET", 27));
		keyList.put(28, new VirtualKey("RETURN", 28));
		keyList.put(29, new VirtualKey("LCONTROL", 29));
		keyList.put(30, new VirtualKey("A", 30));
		keyList.put(31, new VirtualKey("S", 31));
		keyList.put(32, new VirtualKey("D", 32));
		keyList.put(33, new VirtualKey("F", 33));
		keyList.put(34, new VirtualKey("G", 34));
		keyList.put(35, new VirtualKey("H", 35));
		keyList.put(36, new VirtualKey("J", 36));
		keyList.put(37, new VirtualKey("K", 37));
		keyList.put(38, new VirtualKey("L", 38));
		keyList.put(39, new VirtualKey("SEMICOLON", 39));
		keyList.put(40, new VirtualKey("APOSTROPHE", 40));
		keyList.put(41, new VirtualKey("GRAVE", 41));
		keyList.put(42, new VirtualKey("LSHIFT", 42));
		keyList.put(43, new VirtualKey("BACKSLASH", 43));
		keyList.put(44, new VirtualKey("Z", 44));
		keyList.put(45, new VirtualKey("X", 45));
		keyList.put(46, new VirtualKey("C", 46));
		keyList.put(47, new VirtualKey("V", 47));
		keyList.put(48, new VirtualKey("B", 48));
		keyList.put(49, new VirtualKey("N", 49));
		keyList.put(50, new VirtualKey("M", 50));
		keyList.put(51, new VirtualKey("COMMA", 51));
		keyList.put(52, new VirtualKey("PERIOD", 52));
		keyList.put(53, new VirtualKey("SLASH", 53));
		keyList.put(54, new VirtualKey("RSHIFT", 54));
		keyList.put(55, new VirtualKey("MULTIPLY", 55));
		keyList.put(56, new VirtualKey("ALT", 56));
		keyList.put(57, new VirtualKey("SPACE", 57));
		keyList.put(58, new VirtualKey("CAPSLOCK", 58));
		keyList.put(59, new VirtualKey("F1", 59));
		keyList.put(60, new VirtualKey("F2", 60));
		keyList.put(61, new VirtualKey("F3", 61));
		keyList.put(62, new VirtualKey("F4", 62));
		keyList.put(63, new VirtualKey("F5", 63));
		keyList.put(64, new VirtualKey("F6", 64));
		keyList.put(65, new VirtualKey("F7", 65));
		keyList.put(66, new VirtualKey("F8", 66));
		keyList.put(67, new VirtualKey("F9", 67));
		keyList.put(68, new VirtualKey("F10", 68));
		keyList.put(69, new VirtualKey("NUMLOCK", 69));
		keyList.put(70, new VirtualKey("SCROLL", 70));
		keyList.put(71, new VirtualKey("NUMPAD7", 71));
		keyList.put(72, new VirtualKey("NUMPAD8", 72));
		keyList.put(73, new VirtualKey("NUMPAD9", 73));
		keyList.put(74, new VirtualKey("SUBTRACT", 74));
		keyList.put(75, new VirtualKey("NUMPAD4", 75));
		keyList.put(76, new VirtualKey("NUMPAD5", 76));
		keyList.put(77, new VirtualKey("NUMPAD6", 77));
		keyList.put(78, new VirtualKey("ADD", 78));
		keyList.put(79, new VirtualKey("NUMPAD1", 79));
		keyList.put(80, new VirtualKey("NUMPAD2", 80));
		keyList.put(81, new VirtualKey("NUMPAD3", 81));
		keyList.put(82, new VirtualKey("NUMPAD0", 82));
		keyList.put(83, new VirtualKey("DECIMAL", 83));
		keyList.put(87, new VirtualKey("F11", 87));
		keyList.put(88, new VirtualKey("F12", 88));
		keyList.put(100, new VirtualKey("F13", 100));
		keyList.put(101, new VirtualKey("F14", 101));
		keyList.put(102, new VirtualKey("F15", 102));
		keyList.put(103, new VirtualKey("F16", 103));
		keyList.put(104, new VirtualKey("F17", 104));
		keyList.put(105, new VirtualKey("F18", 105));
		keyList.put(112, new VirtualKey("KANA", 112));
		keyList.put(113, new VirtualKey("F19", 113));
		keyList.put(121, new VirtualKey("CONVERT", 121));
		keyList.put(123, new VirtualKey("NOCONVERT", 123));
		keyList.put(125, new VirtualKey("YEN", 125));
		keyList.put(141, new VirtualKey("NUMPADEQUALS", 141));
		keyList.put(144, new VirtualKey("CIRCUMFLEX", 144));
		keyList.put(145, new VirtualKey("AT", 145));
		keyList.put(146, new VirtualKey("COLON", 146));
		keyList.put(147, new VirtualKey("UNDERLINE", 147));
		keyList.put(148, new VirtualKey("KANJI", 148));
		keyList.put(149, new VirtualKey("STOP", 149));
		keyList.put(156, new VirtualKey("NUMPADENTER", 156));
		keyList.put(157, new VirtualKey("RCONTROL", 157));
		keyList.put(179, new VirtualKey("NUMPADCOMMA", 179));
		keyList.put(181, new VirtualKey("DIVIDE", 181));
		keyList.put(183, new VirtualKey("PRINT", 183));
		keyList.put(184, new VirtualKey("ALT_GR", 184));
		keyList.put(197, new VirtualKey("PAUSE", 197));
		keyList.put(199, new VirtualKey("HOME", 199));
		keyList.put(200, new VirtualKey("UP", 200));
		keyList.put(201, new VirtualKey("PRIOR", 201));
		keyList.put(203, new VirtualKey("LEFT", 203));
		keyList.put(205, new VirtualKey("RIGHT", 205));
		keyList.put(207, new VirtualKey("END", 207));
		keyList.put(208, new VirtualKey("DOWN", 208));
		keyList.put(209, new VirtualKey("NEXT", 209));
		keyList.put(210, new VirtualKey("INSERT", 210));
		keyList.put(211, new VirtualKey("DELETE", 211));
		keyList.put(219, new VirtualKey("WIN", 219));
		keyList.put(221, new VirtualKey("APPS", 221));
	}

	public void add(int keycode) {
		String keyString = Integer.toString(keycode);

		keyList.put(keycode, new VirtualKey(keyString, keycode));
	}

	public VirtualKey get(int keycode) {
		VirtualKey key = keyList.get(keycode);
		if (key == null) {
			add(keycode);
			return keyList.get(keycode);
		} else
			return key;
	}

	public VirtualKey get(String keyname) {
		Collection<VirtualKey> list = keyList.values();
		VirtualKey out = null;

		for (VirtualKey key : list) {
			if (key.getName().equalsIgnoreCase(keyname)) {
				out = key;
			}
		}
		return out;
	}

	public List<String> getCurrentPresses() {
		List<String> out = new ArrayList<String>();

		keyList.forEach((keycodes, virtualkeys) -> {
			if (keycodes >= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	public Map<Integer, VirtualKey> getKeyList() {
		return this.keyList;
	}

	public void addChar(char charin) {
		charList.add(charin);
	}

	public List<Character> getCharList() {
		return charList;
	}

	public void clearCharList() {
		charList.clear();
	}

	public void clear() {
		keyList.forEach((keycode, key) -> {
			key.setPressed(false);
		});
		charList.clear();
	}

	public List<VirtualKeyboardEvent> getDifference(VirtualKeyboard keyboardToCompare) {

		List<VirtualKeyboardEvent> eventList = new ArrayList<VirtualKeyboardEvent>();

		keyList.forEach((keycodes, virtualkeys) -> {

			VirtualKey keyToCompare = keyboardToCompare.get(keycodes);

			if (!virtualkeys.equals(keyToCompare)) {
				if (Keyboard.areRepeatEventsEnabled()) {
					if (isUnicodeInList(keycodes)) {
						return;
					}
				}
				eventList.add(new VirtualKeyboardEvent(keycodes, keyToCompare.isKeyDown(), Character.MIN_VALUE));
			}

		});
		keyboardToCompare.charList.forEach(action -> {
			if (Keyboard.areRepeatEventsEnabled()) {
				eventList.add(decodeUnicode(action));
			} else {
				eventList.add(new VirtualKeyboardEvent(0, true, action));
			}
		});
		return eventList;
	}

	public char encodeUnicode(int keycode, char character) {
		switch (keycode) {
		case 15: // Tab
			return '\u21A6';
		case 199: // Pos1
			return '\u21E4';
		case 200: // Arrow Up
			return '\u2191';
		case 201: // Next
			return '\u21E7';
		case 203: // Arrow Left
			return '\u2190';
		case 205: // Arrow Right
			return '\u2192';
		case 207: // End
			return '\u21E5';
		case 208: // Arrow Down
			return '\u2193';
		case 209: // Next
			return '\u21E9';
		default:
			return character;
		}
	}

	public VirtualKeyboardEvent decodeUnicode(char character) {
		switch (character) {
		case '\b':
			return new VirtualKeyboardEvent(14, true, character);
		case '\u21A6':
			return new VirtualKeyboardEvent(15, true, Character.MIN_VALUE);
		case '\u2907':
			return new VirtualKeyboardEvent(15, false, Character.MIN_VALUE);
		case '\u21E4':
			return new VirtualKeyboardEvent(199, true, Character.MIN_VALUE);
		case '\u2191':
			return new VirtualKeyboardEvent(200, true, Character.MIN_VALUE);
		case '\u21E7':
			return new VirtualKeyboardEvent(201, true, Character.MIN_VALUE);
		case '\u2190':
			return new VirtualKeyboardEvent(203, true, Character.MIN_VALUE);
		case '\u2192':
			return new VirtualKeyboardEvent(205, true, Character.MIN_VALUE);
		case '\u21E5':
			return new VirtualKeyboardEvent(207, true, Character.MIN_VALUE);
		case '\u2193':
			return new VirtualKeyboardEvent(208, true, Character.MIN_VALUE);
		case '\u21E9':
			return new VirtualKeyboardEvent(209, true, Character.MIN_VALUE);
		default:
			return new VirtualKeyboardEvent(0, true, character);
		}
	}

	public boolean isUnicodeInList(int keycode) {
		switch (keycode) {
		case 14:
		case 15:
		case 199:
		case 200:
		case 201:
		case 203:
		case 205:
		case 207:
		case 208:
		case 209:
			return true;
		default:
			return false;
		}
	}

	@Override
	public VirtualKeyboard clone() {
		return new VirtualKeyboard(keyList, charList);
	}

	@Override
	public String toString() {
		List<String> stringy = getCurrentPresses();
		String keyString = "";
		if (!stringy.isEmpty()) {
			String seperator = ",";
			for (int i = 0; i < stringy.size(); i++) {
				if (i == stringy.size() - 1) {
					seperator = "";
				}
				keyString = keyString.concat(stringy.get(i) + seperator);
			}
		}
		String charString = "";
		if (!charList.isEmpty()) {
			for (int i = 0; i < charList.size(); i++) {
				charString = charString.concat(Character.toString(charList.get(i)));
			}
			charString = StringUtils.replace(charString, "\r", "\\n");
			charString = StringUtils.replace(charString, "\n", "\\n");
		}
		
		return "Keyboard:"+keyString + ";" + charString;
	}
}
