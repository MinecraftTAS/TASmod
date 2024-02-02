package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

/**
 * Stores keyboard specific values in a given timeframe.<br>
 * <br>
 * This keyboard mimics the {@link org.lwjgl.input.Keyboard} Minecraft is using.
 * <h2>KeyboardEvent</h2>
 * {@link org.lwjgl.input.Keyboard} has the following outputs, when a key is pressed or unpressed on the <em>physical</em> keyboard:
 * <ul>
 *     <li>int <strong>KeyCode</strong>: The unique keycode of the key</li>
 *     <li>boolean <strong>KeyState</strong>: The new state of the key. True for pressed, false for unpressed</li>
 *     <li>char <strong>KeyCharacter</strong>: The character associated for each key</li>
 * </ul>
 * While the keycode is the same between <em>physical</em> keyboards, the key character might differ.<br>
 * It is also common that one keycode has multiple characters associated with it, e.g. <br>
 * holding shift results in a capitalised character.<br>
 * <br>
 * These three outputs together are what we call a "KeyboardEvent" and might look like this:
 * <pre>
 *     17, true, w
 * </pre>
 * For <code>keycode, keystate, keycharacter</code>
 * <h2>Updating the keyboard</h2>
 * This keyboard stores it's values in "states".<br>
 * That means that all the keys that are currently pressed are stored in {@link #pressedKeys}.<br>
 * And this list is updated via a keyboard event in {@link #update(int, boolean, char)}.<br>
 * <h2>Difference</h2>
 * When comparing 2 keyboard states, we can generate a list of differences from them in form of {@link VirtualKeyboardEvent}s.<br>
 * <pre>
 * 	this: W A S
 *	next: W   S D
 * </pre>
 * Since there are 2 differences between this and the next keyboard,
 * this will result in 2 {@link VirtualKeyboardEvent}s. And combined with the {@link #charList} we can also get the associated characters:
 * <pre>
 *	30, false, null // A is unpressed
 * 	32, true, d 	// D is pressed
 * </pre>
 * <h2>Subticks</h2>
 * Minecraft updates it's keyboard every tick. All the key events that occur inbetween are stored,<br>
 * then read out when a new tick has started.<br> We call these "inbetween" ticks <em>subticks</em>.<br>
 * <h3>Parent->Subtick</h3>
 * In a previous version of this keyboard, subticks were bundeled and flattened into one keyboard state.<br>
 * After all, Minecraft updates only occur once every tick, storing subticks seemed unnecessary.<br>
 * <br>
 * However this posed some usability issues when playing in a low game speed via {@link com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient}.<br>
 * Now you had to hold the key until the next tick to get it recognised by the game.<br>
 * <br>
 * To fix this, now every subtick is stored as a keyboard state as well.<br>
 * When updating the keyboard in {@link #update(int, boolean, char)}, a clone of itself is created and stored in {@link #subtickList},<br>
 * with the difference that the subtick state has no {@link #subtickList}.<br>
 * In a nutshell, the keyboard stores it's past changes in {@link #subtickList} with the first being the oldest change.
 *
 * @author Scribble
 * @see com.minecrafttas.tasmod.virtual.VirtualInput2.VirtualKeyboardInput
 */
public class VirtualKeyboard2 extends VirtualPeripheral<VirtualKeyboard2> implements Serializable {

    /**
     * The list of characters that were pressed on this keyboard.
     */
    private final List<Character> charList;

    /**
     * A queue of characters used in {@link #getDifference(VirtualKeyboard2, Queue)}.<br>
     * Used for distributing characters to {@link VirtualKeyboardEvent}s in an order.
     */
    private final ConcurrentLinkedQueue<Character> charQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Creates an empty parent keyboard with all keys unpressed
     */
    public VirtualKeyboard2() {
        this(new HashSet<>(), new ArrayList<>(), new ArrayList<>(), true);
    }

    /**
     * Creates a subtick keyboard with {@link VirtualPeripheral#subtickList} uninitialized
     * @param pressedKeys The new list of pressed keycodes for this subtickKeyboard
     * @param charList A list of characters for this subtickKeyboard
     */
    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList){
        this(pressedKeys, charList, null, false);
    }

    /**
     * Creates a keyboard from existing variables
     * @param pressedKeys The existing list of pressed keycodes
     * @param charList The existing list of characters
     */
	public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList, boolean ignoreFirstUpdate) {
		this(pressedKeys, charList, null, ignoreFirstUpdate);
	}

    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList, List<VirtualKeyboard2> subtick, boolean ignoreFirstUpdate) {
        super(pressedKeys, subtick, ignoreFirstUpdate);
        this.charList = charList;
    }
    
    /**
     * Updates the keyboard, adds a new subtick to this keyboard
     * @param keycode The keycode of this key
     * @param keystate The keystate of this key, true for pressed
     * @param keycharacter The character that is associated with that key. Can change between keyboards or whenever shift is held in combination.
     */
    public void update(int keycode, boolean keystate, char keycharacter) {
    	if(isParent() && !ignoreFirstUpdate()) {
    		addSubtick(clone());
    	}
    	charList.clear();
    	setPressed(keycode, keystate);
    	addChar(keycharacter);
    }
    
    @Override
    public void setPressed(int keycode, boolean keystate) {
        if (keycode >= 0) {    // Keyboard keys always have a keycode larger or equal than 0
            super.setPressed(keycode, keystate);
        }
    }

	/**
	 * Calculates the difference between 2 keyboards via symmetric difference <br>
	 * and returns a list of the changes between them in form of {@link VirtualKeyboardEvent}s
	 *
	 * @param nextKeyboard The keyboard that is comes after this one.<br>
	 *                       If this one is loaded at tick 15, the nextPeripheral
	 *                       should be the one from tick 16
	 * @param reference The queue to fill. Passed in by reference.
	 */
    public void getDifference(VirtualKeyboard2 nextKeyboard, Queue<VirtualKeyboardEvent> reference) {

        charQueue.addAll(nextKeyboard.charList);

        /* Calculate symmetric difference of keycodes */

		/*
		    Calculate unpressed keys
		    this: W A S
		    next: W   S D
		    -------------
		            A     <- unpressed
		 */
        this.pressedKeys.forEach(key -> {
            if (!nextKeyboard.getPressedKeys().contains(key)) {
            	reference.add(new VirtualKeyboardEvent(key, false, Character.MIN_VALUE));
            }
        });

		/*
		 	Calculate pressed keys
		 	next: W   S D
		 	this: W A S
		 	-------------
		 	            D <- pressed
		 */
        nextKeyboard.getPressedKeys().forEach(key -> {
            if (!this.pressedKeys.contains(key)) {
            	reference.add(new VirtualKeyboardEvent(key, true, getOrMinChar(charQueue.poll())));
            }
        });

		/*
			Add the rest of the characters as keyboard events.
			This may happen when specifying more chars than keycodes
			int the TASFile:

			Keyboard:H,E,L;Hello|

			This makes it easier to write words when working only with the TASfile,
			otherwise you'd either need to add a keycode for each char or write it in new lines
		 */
        while (!charQueue.isEmpty()) {
        	reference.add(new VirtualKeyboardEvent(0, false, getOrMinChar(charQueue.poll())));
        }

    }
    
    private char getOrMinChar(Character charr){
        if(charr==null){
            charr = Character.MIN_VALUE;
        }
        return charr;
    }

	/**
	 * Calculates a list of {@link VirtualKeyboardEvent}s to the next peripheral, including
	 * the subticks.
	 * 
	 * @see VirtualKeyboard2#getDifference(VirtualKeyboard2, Queue)
	 * 
	 * @param nextKeyboard The peripheral that is comes after this one.<br>
	 *                       If this one is loaded at tick 15, the nextPeripheral
	 *                       should be the one from tick 16
	 * @param reference The queue to fill. Passed in by reference.
	 */
	public void getVirtualEvents(VirtualKeyboard2 nextKeyboard, Queue<VirtualKeyboardEvent> reference) {
		if (isParent()) {
			VirtualKeyboard2 currentSubtick = this;
			for(VirtualKeyboard2 subtick : nextKeyboard.getAll()) {
				currentSubtick.getDifference(subtick, reference);
				currentSubtick = subtick;
			}
		}
	}
    
    /**
     * Add a character to the {@link #charList}<br>
     * Null characters will be discarded;
     * @param character The character to add
     */
    public void addChar(char character) {
        if(character != Character.MIN_VALUE)
            charList.add(character);
    }

    @Override
    protected void clear(){
    	super.clear();
        charList.clear();
    }
    
	@Override
	public String toString() {
		if (isParent()) {
			return getAll().stream().map(VirtualKeyboard2::toStringWithCharlist).collect(Collectors.joining("\n"));
		} else {
			return toStringWithCharlist();
		}
	}

	private String toStringWithCharlist(){
		return String.format("%s;%s", super.toString(), charListToString(charList));
	}

	private String charListToString(List<Character> charList) {
		String charString = "";
		if (!charList.isEmpty()) {
			charString = charList.stream().map(Object::toString).collect(Collectors.joining());
			charString = StringUtils.replace(charString, "\r", "\\n");
			charString = StringUtils.replace(charString, "\n", "\\n");
		}
		return charString;
	}

	/**
	 * Clones this VirtualKeyboard <strong>without</strong> subticks
	 */
    @Override
	public VirtualKeyboard2 clone() {
        return new VirtualKeyboard2(new HashSet<>(this.pressedKeys), new ArrayList<>(this.charList), isIgnoreFirstUpdate());
    }
    

    @Override
    public void copyFrom(VirtualKeyboard2 keyboard) {
    	super.copyFrom(keyboard);
    	charList.clear();
    	charList.addAll(keyboard.charList);
    }
    
    public List<Character> getCharList() {
        return ImmutableList.copyOf(charList);
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof VirtualKeyboard2) {
    		VirtualKeyboard2 keyboard = (VirtualKeyboard2) obj;
    		
    		if(charList.size() != keyboard.charList.size()) {
    			return false;
    		}
    		
    		for (int i = 0; i < charList.size(); i++) {
				if(charList.get(i)!=keyboard.charList.get(i)) {
					return false;
				}
			}
    		return super.equals(obj);
    	}
    	return super.equals(obj);
    }
}
