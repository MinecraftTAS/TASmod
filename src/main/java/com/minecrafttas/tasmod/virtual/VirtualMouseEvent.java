package com.minecrafttas.tasmod.virtual;

import com.minecrafttas.tasmod.virtual.VirtualEvent.VirtualButtonEvent;

/**
 * Template for recording Mouse.next() events.
 *
 * @author Scribble
 */
public class VirtualMouseEvent extends VirtualButtonEvent {
    private final int scrollwheel;
    private final Integer mouseX;
    private final Integer mouseY;

    public VirtualMouseEvent(){
        this(0, false, 0, null, null);
    }
    public VirtualMouseEvent(int keycode, boolean state, int scrollwheel, Integer mouseX, Integer mouseY) {
        super(keycode, state);
        this.scrollwheel = scrollwheel;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getScrollwheel() {
        return scrollwheel;
    }

    public Integer getMouseX() {
        return mouseX;
    }

    public Integer getMouseY() {
        return mouseY;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", super.toString(), scrollwheel, mouseX != null ? mouseX : " ", mouseY != null ? mouseY : " ");
    }
}
