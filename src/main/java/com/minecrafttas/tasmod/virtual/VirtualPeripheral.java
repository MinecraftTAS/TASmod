package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
	protected final List<T> subtickList;
	
	/**
	 * When creating an empty
	 */
    private boolean ignoreFirstUpdate = false;
	
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
		ignoreFirstUpdate = subtickList != null && subtickList.isEmpty(); // TODO Change, to something more robust
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

	/**
	 * @return An immutable list of subticks
	 */
	public List<T> getSubticks() {
		return ImmutableList.copyOf(subtickList);
	}

	/**
	 * Gets all peripheral states in an immutable list.<br>
	 * <br>
	 * This list is comprised of {@link #subtickList} and the current peripheral state added after that<br>
	 * This will result in a list where the first element is the oldest state and the last being the current state.
	 * @return An immutable list of keyboard states
	 */
	@SuppressWarnings("unchecked")
	public List<T> getAll() {
		return ImmutableList.<T>builder()
				.addAll(subtickList)
				.add((T)this)
				.build();
	}
	
	public boolean isParent() {
		return subtickList != null;
	}
	
	public boolean isKeyDown(int keycode) {
		return pressedKeys.contains(keycode);
	}
	
	public boolean isKeyDown(String keyname) {
		return pressedKeys.contains(VirtualKey2.getKeycode(keyname));
	}
	
	protected void clear() {
		pressedKeys.clear();
		subtickList.clear();
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
     * Moves the data from another virtual peripheral into this peripheral without creating a new object.
     * @param peripheral The peripheral to move from
     */
	protected void moveFrom(T peripheral) {
		this.pressedKeys.clear();
		this.pressedKeys.addAll(peripheral.pressedKeys);
	}
	
	protected boolean ignoreFirstUpdate() {
		boolean ignore = ignoreFirstUpdate;
		ignoreFirstUpdate = false;
		return ignore;
	}
}
