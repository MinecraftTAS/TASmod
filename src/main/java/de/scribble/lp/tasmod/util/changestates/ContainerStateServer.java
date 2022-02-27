package de.scribble.lp.tasmod.util.changestates;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Stores the state of the input container on the server side. <br>
 * <br>
 * Since the current state, whether it's recording playing back or nothing is
 * stored on the client,<br>
 * there needs to be some form of synchronization between all clients so all
 * clients have the same state. <br>
 * <br>
 * Additionally the client can start recording before the server is even started
 * and when multiple clients still have to connect to the server.
 * 
 * @author ScribbleLP
 *
 */
public class ContainerStateServer {
	private TASstate state;

	private boolean shouldChange;

	public ContainerStateServer() {
		state = TASstate.NONE;
		shouldChange = false;
	}

	public void joinServer(EntityPlayerMP player) {
		if (!shouldChange) {
			shouldChange = true;
			CommonProxy.NETWORK.sendTo(new RequestStatePacket(), player); //TODO Rewrite this s***
		} else {
			CommonProxy.NETWORK.sendTo(new SyncStatePacket(state, false), player);
		}
	}

	public void leaveServer(EntityPlayerMP player) {
		MinecraftServer server = TASmod.getServerInstance();
		if (server != null) {
			if (server.getPlayerList().getPlayers().size() == 1) {
				state = TASstate.NONE;
				shouldChange = false;
			}
		}
	}

	public void setState(TASstate stateIn) {
		setServerState(stateIn);
		CommonProxy.NETWORK.sendToAll(new SyncStatePacket(state));
	}
	
	public void setServerState(TASstate stateIn) {
		if (state != stateIn) {
			if (state == TASstate.RECORDING && stateIn == TASstate.PLAYBACK || state == TASstate.PLAYBACK && stateIn == TASstate.RECORDING)
				return;
			if(state==TASstate.NONE&&state==TASstate.PAUSED) {
				return;
			}
			this.state = stateIn;
			TASmod.logger.info(String.format("Set the server state to %s", stateIn.toString()));
		}
	}
	
	public void toggleRecording() {
		setState(state == TASstate.RECORDING ? TASstate.NONE : TASstate.RECORDING);
	}

	public void togglePlayback() {
		setState(state == TASstate.PLAYBACK ? TASstate.NONE : TASstate.PLAYBACK);
	}
	


	public TASstate getState() {
		return state;
	}
}
