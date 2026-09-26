package com.minecrafttas.tasmod.tickratechanger;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientGameLoop;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.events.EventTickratechanger;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.TickratePauseState;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.client.Minecraft;

/**
 * Changes the {@link Minecraft#timer} variable
 * 
 * @author Scribble
 *
 */
public class TickrateChangerClient implements ClientPacketHandler, EventClientGameLoop {
	/**
	 * The current tickrate of the client
	 */
	public float ticksPerSecond;

	/**
	 * <p>The tickrate before {@link #ticksPerSecond} was changed to 0
	 * <p>Used to toggle pausing
	 */
	public float tickrateSaved = 20F;

	/**
	 * True if the tickrate is 20 and the client should advance 1 tick
	 */
	public boolean advanceTick = false;

	/**
	 * How many milliseconds should pass in a tick.
	 */
	public long millisecondsPerTick = 50L;

	/**
	 * Timestamp when the last tickrate change was initiated
	 */
	public long timestampSinceLastTickRateChange = System.currentTimeMillis();

	/**
	 * Time since the last tickrate change was initiated without being affected by tickrate
	 */
	public long fakeTimeSinceTickRateChange = System.currentTimeMillis();

	/**
	 * Timestamp since last game loop and only updated when the tickrate is 0
	 */
	private long timestampSinceLastGameLoop;

	/**
	 * Counts the milliseconds when the tickrate is 0
	 */
	private float timeOffset = 0L;

	/**
	 * The tickrate steps that can be set via {@link #increaseTickrate()} and {@link #decreaseTickrate()}
	 */
	private float[] rates = new float[] { .1f, .2f, .5f, 1f, 2f, 5f, 10f, 20f, 40f, 100f };
	/**
	 * The current index of the {@link #rates}
	 */
	private short rateIndex = 7;	// Defaults to tickrate 20

	/**
	 * <p>Creates a new Tickratechanger that is intended to run solely on the client side
	 * <p>The initial tickrate will be set to 20 ticks/s
	 */
	public TickrateChangerClient() {
		this(20f);
	}

	/**
	 * <p>Creates a new Tickratechanger that is intended to run solely on the client side
	 * 
	 * @param initialTickrate The initial tickrate of the client
	 */
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

	/**
	 * <p>Changes the tickrate of the client
	 * <p>If tickrate is zero, it will pause the game and store the previous tickrate
	 * in {@link #tickrateSaved}
	 * 
	 * @param tickrate The new tickrate of the client
	 */
	public void changeClientTickrate(float tickrate) {
		changeClientTickrate(tickrate, true);
	}

	/**
	 * <p>Changes the tickrate of the client
	 * <p>If tickrate is zero, it will pause the game and store the previous tickrate
	 * in {@link #tickrateSaved}
	 * 
	 * @param tickrate The new tickrate of the client
	 * @param log Whether this interaction should be logged
	 */
	public void changeClientTickrate(float tickrate, boolean log) {
		if (tickrate < 0) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (tickrate > 0) {
			millisecondsPerTick = (long) (1000F / tickrate);
			// deltaTracker.tickLength is private in 1.22+, stub for now
			// setDeltaTrackerTickLength(mc, millisecondsPerTick);

		} else if (tickrate == 0F) {
			if (ticksPerSecond != 0) {
				tickrateSaved = ticksPerSecond;
			}
			// setDeltaTrackerTickLength(mc, Float.MAX_VALUE);
		}
		ticksPerSecond = tickrate;
		EventListenerRegistry.fireEvent(EventTickratechanger.EventClientTickrateChange.class, tickrate);
		if (log)
			log("Setting the client tickrate to " + ticksPerSecond);
	}

	// Stubbed - reflection on private deltaTracker fields is unreliable in 1.22+
	// private void setDeltaTrackerTickLength(Minecraft mc, float tickLength) {
	// }

	/**
	 * <p>Attempts to change the tickrate on the server.
	 * <p>Sends a {@link TASmodPackets#TICKRATE_CHANGE} packet to the server
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
	 * @return Milliseconds affected by tickrate
	 */
	public long getMilliseconds() {
		long time = (long) (System.currentTimeMillis() - timestampSinceLastTickRateChange - timeOffset);
		time *= (ticksPerSecond / 20F);
		return (long) (fakeTimeSinceTickRateChange + time);
	}

	/**
	 * <p>Toggles between tickrate 0 and tickrate > 0
	 */
	public void togglePause() {
		try {
			// request tickrate change
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKRATE_ZERO).writeEnum(TickratePauseState.TOGGLE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>Pauses and unpauses the client, used in main menus
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
	 * <p>Enables tickrate 0
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
	 * <p>Pauses the game without sending a command to the server
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
	 * <p>Advances the game by 1 tick.
	 * <p>Sends a {@link TASmodPackets#TICKRATE_ADVANCE} to the server<p>
	 * or calls {@link #advanceClientTick()} if the world is null.
	 */
	public void advanceTick() {
		if (Minecraft.getInstance().level != null) {
			advanceServerTick();
		} else {
			advanceClientTick();
		}
	}

	/**
	 * <p>Sends a {@link TASmodPackets#TICKRATE_ADVANCE} packet to the server
	 */
	public void advanceServerTick() {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKRATE_ADVANCE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>Advances the game by 1 tick. Doesn't send a packet to the server
	 */
	public void advanceClientTick() {
		if (ticksPerSecond == 0) {
			advanceTick = true;
			changeClientTickrate(tickrateSaved);
		}
	}

	/**
	 * <p>Increases the tickrate to the next value of {@link #rateIndex} in {@link #rates}
	 */
	public void increaseTickrate() {
		rateIndex = findClosestRateIndex(ticksPerSecond);
		rateIndex++;
		rateIndex = (short) clamp(rateIndex, 0, rates.length - 1);
		changeTickrate(rates[rateIndex]);
	}

	/**
	 * <p>Decreases the tickrate to the previous value of {@link #rateIndex} in {@link #rates}
	 */
	public void decreaseTickrate() {
		rateIndex = findClosestRateIndex(ticksPerSecond);
		rateIndex--;
		rateIndex = (short) clamp(rateIndex, 0, rates.length - 1);
		changeTickrate(rates[rateIndex]);
	}

	public void joinServer() {
		changeServerTickrate(ticksPerSecond);
	}

	private static void log(String msg) {
		LOGGER.debug(LoggerMarkers.Tickrate, msg);
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { TASmodPackets.TICKRATE_CHANGE, TASmodPackets.TICKRATE_ADVANCE, TASmodPackets.TICKRATE_ZERO };
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
				TickratePauseState state = TASmodBufferBuilder.readEnum(TickratePauseState.class, buf);

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

	/**
	 * <p>Finds the nearest rate index from the current tickrate
	 * @param tickrate The current tickrate to find the rateIndex for
	 * @return The rateIndex
	 */
	private short findClosestRateIndex(float tickrate) {
		for (int i = 0; i < rates.length; i++) {
			int iMinus1 = i - 1;

			float min = 0f;
			if (iMinus1 >= 0) {
				min = rates[iMinus1];
			}
			float max = rates[i];

			if (tickrate >= min && tickrate < max) {
				if (min == 0f) {
					return (short) i;
				}

				float distanceToMin = tickrate - min;
				float distanceToMax = max - tickrate;

				if (distanceToMin < distanceToMax) {
					return (short) iMinus1;
				} else if (distanceToMax < distanceToMin) {
					return (short) i;
				} else {
					return (short) iMinus1;
				}
			}
		}
		return (short) (rates.length - 1);
	}

	/**
	 * Basic clamping method
	 * @param value The value to clamp
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return The clamped value
	 */
	private static int clamp(long value, int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException(min + " > " + max);
		}
		return (int) Math.min(max, Math.max(value, min));
	}

	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		if (ticksPerSecond == 0) {
			timeOffset += System.currentTimeMillis() - timestampSinceLastGameLoop;
			timestampSinceLastGameLoop = System.currentTimeMillis();
		}
	}
}

