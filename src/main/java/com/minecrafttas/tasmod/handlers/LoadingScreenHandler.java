package com.minecrafttas.tasmod.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import com.minecrafttas.mctcommon.events.EventClient.EventClientGameLoop;
import com.minecrafttas.mctcommon.events.EventClient.EventDoneLoadingWorld;
import com.minecrafttas.mctcommon.events.EventClient.EventLaunchIntegratedServer;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.client.Minecraft;

/**
 * Handles logic during a loading screen to transition between states.
 * 
 * @author Scribble
 *
 */
public class LoadingScreenHandler implements EventLaunchIntegratedServer, EventClientGameLoop, EventDoneLoadingWorld {

	private boolean waszero;
	private boolean isLoading;
	private int loadingScreenDelay = -1;

	@Override
	public void onLaunchIntegratedServer() {
		LOGGER.debug(LoggerMarkers.Event, "Starting the integrated server");
		PlaybackControllerClient container = TASmodClient.controller;
		if (!container.isNothingPlaying() && !container.isPaused()) {
			container.pause(true);
		}
		if (TASmodClient.tickratechanger.ticksPerSecond == 0 || TASmodClient.tickratechanger.advanceTick) {
			waszero = true;
		}
		isLoading = true;
	}

	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		if (loadingScreenDelay > -1) {
			if (loadingScreenDelay == 0) {
				LOGGER.debug(LoggerMarkers.Event, "Finished loading screen on the client");
				TASmodClient.tickratechanger.joinServer();
				if (!waszero) {
					if (TASmod.getServerInstance() != null) { // Check if a server is running and if it's an integrated server
						TASmodClient.tickratechanger.pauseClientGame(false);
						TASmod.tickratechanger.pauseServerGame(false);
					}
				} else {
					waszero = false;
				}
				isLoading = false;
			}
			loadingScreenDelay--;
		}
	}

	@Override
	public void onDoneLoadingWorld() {
		if (TASmod.getServerInstance() != null) { // Check if a server is running and if it's an integrated server
			LOGGER.debug(LoggerMarkers.Event, "Finished loading the world on the client");
			loadingScreenDelay = 1;
		}
	}

	public boolean isLoading() {
		return isLoading;
	}

}
