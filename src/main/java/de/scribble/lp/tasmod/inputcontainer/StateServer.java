package de.scribble.lp.tasmod.inputcontainer;

import de.scribble.lp.tasmod.util.TASstate;

/**
 * Saves the current recording state, whether it is Recording, Playing back or nothing and synchronizes it with the client
 * @author ScribbleLP
 *
 */
public class StateServer {
	
	/**
	 * The recording state of the server. Gets synchronized with the client once someone joins the game.
	 */
	TASstate serverState=TASstate.NONE;
}
