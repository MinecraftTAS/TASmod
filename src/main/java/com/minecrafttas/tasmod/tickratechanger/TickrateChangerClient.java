package com.minecrafttas.tasmod.tickratechanger;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;

import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.server.Client.Side;
import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;
import com.minecrafttas.mctcommon.server.interfaces.ClientPacketHandler;
import com.minecrafttas.mctcommon.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickrateChange;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.TickratePauseState;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.client.Minecraft;

/**
 * Changes the {@link Minecraft#timer} variable
 * 
 * @author Scribble
 *
 */
public class TickrateChangerClient implements ClientPacketHandler {
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
			mc.timer.tickLength = millisecondsPerTick;

		} else if (tickrate == 0F) {
			if (ticksPerSecond != 0) {
				tickrateSaved = ticksPerSecond;
			}
			mc.timer.tickLength = Float.MAX_VALUE;
		}
		ticksPerSecond = tickrate;
		EventListenerRegistry.fireEvent(EventClientTickrateChange.class, tickrate);
		if (log)
			log("Setting the client tickrate to " + ticksPerSecond);
	}

	/**
	 * Attempts to change the tickrate on the server. Sends a
	 * {@link TASmodPackets#TICKRATE_CHANGE} packet to the server
	 * 
	 * @param tickrate The new server tickrate
	 */
	public void changeServerTickrate(float tickrate) {
		if (tickrate < 0) {
			return;
		}

		try {
			// request tickrate change
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKRATE_CHANGE).writeFloat(tickrate));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Toggles between tickrate 0 and tickrate > 0
	 */
	public void togglePause() {
		try {
			// request tickrate change
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKRATE_ZERO).writeTickratePauseState(TickratePauseState.TOGGLE));
		} catch (Exception e) {
			e.printStackTrace();
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
			advanceTick = false;
			changeTickrate(tickrateSaved);
		}
	}

	/**
	 * Pauses the game without sending a command to the server
	 * 
	 * @param pause The state of the client
	 */
	public void pauseClientGame(boolean pause) {
		if (pause) {
			changeClientTickrate(0F);
		} else {
			changeClientTickrate(tickrateSaved);
		}
	}

	/**
	 * Advances the game by 1 tick. Sends a {@link AdvanceTickratePacket} to the
	 * server or calls {@link #advanceClientTick()} if the world is null
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
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKRATE_ADVANCE));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		LOGGER.debug(LoggerMarkers.Tickrate, msg);
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { TASmodPackets.TICKRATE_CHANGE, TASmodPackets.TICKRATE_ADVANCE, TASmodPackets.TICKRATE_ZERO, };
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case TICKRATE_CHANGE:
				float tickrate = TASmodBufferBuilder.readFloat(buf);
				changeClientTickrate(tickrate);
				break;
			case TICKRATE_ADVANCE:
				advanceClientTick();
				break;
			case TICKRATE_ZERO:
				TickratePauseState state = TASmodBufferBuilder.readTickratePauseState(buf);

				switch (state) {
					case PAUSE:
						pauseClientGame(true);
						break;
					case UNPAUSE:
						pauseClientGame(false);
						break;
					case TOGGLE:
						togglePauseClient();
					default:
						break;
				}
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.CLIENT);
		}
	}

}
