package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VirtualKeyboard2 extends VirtualPeripheral implements Serializable {

    private List<Character> charList;

    @Override
    public void setPressed(int keycode, boolean keystate) {
        if (keycode >= 0) {    // Keyboard keys always have a keycode larger or equal than 0
            super.setPressed(keycode, keystate);
        }
    }

    @Override
    protected <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral) {
        List<VirtualKeyboardEvent> eventList = new ArrayList<>();

        ConcurrentLinkedQueue<Character> chars = new ConcurrentLinkedQueue<>(charList);

        /* Calculate symmetric difference */

        /*
         * Calculate unpressed keys
         * this: W A S
         * next: W   S D
         * -------------
         *         A     <- unpressed
         * */
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
                eventList.add(new VirtualKeyboardEvent(key, true, chars.poll()));
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
        while (!chars.isEmpty()) {
            eventList.add(new VirtualKeyboardEvent(VirtualKey2.ZERO.getKeycode(), true, chars.poll()));
        }

        return eventList;
    }

    public void addChar(char character) {
        charList.add(character);
    }

    private void clearCharacters() {
        charList.clear();
    }

    public void clear() {
        super.clearPressedKeys();
        clearCharacters();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
