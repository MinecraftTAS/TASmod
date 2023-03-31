package com.minecrafttas.tasmod.inputcontainer.server;

import com.minecrafttas.tasmod.CommonProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.inputcontainer.TASstate;

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
 * @author Scribble
 *
 */
public class ContainerStateServer {
	
	private TASstate state;

	private boolean shouldChange = true;

	public ContainerStateServer() {
		state = TASstate.NONE;
		shouldChange = true;
	}

	public void onInitialPacket(EntityPlayerMP player, TASstate tasState) {
		if(player.canUseCommand(2, "") && shouldChange) {
			setState(tasState);
			shouldChange = false;
		}else {
			TASmod.packetServer.sendTo(new SyncStatePacket(tasState), player);
		}
	}
	
	public void onPacket(EntityPlayerMP player, TASstate tasState) {
		if(player.canUseCommand(2, "")) {
			setState(tasState);
		}
	}

	public void leaveServer(EntityPlayerMP player) {
		MinecraftServer server = TASmod.getServerInstance();
		if (server != null) {
			if (server.getPlayerList().getPlayers().size() == 1) {
				state = TASstate.NONE;
				shouldChange = true;
			}
		}
	}

	public void setState(TASstate stateIn) {
		setServerState(stateIn);
		TASmod.packetServer.sendToAll(new SyncStatePacket(state, true));
		
		if(state == TASstate.RECORDING) { // Set the start seed of the recording
			CommonProxy.tickSchedulerServer.add(() ->{
				TASmod.ktrngHandler.broadcastStartSeed();
			});
		}
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
