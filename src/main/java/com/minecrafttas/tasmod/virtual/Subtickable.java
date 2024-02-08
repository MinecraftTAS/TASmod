package com.minecrafttas.tasmod.virtual;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Subtickable<T> {
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
	
	protected Subtickable(List<T> subtickList, boolean ignoreFirstUpdate) {
		this.subtickList = subtickList;
		this.ignoreFirstUpdate = ignoreFirstUpdate;
	}
	
    /**
     * Adds a peripheral to {@link #subtickList}
     * @param peripheral The peripheral to add
     */
    protected void addSubtick(T peripheral) {
    	subtickList.add(peripheral);
    }
    
	/**
	 * @return An immutable list of subticks
	 */
	public List<T> getSubticks() {
		return ImmutableList.copyOf(subtickList);
	}

	/**
	 * @return If the peripheral is a parent and can add subticks
	 */
	public boolean isParent() {
		return subtickList != null;
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
	
	protected void resetFirstUpdate() {
		ignoreFirstUpdate = true;
	}
}
