package com.minecrafttas.tasmod.ticksync;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minecrafttas.server.ByteBufferBuilder;
import com.minecrafttas.server.interfaces.ClientPacketHandler;
import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.TASmodPackets;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;

import net.minecraft.client.Minecraft;

/**
 * This class manages tick sync
 * German: https://1drv.ms/p/s!Av_ysXerhm5CphLvLvguvL5QYe1A?e=MHPldP
 * English: https://1drv.ms/p/s!Av_ysXerhm5Cpha7Qq2tiVebd4DY?e=pzxOva
 *
 * @author Pancake
 */
public class TickSyncClient implements ClientPacketHandler, EventClientTickPost{

	public static final AtomicBoolean shouldTick = new AtomicBoolean(true);
	
	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {TASmodPackets.TICKSYNC};
	}

	
	/**
	 * Handles incoming tick packets from the server to the client
	 * This will simply tick the client as long as the tick is correct
	 *
	 * @param uuid Server UUID, null
	 * @param tick Current tick of the server
	 */
	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) {
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
		if (mc.player == null) {
			return;
		}
		
		try {
			TASmodClient.client.send(new ByteBufferBuilder(TASmodPackets.TICKSYNC));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to server:", e);
		}
	}
}
