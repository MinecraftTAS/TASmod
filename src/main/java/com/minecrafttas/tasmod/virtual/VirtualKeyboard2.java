package com.minecrafttas.tasmod.virtual;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 * then the resulting keyboard event would hold a capitalized "W" as the character.<br>
 * <br>
 *
 */
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
     * A list of subtick keyboards.<br>
     * If subtickKeyboards is initialized, the object can be considered as a <em>parent</em> keyboard,<br>
     * able to house subtickKeyboards.<br>
     * <br>
     * If subtickKeyboards is null then the object is a "child"/subtickKeyboard stored in a parent list
     */
    private final List<VirtualKeyboard2> subtickKeyboards;

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
    public VirtualKeyboard2(Set<Integer> pressedKeys, List<Character> charList, List<VirtualKeyboard2> subtickKeyboards) {
        super(pressedKeys);
        this.charList = charList;
        this.subtickKeyboards = subtickKeyboards;
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
        if(subtickKeyboards == null){
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
        else {
            String charString = "";
            if (!charList.isEmpty()) {
                for (Character charr : charList) {
                    charString = charString.concat(Character.toString(charr));
                }
                charString = StringUtils.replace(charString, "\r", "\\n");
                charString = StringUtils.replace(charString, "\n", "\\n");
            }

            String out = String.format("%s;%s", super.toString(), charString);
            for(VirtualKeyboard2 child : subtickKeyboards){
                out=out.concat("\n\t"+child.toString());
            }
            return out;
        }
    }

    @Override
    protected VirtualKeyboard2 clone() {
        return new VirtualKeyboard2(this.pressedKeys, this.charList);
    }

    public boolean isParent(){
        return subtickKeyboards!=null;
    }

    public List<Character> getCharList() {
        return ImmutableList.copyOf(charList);
    }
}
