package com.minecrafttas.tasmod.ticksync;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;

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
	
	private boolean enabled = true;
	
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
		if (TASmodClient.client == null || TASmodClient.client.isClosed() || !enabled) {
			return;
		}
		
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.TICKSYNC));
		} catch (Exception e) {
			LOGGER.error("Unable to send packet to server:", e);
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
