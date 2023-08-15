package com.minecrafttas.tasmod.playback.server;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

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
public class TASstateServer implements ServerPacketHandler{
	
	private TASstate state;

	private boolean shouldChange = true;

	public TASstateServer() {
		state = TASstate.NONE;
		shouldChange = true;
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {TASmodPackets.STATESYNC_INITIAL, TASmodPackets.STATESYNC};
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		TASstate networkState = TASmodBufferBuilder.readTASState(buf);
		
		switch (packet) {
		case STATESYNC_INITIAL:
			if (/* TODO Permissions && */ shouldChange) {
				setState(networkState);
				shouldChange = false;
			} else {
				TASmod.server.sendTo(clientID, new TASmodBufferBuilder(TASmodPackets.STATESYNC_INITIAL).writeTASState(networkState));
			}
			break;

		case STATESYNC:
			/* TODO Permissions */
			setState(networkState);
			break;

		default:
			throw new PacketNotImplementedException(packet, this.getClass());
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
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.STATESYNC).writeTASState(state).writeBoolean(true));
		} catch (Exception e) {
			e.printStackTrace();
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
			TASmod.LOGGER.info(String.format("Set the server state to %s", stateIn.toString()));
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
