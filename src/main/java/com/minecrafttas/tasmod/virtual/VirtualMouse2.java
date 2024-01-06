package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.List;

public class VirtualMouse2 extends VirtualPeripheral implements Serializable {
	
	/**
	 * Creates a keyboard where all keys are unpressed
	 */
	public VirtualMouse2() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral) {
		return null;
	}

}
