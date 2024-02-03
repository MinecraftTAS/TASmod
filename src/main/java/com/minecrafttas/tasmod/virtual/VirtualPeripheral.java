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
	 * A list of subtick peripherals.<br>
	 * If a peripheral <em>parent</em> is updated, it first adds it's current state to the subtickList before updating.<br>
	 * This makes the subtickList a list of previous peripheral states, with the first element being the oldest change.<br>
	 * <br>
	 * To distinguish a peripheral of being a subtick or a "parent", subtickList is either null or not null respectively (see {@link #isParent()})<br>
	 */
	protected final List<T> subtickList;
	
	/**
	 * The way the parent/subtick relationship is set up (see {@link #subtickList}),<br>
	 * the subtickList contains all previous changes, while the parent contains the current state.<br>
	 * To achieve this and to prevent a ghost state from being added to the subtickList,<br>
	 * it is sometimes necessary to ignore the first time an addition is made to the subtickList,<br>
	 * to delay the subtickList and make the parent the current state.
	 */
    private boolean ignoreFirstUpdate = false;
	
    /**
     * Creates a VirtualPeripheral
     * @param pressedKeys The {@link #pressedKeys}
     * @param subtickList The {@link #subtickList}
     * @param ignoreFirstUpdate The {@link #ignoreFirstUpdate} state
     */
    protected VirtualPeripheral(Set<Integer> pressedKeys, List<T> subtickList, boolean ignoreFirstUpdate) {
        this.pressedKeys = pressedKeys;
        this.subtickList = subtickList;
		this.ignoreFirstUpdate = ignoreFirstUpdate;
    }

    /**
     * Set the specified keycode to pressed
     * @param keycode The keycode to check
     * @param keystate The keystate of the keycode
     */
    protected void setPressed(int keycode, boolean keystate) {
		if (VirtualKeybindings.isKeyCodeAlwaysBlocked(keycode)) { //TODO Maybe a better system?
			return;
		}
        if (keystate)
            pressedKeys.add(keycode);
        else
            pressedKeys.remove(keycode);
    }
    
    /**
     * Adds a peripheral to {@link #subtickList}
     * @param peripheral The peripheral to add
     */
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
	
	/**
	 * @return If the peripheral is a parent and can add subticks
	 */
	public boolean isParent() {
		return subtickList != null;
	}
	
	/**
	 * If the key is available in {@link #pressedKeys}
	 * @param keycode The keycode in question
	 * @return If the key is pressed
	 */
	public boolean isKeyDown(int keycode) {
		return pressedKeys.contains(keycode);
	}
	
	/**
	 * If the key is available in {@link #pressedKeys}
	 * @param keyname The keyname in question
	 * @return If the key is pressed
	 */
	public boolean isKeyDown(String keyname) {
		return pressedKeys.contains(VirtualKey2.getKeycode(keyname));
	}
	
	/**
	 * Clears pressed keys and subticks
	 */
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
     * Copies the data from another virtual peripheral into this peripheral without creating a new object.
     * @param peripheral The peripheral to move from
     */
	protected void copyFrom(T peripheral) {
		this.pressedKeys.clear();
		this.pressedKeys.addAll(peripheral.pressedKeys);
		peripheral.subtickList.clear();
	}
	
	/**
	 * Retrieves and sets {@link #ignoreFirstUpdate} to false
	 * @return If the first update should be ignored
	 */
	protected boolean ignoreFirstUpdate() {
		boolean ignore = ignoreFirstUpdate;
		ignoreFirstUpdate = false;
		return ignore;
	}

	/**
	 * @return If this peripheral should ignore it's first update
	 * @see #ignoreFirstUpdate
	 */
	protected boolean isIgnoreFirstUpdate(){
		return ignoreFirstUpdate;
	}
}
