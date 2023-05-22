package com.minecrafttas.tasmod.util;

import com.minecrafttas.common.events.client.EventClientGameLoop;
import com.minecrafttas.common.events.client.EventLaunchIntegratedServer;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackController;

import net.minecraft.client.Minecraft;

/**
 * Handles logic during a loading screen to transition between states.
 * @author Scribble
 *
 */
public class LoadingScreenHandler implements EventLaunchIntegratedServer, EventClientGameLoop{
	
	
	private boolean waszero;
	private boolean isLoading;
	private int loadingScreenDelay = -1;

	@Override
	public void onLaunchIntegratedServer() {
		TASmod.logger.info("Starting the integrated server");
		PlaybackController container = TASmodClient.virtual.getContainer();
		if(!container.isNothingPlaying() && !container.isPaused()) {
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
				TASmod.logger.info("Finished loading screen on the client");
				TASmodClient.tickratechanger.joinServer();
				if (!waszero) {
					if(TASmod.getServerInstance()!=null) {	//Check if a server is running and if it's an integrated server
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
	
	public boolean isLoading() {
		return isLoading;
	}
	
}
