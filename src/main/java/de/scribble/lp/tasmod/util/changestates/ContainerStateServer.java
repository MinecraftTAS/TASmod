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
			shouldChange=true;
			CommonProxy.NETWORK.sendTo(new RequestStatePacket(), player);
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
		if (state!=stateIn) {
			if(state==TASstate.NONE) {
				TASmod.logger.info(String.format("Set the server state to %s", stateIn.toString()));
				this.state = stateIn;
			}else if(state==TASstate.RECORDING) {
				if(stateIn==TASstate.PLAYBACK) {
					return;
				}
				TASmod.logger.info(String.format("Set the server state to %s", stateIn.toString()));
				this.state = stateIn;
			}else if(state==TASstate.PLAYBACK) {
				if(stateIn==TASstate.RECORDING) {
					return;
				}
				TASmod.logger.info(String.format("Set the server state to %s", stateIn.toString()));
				this.state = stateIn;
			}
		}
	}
	
	public TASstate toggleRecording() {
		setState(state==TASstate.RECORDING ? TASstate.NONE : TASstate.RECORDING);
		return state;
	}
	
	public TASstate togglePlayback() {
		setState(state == TASstate.PLAYBACK ? TASstate.NONE : TASstate.PLAYBACK);
		return state;
	}

	public TASstate getState() {
		return state;
	}
}
