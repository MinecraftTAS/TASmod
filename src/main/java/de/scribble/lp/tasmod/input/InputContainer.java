package de.scribble.lp.tasmod.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.scribble.lp.tasmod.virtual.VirtualKeys;

public class InputContainer {
	private final List<TickInputContainer> tickinputContainer;
	
	InputContainer(){
		tickinputContainer= new ArrayList<TickInputContainer>();
	}
	
	public void add(Map<Integer, VirtualKeys> keysIn) {
		tickinputContainer.add(new TickInputContainer());
	}
}
