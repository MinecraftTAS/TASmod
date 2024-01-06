package com.minecrafttas.tasmod.virtual;

public class VirtualKeyboardEvent extends VirtualEvent {
    private final Character character;

    public VirtualKeyboardEvent(int keycode, boolean keystate, Character character) {
        super(keycode, keystate);
        this.character = character;
    }

    public VirtualKeyboardEvent(VirtualEvent event, char character) {
        super(event);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", super.toString(), character != null ? character : Character.MIN_VALUE);
    }
}
