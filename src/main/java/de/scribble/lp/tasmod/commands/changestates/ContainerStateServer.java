package de.scribble.lp.tasmod.commands.changestates;

import de.scribble.lp.tasmod.util.TASstate;

/**
 * Stores the state of the input container on the server side. <br>
 * <br>
 * Since the current state, whether it's recording playing back or nothing is stored on the client,<br>
 * there needs to be some form of synchronization between all clients so all clients have the same state. <br>
 * <br>
 * Additionally the client can start recording before the server is even started and when multiple clients still have to connect to the server.
 * 
 * @author ScribbleLP
 *
 */
public class ContainerStateServer {
	private TASstate state;
	
	private boolean shouldChange;
	
	public ContainerStateServer() {
		state=TASstate.NONE;
		shouldChange=false;
	}
	
	
}
