package com.minecrafttas.tasmod.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientGameLoop;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventDoneLoadingWorld;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventLaunchIntegratedServer;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerLeaveClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventSetCameraAngle;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualCameraAngleInput;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Handles logic during a loading screen to transition between states.
 * 
 * @author Scribble
 */
public class LoadingScreenHandler implements EventLaunchIntegratedServer, EventClientGameLoop, EventDoneLoadingWorld, EventSetCameraAngle, EventPlayerJoinedClientSide, EventPlayerLeaveClientSide {

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

	/**
	 * {@inheritDoc}
	 * 
	 * <p>Initializes the virtual camera to be in line with the vanilla camera.
	 */
	@Override
	public void onSetCameraAngle() {
		LOGGER.debug(LoggerMarkers.Event, "Setting the camera angle");
		VirtualCameraAngleInput cameraAngle = TASmodClient.virtual.CAMERA_ANGLE;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		cameraAngle.setCamera(player.getXRot(), player.getYRot());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>Fixes stuck keys when loading the world  
	 */
	@Override
	public void onPlayerJoinedClientSide(LocalPlayer player) {
		TASmodClient.virtual.clearNext();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>Resets the camera angle when leaving the world.
	 * <p>If you later rejoin the world {@link #onSetCameraAngle()} will re-initialise the camera angle
	 */
	@Override
	public void onPlayerLeaveClientSide(LocalPlayer player) {
		LOGGER.debug(LoggerMarkers.Event, "Finished leaving on the on the client side");
		LOGGER.debug("Resetting the camera angle on leaving the world");
		TASmodClient.virtual.CAMERA_ANGLE.clearNext();
	}
}

