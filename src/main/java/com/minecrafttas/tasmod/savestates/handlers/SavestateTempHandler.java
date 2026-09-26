package com.minecrafttas.tasmod.savestates.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.NONE;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.PLAYBACK;
import static com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate.RECORDING;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_CLEAR_SCREEN;

import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventRecordClear;
import com.minecrafttas.tasmod.events.EventPlaybackServer.EventControllerStateChange;
import com.minecrafttas.tasmod.events.EventSavestate;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.SavestatePaths;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.util.TASComponent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * <p>Handles the creation of temporary savestates when recording/playing back a TAS
 * <p>Is exclusively run on the server side
 * 
 * @author Scribble
 */
public class SavestateTempHandler implements EventControllerStateChange, EventRecordClear, EventSavestate.EventServerLoadstate {

	private final Logger logger;
	private final SavestateHandlerServer handler;

	private boolean createState = true;
	private boolean active = false;

	public SavestateTempHandler(SavestateHandlerServer handler, Logger logger) {
		this.logger = logger;
		this.handler = handler;
	}

	@Override
	public void onControllerStateChange(TASstate newstate, TASstate oldstate) {

		if (oldstate != NONE) {
			return;
		}

		if (!active) {
			return;
		}
		active = false;

		if (newstate == RECORDING && createState) {
			logger.info("Creating temporary savestate");
			createState = false;
			try {
				handler.saveStateTemp((paths) -> {
					try {
						TASmod.server.sendToAll(new TASmodBufferBuilder(SAVESTATE_CLEAR_SCREEN));
					} catch (Exception e) {
						logger.catching(e);
					}
				});
			} catch (SavestateException e) {
				TASmod.getServerInstance().getPlayerList().broadcastSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED), false);

				try {
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
				} catch (Exception e1) {
					logger.catching(e);
				}

				LOGGER.error("Failed to create a temp savestate");
				LOGGER.catching(e);
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause == null) {
					cause = e;
				}
				TASmod.getServerInstance().getPlayerList().broadcastSystemMessage(Component.translatable("msg.tasmod.savestate.failure", e.getMessage()).withStyle(ChatFormatting.RED), false);

				try {
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
				} catch (Exception e1) {
					logger.catching(e);
				}

				LOGGER.error("Failed to create a temp savestate");
				LOGGER.catching(e);
			} finally {
				handler.resetState();
			}
		} else if (newstate == PLAYBACK) {
			logger.info("Loading temporary savestate");
			createState = false;
			try {
				handler.loadStateTemp((paths) -> {
					try {
						TASmod.server.sendToAll(new TASmodBufferBuilder(SAVESTATE_CLEAR_SCREEN));
					} catch (Exception e) {
						logger.catching(e);
					}
				});
			} catch (SavestateException e) {
				TASmod.getServerInstance().getPlayerList().broadcastSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED), false);

				try {
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
				} catch (Exception e1) {
					logger.catching(e);
				}

				LOGGER.error("Failed to load a temp savestate");
				LOGGER.catching(e);
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause == null) {
					cause = e;
				}
				TASmod.getServerInstance().getPlayerList().broadcastSystemMessage(Component.translatable("msg.tasmod.savestate.failure", e.getMessage()).withStyle(ChatFormatting.RED), false);

				try {
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
				} catch (Exception e1) {
					logger.catching(e);
				}

				LOGGER.error("Failed to load a temp savestate");
				LOGGER.catching(e);
			} finally {
				handler.resetState();
			}
		}
	}

	@Override
	public void onRecordingClear() {
		createState = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void onServerLoadstate(MinecraftServer server, SavestatePaths paths) {
		createState = false;
	}
}

