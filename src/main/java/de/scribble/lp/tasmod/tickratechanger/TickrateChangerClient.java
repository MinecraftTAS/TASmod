package de.scribble.lp.tasmod.tickratechanger;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.events.LoadWorldEvents;
import de.scribble.lp.tasmod.mixin.accessors.AccessorRunStuff;
import de.scribble.lp.tasmod.mixin.accessors.AccessorTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

/**
 * Changes the {@link Minecraft#timer} variable
 * @author ScribbleLP
 *
 */
public class TickrateChangerClient {
	/**
	 * The current tickrate of the client
	 */
	public static float ticksPerSecond = 20f;

	/**
	 * The tickrate before {@link #ticksPerSecond} was changed to 0, used to toggle
	 * pausing
	 */
	public static float tickrateSaved = 20F;
	
	/**
	 * True if the tickrate is 20 and the client should advance 1 tick
	 */
	public static boolean advanceTick = false;

	/**
	 * Changes both client and server tickrates
	 * 
	 * @param tickrate The new tickrate of client and server
	 */
	public static void changeTickrate(float tickrate) {
		changeClientTickrate(tickrate);
		changeServerTickrate(tickrate);
	}

	/**
	 * Changes the tickrate of the client <br>
	 * If tickrate is zero, it will pause the game and store the previous tickrate
	 * in {@link #tickrateSaved}
	 * 
	 * @param tickrate The new tickrate of the client
	 */
	public static void changeClientTickrate(float tickrate) {
		if (tickrate < 0) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (tickrate > 0) {
			((AccessorTimer) ((AccessorRunStuff) mc).timer()).tickLength(1000F / tickrate);
		} else if (tickrate == 0F) {
			if (ticksPerSecond != 0) {
				tickrateSaved = ticksPerSecond;
			}
			((AccessorTimer) ((AccessorRunStuff) mc).timer()).tickLength(Float.MAX_VALUE);
		}
		ticksPerSecond = tickrate;
		log("Setting the client tickrate to "+ ticksPerSecond);
	}

	/**
	 * Attempts to change the tickrate on the server. Sends a
	 * {@link ChangeTickratePacket} to the server
	 * 
	 * @param tickrate
	 */
	public static void changeServerTickrate(float tickrate) {
		if (tickrate < 0) {
			return;
		}
		CommonProxy.NETWORK.sendToServer(new ChangeTickratePacket(tickrate));
	}

	/**
	 * Toggles between tickrate 0 and tickrate > 0
	 */
	public static void togglePause() {
		if (Minecraft.getMinecraft().world != null) {
			CommonProxy.NETWORK.sendToServer(new PauseTickratePacket());
		} else {
			togglePauseClient();
		}
	}

	/**
	 * Pauses and unpauses the client, used in main menus
	 */
	public static void togglePauseClient() {
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
	public static void pauseGame(boolean pause) {
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
	public static void pauseClientGame(boolean pause) {
		if(pause) {
			changeClientTickrate(0F);
		}else {
			changeClientTickrate(tickrateSaved);
		}
	}
	
	/**
	 * Advances the game by 1 tick. Sends a {@link AdvanceTickratePacket} to the server or calls {@link #advanceClientTick()} if the world is null
	 */
	public static void advanceTick() {
		if (Minecraft.getMinecraft().world != null) {
			advanceServerTick();
		} else {
			advanceClientTick();
		}
	}

	/**
	 * Sends a {@link AdvanceTickratePacket} to the server to advance the server
	 */
	public static void advanceServerTick() {
		CommonProxy.NETWORK.sendToServer(new AdvanceTickratePacket());
	}
	
	/**
	 * Advances the game by 1 tick. Doesn't send a packet to the server
	 */
	public static void advanceClientTick() {
		if (ticksPerSecond == 0) {
			advanceTick = true;
			changeClientTickrate(tickrateSaved);
		}
	}
	
	public static void joinServer() {
		changeServerTickrate(ticksPerSecond);
	}
	
	private static void log(String msg) {
		TASmod.logger.debug(msg);
	}

}
