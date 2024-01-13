package com.minecrafttas.tasmod.virtual;

import com.minecrafttas.tasmod.virtual.VirtualEvent.VirtualButtonEvent;

public class VirtualKeyboardEvent extends VirtualButtonEvent {
    private final char character;

    public VirtualKeyboardEvent(int keycode, boolean keystate, char character) {
        super(keycode, keystate);
        this.character = character;
    }

    public VirtualKeyboardEvent(VirtualButtonEvent event, char character) {
        super(event);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", super.toString(), character);
    }
}
