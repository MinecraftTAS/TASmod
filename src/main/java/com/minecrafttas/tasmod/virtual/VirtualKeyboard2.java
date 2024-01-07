package com.minecrafttas.tasmod.virtual;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VirtualKeyboard2 extends VirtualPeripheral implements Serializable {

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
     * Creates a keyboard with all keys unpressed
     */
    public VirtualKeyboard2() {
        super();
        this.charList = new ArrayList<>();
    }

    /**
     * Creates a keyboard from existing variables
     * @param pressedKeys The existing list of pressed keycodes
     * @param charList The existing list of characters
     */
    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList) {
        super(pressedKeys);
        this.charList = charList;
    }

    @Override
    public void setPressed(int keycode, boolean keystate) {
        if (keycode >= 0) {    // Keyboard keys always have a keycode larger or equal than 0
            super.setPressed(keycode, keystate);
        }
    }

    @Override
    protected <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral) {
        List<VirtualKeyboardEvent> eventList = new ArrayList<>();

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
            if (!nextPeripheral.pressedKeys.contains(key)) {
                eventList.add(new VirtualKeyboardEvent(key, false, Character.MIN_VALUE));
            }
        });

		/*
		 	Calculate pressed keys
		 	next: W   S D
		 	this: W A S
		 	-------------
		 	            D <-     pressed
		 */
        nextPeripheral.pressedKeys.forEach(key -> {
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

    @Override
    public String toString() {
        String charString = "";
        if (!charList.isEmpty()) {
            for (Character charr : charList) {
                charString = charString.concat(Character.toString(charr));
            }
            charString = StringUtils.replace(charString, "\r", "\\n");
            charString = StringUtils.replace(charString, "\n", "\\n");
        }

        return String.format("%s;%s", super.toString(), charString);
    }

    @Override
    protected VirtualKeyboard2 clone() throws CloneNotSupportedException {
        return new VirtualKeyboard2(this.pressedKeys, this.charList);
    }

}
