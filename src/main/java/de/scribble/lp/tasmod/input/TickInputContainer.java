package de.scribble.lp.tasmod.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.scribble.lp.tasmod.virtual.VirtualKeys;

public class TickInputContainer {
	
	private final List<KeyboardInputContainer> keyboardInput = new ArrayList<KeyboardInputContainer>();
	
	private final List<MouseInputContainer> mouseInput = new ArrayList<MouseInputContainer>();
	
	private final List<CharContainer> charInput = new ArrayList<CharContainer>();
	
	private final List<CameraInputContainer> cameraInput = new ArrayList<CameraInputContainer>();
	
	private final List<MouseCoordsInputContainer> mouseCoordsInput = new ArrayList<MouseCoordsInputContainer>();

	public TickInputContainer() {
		
	}

}
