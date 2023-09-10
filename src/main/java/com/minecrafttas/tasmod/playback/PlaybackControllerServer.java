package com.minecrafttas.tasmod.playback;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.networking.TASmodPackets.*;

import java.nio.ByteBuffer;

import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * The playback controller on the server side.<br>
 * Currently used sync the {@link TASstate} with all clients
 * 
 * @author Scribble
 *
 */
public class PlaybackControllerServer implements ServerPacketHandler {

	private TASstate state;

	private boolean shouldChange = true;

	public PlaybackControllerServer() {
		state = TASstate.NONE;
		shouldChange = true;
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] 
				{ 
				STATESYNC_INITIAL,
				STATESYNC,
				PLAYBACK_TELEPORT,
				CLEAR_INNPUTS,
				PLAYBACK_FULLPLAY,
				PLAYBACK_FULLRECORD,
				PLAYBACK_RESTARTANDPLAY,
				PLAYBACK_PLAYUNTIL,
				PLAYBACK_SAVE,
				PLAYBACK_LOAD
				};
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case STATESYNC_INITIAL:
				TASstate networkState = TASmodBufferBuilder.readTASState(buf);
				if (/* TODO Permissions && */ shouldChange) {
					setState(networkState);
					shouldChange = false;
				} else {
					TASmod.server.sendTo(username, new TASmodBufferBuilder(TASmodPackets.STATESYNC_INITIAL).writeTASState(networkState));
				}
				break;

			case STATESYNC:
				networkState = TASmodBufferBuilder.readTASState(buf);
				/* TODO Permissions */
				setState(networkState);
				break;

			case PLAYBACK_TELEPORT:
				double x = TASmodBufferBuilder.readDouble(buf);
				double y = TASmodBufferBuilder.readDouble(buf);
				double z = TASmodBufferBuilder.readDouble(buf);
				float angleYaw = TASmodBufferBuilder.readFloat(buf);
				float anglePitch = TASmodBufferBuilder.readFloat(buf);

				EntityPlayerMP player = TASmod.getServerInstance().getPlayerList().getPlayerByUsername(username);
				player.getServerWorld().addScheduledTask(() -> {
					player.rotationPitch = anglePitch;
					player.rotationYaw = angleYaw;

					player.setPositionAndUpdate(x, y, z);
				});
				break;

			case CLEAR_INNPUTS:
				TASmod.server.sendToAll(new TASmodBufferBuilder(CLEAR_INNPUTS));

			case PLAYBACK_FULLPLAY:
			case PLAYBACK_FULLRECORD:
			case PLAYBACK_RESTARTANDPLAY:
			case PLAYBACK_PLAYUNTIL:
			case PLAYBACK_SAVE:
			case PLAYBACK_LOAD:
				TASmod.server.sendToAll(new TASmodBufferBuilder(buf));
				break;
				
			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
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
			if (state == TASstate.NONE && state == TASstate.PAUSED) {
				return;
			}
			this.state = stateIn;
			LOGGER.info(String.format("Set the server state to %s", stateIn.toString()));
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
