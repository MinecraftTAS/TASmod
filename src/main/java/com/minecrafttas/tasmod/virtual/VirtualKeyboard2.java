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
 * A state of the virtual keyboard at a given time.<br>
 * <br>
 * This class can be seen as a storage class,<br>
 * representing a state of the physical keyboard at a given time.<br>
 * <br>
 * This class aims to store the currently pressed keys and the list of characters that were entered in that time frame.<br>
 * <br>
 * A keyboard event coming from the physical keyboard may look like this:<br>
 * <br>
 * <pre>
 * Keycode:17,Keystate:true,Character:w
 * </pre>
 * This indicates that the key "W" is currently pressed.<br>
 * Therefore, the keycode 17 will be added to the set of {@link #pressedKeys} in {@link VirtualPeripheral}.<br>
 * This behaviour is also present in the {@link VirtualMouse2}.<br>
 * <br>
 * The character "w" coming from that keyboard event is added to the {@link #charList}.<br>
 * If shift were to be pressed while pressing the key,<br>
 * then the resulting keyboard event would hold a capitalized "W" as the character.
 * TODO Write Difference and VirtualKey once complete
 * 
 * @author Scribble
 */
public class VirtualKeyboard2 extends VirtualPeripheral<VirtualKeyboard2> implements Serializable {

    /**
     * The list of characters that were pressed on this keyboard.
     */
    private final List<Character> charList;

    /**
     * A queue of characters used in {@link #getDifference(VirtualPeripheral)}.<br>
     * Used for distributing characters to {@link VirtualKeyboardEvent}s in an order.
     */
    private final ConcurrentLinkedQueue<Character> charQueue = new ConcurrentLinkedQueue<>();

    /**
     * Creates an empty parent keyboard with all keys unpressed
     */
    public VirtualKeyboard2() {
        this(new HashSet<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates a subtick keyboard with {@link #subtickKeyboards} uninitialized
     * @param pressedKeys The new list of pressed keycodes for this subtickKeyboard
     * @param charList A list of characters for this subtickKeyboard
     */
    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList){
        this(pressedKeys, charList, null);
    }

    /**
     * Creates a keyboard from existing variables
     * @param pressedKeys The existing list of pressed keycodes
     * @param charList The existing list of characters
     */
    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList, List<VirtualKeyboard2> subtick) {
        super(pressedKeys, subtick);
        this.charList = charList;
    }

    /**
     * Updates the keyboard, adds a new subtick to this keyboard
     * @param keycode The keycode of this key
     * @param keystate The keystate of this key, true for pressed
     * @param character The character that is associated with that key. Can change between keyboards or whenever shift is held in combination.
     */
    public void update(int keycode, boolean keystate, char character) {
    	setPressed(keycode, keystate);
    	addChar(character);
    	
    	if(isParent()) {
    		addSubtick(clone());
    	}
    }
    
    @Override
    public void setPressed(int keycode, boolean keystate) {
        if (keycode >= 0) {    // Keyboard keys always have a keycode larger or equal than 0
            super.setPressed(keycode, keystate);
        }
    }

    @Override
    protected Queue<VirtualKeyboardEvent> getDifference(VirtualKeyboard2 nextPeripheral) {
        Queue<VirtualKeyboardEvent> eventList = new ConcurrentLinkedQueue<>();

        charQueue.addAll(charList);

        /* Calculate symmetric difference of keycodes */

        /*
            Calculate unpressed keys
            this: W A S
            next: W   S D
            -------------
                    A     <- unpressed
         */
        this.pressedKeys.forEach(key -> {
            if (!nextPeripheral.getPressedKeys().contains(key)) {
                eventList.add(new VirtualKeyboardEvent(key, false, Character.MIN_VALUE));
            }
        });

		/*
		 	Calculate pressed keys
		 	next: W   S D
		 	this: W A S
		 	-------------
		 	            D <- pressed
		 */
        nextPeripheral.getPressedKeys().forEach(key -> {
            if (!this.pressedKeys.contains(key)) {
                eventList.add(new VirtualKeyboardEvent(key, true, getOrDefault(charQueue.poll())));
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
            eventList.add(new VirtualKeyboardEvent(0, false, getOrDefault(charQueue.poll())));
        }

        return eventList;
    }

    @Override
    protected Queue<? extends VirtualEvent> getVirtualEvents(VirtualKeyboard2 nextPeripheral) {
    	Queue<VirtualKeyboardEvent> eventList = new ConcurrentLinkedQueue<>();
    	
    	getSubticks().forEach(keyboard -> {
    		eventList.addAll(keyboard.getDifference(nextPeripheral));
    	});
    	
    	return eventList;
    }
    
    private char getOrDefault(Character charr){
        if(charr==null){
            charr = Character.MIN_VALUE;
        }
        return charr;
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

    public void clearCharList(){
        charList.clear();
    }

	@Override
	public String toString() {
		if (isParent()) {
			return getSubticks().stream().map(element -> element.toString()).collect(Collectors.joining("\n"));
		} else {
			return String.format("%s;%s", super.toString(), charListToString(charList));
		}
	}
	
	private String charListToString(List<Character> charList) {
		String charString = "";
		if (!charList.isEmpty()) {
			charString = charList.stream().map(element -> element.toString()).collect(Collectors.joining());
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
        return new VirtualKeyboard2(this.pressedKeys, this.charList);
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
