package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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
public abstract class VirtualPeripheral<T extends VirtualPeripheral<T>> implements Serializable {

    /**
     * The list of keycodes that are currently pressed on this peripheral.
     */
    protected final Set<Integer> pressedKeys;
	/**
	 * A list of subtick keyboards.<br>
	 * If subtickKeyboards is initialized, the object can be considered as a <em>parent</em> keyboard,<br>
	 * able to house subtickKeyboards.<br>
	 * <br>
	 * If subtickKeyboards is null then the object is a "child"/subtickKeyboard stored in a parent list
	 */
	private final List<T> subtickList;
	
    /**
     * Create a peripheral with already existing pressed keys
     * @param pressedKeys The existing pressedKeys
     */
	protected VirtualPeripheral(Set<Integer> pressedKeys) {
		this(pressedKeys, null);
	}
	
    protected VirtualPeripheral(Set<Integer> pressedKeys, List<T> subtickList) {
        this.pressedKeys = pressedKeys;
        this.subtickList = subtickList;
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
    
    protected void addSubtick(T peripheral) {
    	subtickList.add(peripheral);
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
	 *                       If this one is loaded at tick 15, the nextPeripheral
	 *                       should be the one from tick 16
	 * @return A list of {@link VirtualEvent}s
	 */
	protected abstract Queue<? extends VirtualEvent> getDifference(T nextPeripheral);

	/**
	 * Calculates a list of {@link VirtualEvent}s to the next peripheral including
	 * the subticks.
	 * 
	 * @param nextPeripheral The peripheral that is comes after this one.<br>
	 *                       If this one is loaded at tick 15, the nextPeripheral
	 *                       should be the one from tick 16
	 * @return A list of {@link VirtualEvent}s
	 */
	protected abstract Queue<? extends VirtualEvent> getVirtualEvents(T nextPeripheral);
    
    @Override
    public String toString() {
        return String.join(",", getCurrentPresses());
    }

    /**
     * @return An immutable set of pressed keycodes
     */
	public Set<Integer> getPressedKeys() {
		return ImmutableSet.copyOf(pressedKeys);
	}

	public List<T> getSubticks() {
		return ImmutableList.copyOf(subtickList);
	}

	public boolean isParent() {
		return subtickList != null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VirtualPeripheral) {
			VirtualPeripheral<?> peripheral = (VirtualPeripheral<?>) obj;
			for (Integer keycode : pressedKeys) {
				if(!peripheral.pressedKeys.contains(keycode)) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}
	
    /**
     * Copies the data from another virtual peripheral into this peripheral without creating a new object.
     * @param peripheral The peripheral to copy from
     */
	protected void copyFrom(T peripheral) {
		this.pressedKeys.clear();
		this.pressedKeys.addAll(peripheral.pressedKeys);
	}
}
