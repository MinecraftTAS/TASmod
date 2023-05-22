package com.minecrafttas.tasmod.tickratechanger;

import com.minecrafttas.common.events.client.EventClientGameLoop;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.mixin.accessors.AccessorRunStuff;
import com.minecrafttas.tasmod.mixin.accessors.AccessorTimer;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;

import net.minecraft.client.Minecraft;

/**
 * Changes the {@link Minecraft#timer} variable
 * @author Scribble
 *
 */
public class TickrateChangerClient implements EventClientGameLoop{
	/**
	 * The current tickrate of the client
	 */
	public float ticksPerSecond;

	/**
	 * The tickrate before {@link #ticksPerSecond} was changed to 0, used to toggle
	 * pausing
	 */
	public float tickrateSaved = 20F;
	
	/**
	 * True if the tickrate is 20 and the client should advance 1 tick
	 */
	public boolean advanceTick = false;
	
	public long millisecondsPerTick = 50L;

	public TickrateChangerClient() {
		this(20f);
	}
	
	public TickrateChangerClient(float initialTickrate) {
		ticksPerSecond = initialTickrate;
	}
	
	/**
	 * Changes both client and server tickrates
	 * 
	 * @param tickrate The new tickrate of client and server
	 */
	public void changeTickrate(float tickrate) {
		changeClientTickrate(tickrate);
		changeServerTickrate(tickrate);
	}
	
	public void changeClientTickrate(float tickrate) {
		changeClientTickrate(tickrate, true);
	}

	/**
	 * Changes the tickrate of the client <br>
	 * If tickrate is zero, it will pause the game and store the previous tickrate
	 * in {@link #tickrateSaved}
	 * 
	 * @param tickrate The new tickrate of the client
	 */
	public void changeClientTickrate(float tickrate, boolean log) {
		if (tickrate < 0) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (tickrate > 0) {
			millisecondsPerTick = (long) (1000F / tickrate);
			((AccessorTimer) ((AccessorRunStuff) mc).timer()).tickLength(millisecondsPerTick);
			
		} else if (tickrate == 0F) {
			if (ticksPerSecond != 0) {
				tickrateSaved = ticksPerSecond;
			}
			((AccessorTimer) ((AccessorRunStuff) mc).timer()).tickLength(Float.MAX_VALUE);
		}
		ticksPerSecond = tickrate;
		if(log)
			log("Setting the client tickrate to "+ ticksPerSecond);
	}

	/**
	 * Attempts to change the tickrate on the server. Sends a
	 * {@link ChangeTickratePacket} to the server
	 * 
	 * @param tickrate
	 */
	public void changeServerTickrate(float tickrate) {
		if (tickrate < 0) {
			return;
		}
		TASmodClient.packetClient.sendToServer(new ChangeTickratePacket(tickrate));
	}

	/**
	 * Toggles between tickrate 0 and tickrate > 0
	 */
	public void togglePause() {
		if (Minecraft.getMinecraft().world != null) {
			TASmodClient.packetClient.sendToServer(new PauseTickratePacket());
		} else {
			togglePauseClient();
		}
	}

	/**
	 * Pauses and unpauses the client, used in main menus
	 */
	public void togglePauseClient() {
		if (ticksPerSecond > 0) {
			tickrateSaved = ticksPerSecond;
			pauseClientGame(true);
		} else if (ticksPerSecond == 0) {
			pauseClientGame(false);
		}
	}

	/**
	 * Enables tickrate 0
	 * 
	 * @param pause True if the game should be paused, false if unpause
	 */
	public void pauseGame(boolean pause) {
		if (pause) {
			changeTickrate(0F);
		} else {
			advanceTick=false;
			changeTickrate(tickrateSaved);
		}
	}

	/**
	 * Pauses the game without sending a command to the server
	 * @param pause The state of the client
	 */
	public void pauseClientGame(boolean pause) {
		if(pause) {
			changeClientTickrate(0F);
		}else {
			changeClientTickrate(tickrateSaved);
		}
	}
	
	/**
	 * Advances the game by 1 tick. Sends a {@link AdvanceTickratePacket} to the server or calls {@link #advanceClientTick()} if the world is null
	 */
	public void advanceTick() {
		if (Minecraft.getMinecraft().world != null) {
			advanceServerTick();
		} else {
			advanceClientTick();
		}
	}

	/**
	 * Sends a {@link AdvanceTickratePacket} to the server to advance the server
	 */
	public void advanceServerTick() {
		TASmodClient.packetClient.sendToServer(new AdvanceTickratePacket());
	}
	
	/**
	 * Advances the game by 1 tick. Doesn't send a packet to the server
	 */
	public void advanceClientTick() {
		if (ticksPerSecond == 0) {
			advanceTick = true;
			changeClientTickrate(tickrateSaved);
		}
	}
	
	public void joinServer() {
		changeServerTickrate(ticksPerSecond);
	}
	
	private static void log(String msg) {
		TASmod.logger.info(msg);
	}

	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		if (TASmodClient.packetClient != null && TASmodClient.packetClient.isClosed()) { // If the server died, but the client has not left the world
			TickSyncClient.shouldTick.set(true);
		}
	}

}
