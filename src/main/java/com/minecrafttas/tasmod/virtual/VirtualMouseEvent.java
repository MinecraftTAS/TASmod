package com.minecrafttas.tasmod.virtual;

/**
 * Template for recording Mouse.next() events.
 *
 * @author Scribble
 */
public class VirtualMouseEvent extends VirtualEvent {
    private int scrollwheel;
    private int mouseX;
    private int mouseY;

    public VirtualMouseEvent(int keycode, boolean state, int scrollwheel, int mouseX, int mouseY) {
        super(keycode, state);
        this.scrollwheel = scrollwheel;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getScrollwheel() {
        return scrollwheel;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", super.toString(), scrollwheel, mouseX, mouseY);
    }
}
