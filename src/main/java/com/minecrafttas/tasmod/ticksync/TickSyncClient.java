package com.minecrafttas.tasmod.ticksync;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;

import net.minecraft.client.Minecraft;

/**
 * <p>Synchronizes the client tickrate with the server tickrate
 * 
 * @author Pancake
 * @see TickSyncServer
 */
public class TickSyncClient implements ClientPacketHandler, EventClientTickPost {

	public static final AtomicBoolean shouldTick = new AtomicBoolean(true);

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { TASmodPackets.TICKSYNC };
	}

	/**
	 * Handles incoming tick packets from the server to the client
	 * This will simply tick the client as long as the tick is correct
	 *
	 * @param uuid Server UUID, null
	 * @param tick Current tick of the server
	 */
	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) {
		shouldTick.set(true);
	}

	/**
	 * Called after a client tick. This will send a packet
	 * to the server making it tick
	 *
	 * @param mc Instance of Minecraft
	 */
	@Override
	public void onClientTickPost(Minecraft mc) {
		if (TASmodClient.client == null || TASmodClient.client.isClosed()) {
			return;
		}

		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKSYNC));
		} catch (Exception e) {
			LOGGER.error("Unable to send packet to server:", e);
		}
	}
}

