package de.scribble.lp.tasmod.inputcontainer;

/**
 * State of the input recorder
 * @author ScribbleLP
 *
 */
public enum TASstate {
	/**
	 * The game records inputs to the {@link InputContainer}.
	 */
	RECORDING,
	/**
	 * The game plays back the inputs loaded in {@link InputContainer} and locks user interaction.
	 */
	PLAYBACK,
	/**
	 * The playback or recording is paused and may be resumed. Note that the game isn't paused, only the playback. Useful for debugging things.
	 */
	PAUSED,	// #124
	/**
	 * The game is neither recording, playing back or paused, is also set when aborting all mentioned states.
	 */
	NONE;
	
	public int getIndex() {
		switch(this) {
		case NONE:
			return 0;
		case PLAYBACK:
			return 1;
		case RECORDING:
			return 2;
		case PAUSED:
			return 3;
		default:
			return 0;	
		}
	}
	
	public static TASstate fromIndex(int state) {
		switch (state) {
		case 0:
			return NONE;
		case 1:
			return PLAYBACK;
		case 2:
			return RECORDING;
		case 3:
			return PAUSED;
		default:
			return NONE;
		}
	}
}
