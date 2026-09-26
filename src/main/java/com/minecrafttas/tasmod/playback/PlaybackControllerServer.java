package com.minecrafttas.tasmod.playback;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.NONE;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.PAUSED;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.PLAYBACK;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.RECORDING;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_CLEAR_INPUTS;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_FULLPLAY;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_FULLRECORD;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_LOAD;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_RESTARTANDPLAY;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_SAVE;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_STATE;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_CLEAR_SCREEN;
import static com.minecrafttas.tasmod.util.LoggerMarkers.Playback;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventPlaybackServer;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateCallback;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateFlags;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;

/**
 * The playback controller on the server side.<br>
 * Currently used sync the {@link TASstate} with all clients
 * 
 * @author Scribble
 *
 */
public class PlaybackControllerServer implements ServerPacketHandler {

	private TASstate state = NONE;

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		//@formatter:off
		return new TASmodPackets[] 
				{ 
				PLAYBACK_STATE,
				PLAYBACK_CLEAR_INPUTS,
				PLAYBACK_FULLPLAY,
				PLAYBACK_FULLRECORD,
				PLAYBACK_RESTARTANDPLAY,
				PLAYBACK_SAVE,
				PLAYBACK_LOAD
				};
		//@formatter:on
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {

			case PLAYBACK_STATE_TEMP_SAVESTATE:
				if (TASmod.savestateHandlerServer != null)
					TASmod.savestateHandlerServer.getSavestateTemporaryHandler().setActive(true);
			case PLAYBACK_STATE:
				TASstate networkState = TASmodBufferBuilder.readEnum(TASstate.class, buf);

				/* TODO Permissions */
				setTASState(networkState);
				break;

			case PLAYBACK_CLEAR_INPUTS:
				clearInputs();
				break;
			case PLAYBACK_FULLRECORD:
				fullRecord();
				break;
			case PLAYBACK_FULLPLAY:
				fullPlay();
				break;
			case PLAYBACK_RESTARTANDPLAY:
				String tasFileName = TASmodBufferBuilder.readString(buf);
				restartAndPlay(tasFileName);
				break;
			case PLAYBACK_SAVE:
			case PLAYBACK_LOAD:
				TASmod.server.sendToAll(new TASmodBufferBuilder(buf));
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
		}
	}

	public void setTASState(TASstate stateIn) {
		setTASStateServer(stateIn);
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(PLAYBACK_STATE).writeEnum(state).writeBoolean(true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTASStateServer(TASstate stateIn) {
		if (state != stateIn) {
			if (state == RECORDING && stateIn == PLAYBACK)
				return;
			if (state == NONE && state == PAUSED) {
				return;
			}
			EventListenerRegistry.fireEvent(EventPlaybackServer.EventControllerStateChange.class, stateIn, this.state);

			this.state = stateIn;
			LOGGER.info(Playback, "Set the server state to {}", stateIn.toString());
		}
	}

	public void toggleRecording() {
		setTASState(state == RECORDING ? NONE : RECORDING);
	}

	public void togglePlayback() {
		setTASState(state == PLAYBACK ? NONE : PLAYBACK);
	}

	public void clearInputs() {
		EventListenerRegistry.fireEvent(EventPlaybackServer.EventRecordClear.class);
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(PLAYBACK_CLEAR_INPUTS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TASstate getState() {
		return state;
	}

	public void fullRecord() {
		SavestateCallback cb = (paths) -> {
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(SAVESTATE_CLEAR_SCREEN));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		};

		TASmod.tickSchedulerServer.add(() -> {
			try {
				TASmod.savestateHandlerServer.saveState(0, cb, SavestateFlags.BLOCK_PAUSE_TICKRATE);
			} catch (SavestateException e) {
				LOGGER.catching(e);
				return;
			} finally {
				TASmod.savestateHandlerServer.resetState();
			}

			setTASStateServer(TASstate.RECORDING);

			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(PLAYBACK_FULLRECORD));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		});
	}

	public void fullPlay() {
		SavestateCallback cb = (paths) -> {
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_CLEAR_SCREEN));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		};

		TASmod.tickSchedulerServer.add(() -> {
			try {
				TASmod.savestateHandlerServer.loadState(0, cb, SavestateFlags.BLOCK_CHANGE_INDEX, SavestateFlags.BLOCK_PAUSE_TICKRATE);
			} catch (LoadstateException e) {
				LOGGER.catching(e);
				return;
			} finally {
				TASmod.savestateHandlerServer.resetState();
			}

			setTASStateServer(TASstate.PLAYBACK);

			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(PLAYBACK_FULLPLAY));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		});
	}

	public void restartAndPlay(String tasFileName) {
		TASmod.playbackControllerServer.setTASStateServer(PLAYBACK);

		TASmod.tickSchedulerServer.add(() -> {
			try {
				TASmod.savestateHandlerServer.loadState(0, null, SavestateFlags.BLOCK_PAUSE_TICKRATE);
			} catch (LoadstateException e) {
				LOGGER.catching(e);
				TASmod.savestateHandlerServer.resetState();
				TASmod.tickratechanger.pauseGame(false);
				return;
			}
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(PLAYBACK_RESTARTANDPLAY).writeString(tasFileName));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		});
	}
}

