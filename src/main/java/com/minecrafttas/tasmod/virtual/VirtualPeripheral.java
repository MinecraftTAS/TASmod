package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for {@link VirtualKeyboard2} and {@link VirtualMouse2}<br>
 * <br>
 * Contains the shared code for keeping track of which buttons are pressed.<br>
 * This works by storing the keycodes of the buttons in a set, as keycodes are supposed to be unique<br>
 * <br>
 * Generating {@link VirtualEvent}s is handled in the child classes.
 *
 * @author Scribble
 */
public abstract class VirtualPeripheral implements Serializable {

    /**
     * The list of keycodes that are currently pressed on this peripheral.
     */
    protected final Set<Integer> pressedKeys;

    /**
     * Create an empty peripheral with all keys unpressed
     */
    protected VirtualPeripheral() {
        this.pressedKeys = new HashSet<>();
    }

    /**
     * Create a peripheral with already existing pressed keys
     * @param pressedKeys The existing pressedKeys
     */
    protected VirtualPeripheral(Set<Integer> pressedKeys) {
        this.pressedKeys = pressedKeys;
    }

    /**
     * Set the specified keycode to pressed
     * @param keycode The keycode to check
     * @param keystate The keystate of the keycode
     */
    protected void setPressed(int keycode, boolean keystate) {
        if (keystate)
            pressedKeys.add(keycode);
        else
            pressedKeys.remove(keycode);
    }

    /**
     * Set the specified keyname to pressed
     * @param keyname The keyname to check
     * @param keystate The keystate of the keyname
     */
    public void setPressed(String keyname, boolean keystate) {
        Integer keycode = VirtualKey2.getKeycode(keyname);
        if (keycode != null) {
            setPressed(keycode, keystate);
        }
    }

    /**
     * @return A list of all currently pressed keynames
     */
    public List<String> getCurrentPresses() {
        List<String> out = new ArrayList<>();
        pressedKeys.forEach(keycode -> {
            out.add(VirtualKey2.getName(keycode));
        });
        return out;
    }

    /**
     * Calculates the difference between 2 peripherals via symmetric difference <br>
     * and returns a list of the changes between in form of {@link VirtualEvent}s
     *
     * @param nextPeripheral The peripheral that is comes after this one.<br>
     *                       If this one is loaded at tick 15, the nextPeripheral should be the one from tick 16
     * @return A list of {@link VirtualEvent}s
     */
    protected abstract <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral);

    @Override
    public String toString() {
        return String.join(",", getCurrentPresses());
    }
}
