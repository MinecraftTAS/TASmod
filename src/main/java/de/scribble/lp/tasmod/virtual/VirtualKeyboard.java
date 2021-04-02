package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class VirtualKeyboard {
	
	private Map<Integer, VirtualKeyboardKey> keyList;
	
	private List<Character> charList;
	
	/**
	 * Creates a copy of the virtual keyboard with the given key list
	 * @param keyListIn
	 */
	public VirtualKeyboard(Map<Integer, VirtualKeyboardKey> keyListIn, List<Character> charListIn){
		Map<Integer, VirtualKeyboardKey> copy=new HashMap<Integer, VirtualKeyboardKey>();
		
		keyListIn.forEach((key,value)->{
			copy.put(key, value.clone());
		});
		keyList=copy;
		
		List<Character> charCopy=new ArrayList<Character>();
		
		charListIn.forEach(charAction->{
			charCopy.add(charAction);
		});
		charList=charCopy;
	}
	
	/**
	 * Creates a Keyboard, where the keys are all unpressed
	 */
	public VirtualKeyboard() {
		charList=new ArrayList<Character>();
		
		keyList = Maps.<Integer, VirtualKeyboardKey>newHashMap();
		keyList.put(0, new VirtualKeyboardKey("0", 0));
		keyList.put(1, new VirtualKeyboardKey("ESC", 1));
		keyList.put(2, new VirtualKeyboardKey("KEY_1", 2));
		keyList.put(3, new VirtualKeyboardKey("KEY_2", 3));
		keyList.put(4, new VirtualKeyboardKey("KEY_3", 4));
		keyList.put(5, new VirtualKeyboardKey("KEY_4", 5));
		keyList.put(6, new VirtualKeyboardKey("KEY_5", 6));
		keyList.put(7, new VirtualKeyboardKey("KEY_6", 7));
		keyList.put(8, new VirtualKeyboardKey("KEY_7", 8));
		keyList.put(9, new VirtualKeyboardKey("KEY_8", 9));
		keyList.put(10, new VirtualKeyboardKey("KEY_9", 10));
		keyList.put(11, new VirtualKeyboardKey("KEY_0", 11));
		keyList.put(12, new VirtualKeyboardKey("MINUS", 12));
		keyList.put(13, new VirtualKeyboardKey("EQUALS", 13));
		keyList.put(14, new VirtualKeyboardKey("BACK", 14));
		keyList.put(15, new VirtualKeyboardKey("TAB", 15));
		keyList.put(16, new VirtualKeyboardKey("Q", 16));
		keyList.put(17, new VirtualKeyboardKey("W", 17));
		keyList.put(18, new VirtualKeyboardKey("E", 18));
		keyList.put(19, new VirtualKeyboardKey("R", 19));
		keyList.put(20, new VirtualKeyboardKey("T", 20));
		keyList.put(21, new VirtualKeyboardKey("Y", 21));
		keyList.put(22, new VirtualKeyboardKey("U", 22));
		keyList.put(23, new VirtualKeyboardKey("I", 23));
		keyList.put(24, new VirtualKeyboardKey("O", 24));
		keyList.put(25, new VirtualKeyboardKey("P", 25));
		keyList.put(26, new VirtualKeyboardKey("LBRACKET", 26));
		keyList.put(27, new VirtualKeyboardKey("RBRACKET", 27));
		keyList.put(28, new VirtualKeyboardKey("RETURN", 28));
		keyList.put(29, new VirtualKeyboardKey("LCONTROL", 29));
		keyList.put(30, new VirtualKeyboardKey("A", 30));
		keyList.put(31, new VirtualKeyboardKey("S", 31));
		keyList.put(32, new VirtualKeyboardKey("D", 32));
		keyList.put(33, new VirtualKeyboardKey("F", 33));
		keyList.put(34, new VirtualKeyboardKey("G", 34));
		keyList.put(35, new VirtualKeyboardKey("H", 35));
		keyList.put(36, new VirtualKeyboardKey("J", 36));
		keyList.put(37, new VirtualKeyboardKey("K", 37));
		keyList.put(38, new VirtualKeyboardKey("L", 38));
		keyList.put(39, new VirtualKeyboardKey("SEMICOLON", 39));
		keyList.put(40, new VirtualKeyboardKey("APOSTROPHE", 40));
		keyList.put(41, new VirtualKeyboardKey("GRAVE", 41));
		keyList.put(42, new VirtualKeyboardKey("LSHIFT", 42));
		keyList.put(43, new VirtualKeyboardKey("BACKSLASH", 43));
		keyList.put(44, new VirtualKeyboardKey("Z", 44));
		keyList.put(45, new VirtualKeyboardKey("X", 45));
		keyList.put(46, new VirtualKeyboardKey("C", 46));
		keyList.put(47, new VirtualKeyboardKey("V", 47));
		keyList.put(48, new VirtualKeyboardKey("B", 48));
		keyList.put(49, new VirtualKeyboardKey("N", 49));
		keyList.put(50, new VirtualKeyboardKey("M", 50));
		keyList.put(51, new VirtualKeyboardKey("COMMA", 51));
		keyList.put(52, new VirtualKeyboardKey("PERIOS", 52));
		keyList.put(53, new VirtualKeyboardKey("SLASH", 53));
		keyList.put(54, new VirtualKeyboardKey("RSHIFT", 54));
		keyList.put(55, new VirtualKeyboardKey("MULTIPLY", 55));
		keyList.put(56, new VirtualKeyboardKey("ALT", 56));
		keyList.put(57, new VirtualKeyboardKey("SPACE", 57));
		keyList.put(58, new VirtualKeyboardKey("CAPSLOCK", 58));
		keyList.put(59, new VirtualKeyboardKey("F1", 59));
		keyList.put(60, new VirtualKeyboardKey("F2", 60));
		keyList.put(61, new VirtualKeyboardKey("F3", 61));
		keyList.put(62, new VirtualKeyboardKey("F4", 62));
		keyList.put(63, new VirtualKeyboardKey("F5", 63));
		keyList.put(64, new VirtualKeyboardKey("F6", 64));
		keyList.put(65, new VirtualKeyboardKey("F7", 65));
		keyList.put(66, new VirtualKeyboardKey("F8", 66));
		keyList.put(67, new VirtualKeyboardKey("F9", 67));
		keyList.put(68, new VirtualKeyboardKey("F10", 68));
		keyList.put(69, new VirtualKeyboardKey("NUMLOCK", 69));
		keyList.put(70, new VirtualKeyboardKey("SCROLL", 70));
		keyList.put(71, new VirtualKeyboardKey("NUMPAD7", 71));
		keyList.put(72, new VirtualKeyboardKey("NUMPAD8", 72));
		keyList.put(73, new VirtualKeyboardKey("NUMPAD9", 73));
		keyList.put(74, new VirtualKeyboardKey("SUBTRACT", 74));
		keyList.put(75, new VirtualKeyboardKey("NUMPAD4", 75));
		keyList.put(76, new VirtualKeyboardKey("NUMPAD5", 76));
		keyList.put(77, new VirtualKeyboardKey("NUMPAD6", 77));
		keyList.put(78, new VirtualKeyboardKey("ADD", 78));
		keyList.put(79, new VirtualKeyboardKey("NUMPAD1", 79));
		keyList.put(80, new VirtualKeyboardKey("NUMPAD2", 80));
		keyList.put(81, new VirtualKeyboardKey("NUMPAD3", 81));
		keyList.put(82, new VirtualKeyboardKey("NUMPAD0", 82));
		keyList.put(83, new VirtualKeyboardKey("DECIMAL", 83));
		keyList.put(87, new VirtualKeyboardKey("F11", 87));
		keyList.put(88, new VirtualKeyboardKey("F12", 88));
		keyList.put(100, new VirtualKeyboardKey("F13", 100));
		keyList.put(101, new VirtualKeyboardKey("F14", 101));
		keyList.put(102, new VirtualKeyboardKey("F15", 102));
		keyList.put(103, new VirtualKeyboardKey("F16", 103));
		keyList.put(104, new VirtualKeyboardKey("F17", 104));
		keyList.put(105, new VirtualKeyboardKey("F18", 105));
		keyList.put(112, new VirtualKeyboardKey("KANA", 112));
		keyList.put(113, new VirtualKeyboardKey("F19", 113));
		keyList.put(121, new VirtualKeyboardKey("CONVERT", 121));
		keyList.put(123, new VirtualKeyboardKey("NOCONVERT", 123));
		keyList.put(125, new VirtualKeyboardKey("YEN", 125));
		keyList.put(141, new VirtualKeyboardKey("NUMPADEQUALS", 141));
		keyList.put(144, new VirtualKeyboardKey("CIRCUMFLEX", 144));
		keyList.put(145, new VirtualKeyboardKey("AT", 145));
		keyList.put(146, new VirtualKeyboardKey("COLON", 146));
		keyList.put(147, new VirtualKeyboardKey("UNDERLINE", 147));
		keyList.put(148, new VirtualKeyboardKey("KANJI", 148));
		keyList.put(149, new VirtualKeyboardKey("STOP", 149));
		keyList.put(156, new VirtualKeyboardKey("NUMPADENTER", 156));
		keyList.put(157, new VirtualKeyboardKey("RCONTROL", 157));
		keyList.put(179, new VirtualKeyboardKey("NUMPADCOMMA", 179));
		keyList.put(181, new VirtualKeyboardKey("DIVIDE", 181));
		keyList.put(183, new VirtualKeyboardKey("PRINT", 183));
		keyList.put(184, new VirtualKeyboardKey("ALT_GR", 184));
		keyList.put(197, new VirtualKeyboardKey("PAUSE", 197));
		keyList.put(199, new VirtualKeyboardKey("HOME", 199));
		keyList.put(200, new VirtualKeyboardKey("UP", 200));
		keyList.put(201, new VirtualKeyboardKey("PRIOR", 201));
		keyList.put(203, new VirtualKeyboardKey("LEFT", 203));
		keyList.put(205, new VirtualKeyboardKey("RIGHT", 205));
		keyList.put(207, new VirtualKeyboardKey("END", 207));
		keyList.put(208, new VirtualKeyboardKey("DOWN", 208));
		keyList.put(209, new VirtualKeyboardKey("NEXT", 209));
		keyList.put(210, new VirtualKeyboardKey("INSERT", 210));
		keyList.put(211, new VirtualKeyboardKey("DELETE", 211));
		keyList.put(219, new VirtualKeyboardKey("WIN", 219));
		keyList.put(221, new VirtualKeyboardKey("CONTEXT_MENU", 221));
	}
	
	public void add(int keycode) {
		String keyString= Integer.toString(keycode);
		
		keyList.put(keycode, new VirtualKeyboardKey(keyString, keycode));
	}
	
	public VirtualKeyboardKey get(int keycode) {
		VirtualKeyboardKey key=keyList.get(keycode);
		if(key==null) {
			add(keycode);
			return keyList.get(keycode);
		}
		else return key;
	}
	
	public VirtualKeyboardKey get(String keyname) {
		Collection<VirtualKeyboardKey> list=keyList.values();
		VirtualKeyboardKey out=null;
		
		for (VirtualKeyboardKey key: list) {
			if(key.getName().equalsIgnoreCase(keyname)) {
				out=key;
			}
		}
		return out;
	}
	
	public List<String> getCurrentPresses(){
		List<String> out=new ArrayList<String>();
		
		keyList.forEach((keycodes, virtualkeys)->{
			if(keycodes>=0) {
				if(virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});
		
		return out;
	}
	
	public Map<Integer, VirtualKeyboardKey> getKeyList() {
		return this.keyList;
	}
	
	public void addChar(char charin) {
		charList.add(charin);
	}
	
	public List<Character> getCharList(){
		return charList;
	}
	
	public void clearCharList() {
		charList.clear();
	}
	
	public void clear() {
		keyList.forEach((keycode,key)->{
			key.setPressed(false);
		});
		charList.clear();
	}
	
	public List<VirtualKeyboardEvent> getDifference(VirtualKeyboard keyboardToCompare){
		
		List<VirtualKeyboardEvent> eventList=new ArrayList<VirtualKeyboardEvent>();
		
		Iterator<Character> charIterator= keyboardToCompare.charList.iterator();
		
		keyList.forEach((keycodes, virtualkeys)->{
			
			VirtualKeyboardKey keyToCompare=keyboardToCompare.get(keycodes);
			
			if(!virtualkeys.equals(keyToCompare)) {
				
				if(charIterator.hasNext()) {
					eventList.add(new VirtualKeyboardEvent(keycodes, keyToCompare.isKeyDown(), charIterator.next()));
				}else {				
					eventList.add(new VirtualKeyboardEvent(keycodes, keyToCompare.isKeyDown(), Character.MIN_VALUE));
				}
			}
			
		});
		
		return eventList;
	}
	
	@Override
	public VirtualKeyboard clone() throws CloneNotSupportedException {
		return new VirtualKeyboard(keyList, charList);
	}
	
}
