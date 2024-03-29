package com.minecrafttas.tasmod.virtual.event;

/**
 * Template for recording {@link org.lwjgl.input.Keyboard#next()} events.
 *
 * @author Scribble
 */
public class VirtualKeyboardEvent extends VirtualEvent {
    private final char character;

    public VirtualKeyboardEvent(){
        this(0, false, Character.MIN_VALUE);
    }

    public VirtualKeyboardEvent(int keycode, boolean keystate, char character) {
        super(keycode, keystate);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", super.toString(), character);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VirtualKeyboardEvent){
            VirtualKeyboardEvent e = (VirtualKeyboardEvent) obj;
            return keycode == e.keycode && keystate == e.keystate && character == e.character;
        }
        return super.equals(obj);
    }
}
