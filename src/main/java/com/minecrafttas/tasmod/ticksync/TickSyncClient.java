package com.minecrafttas.tasmod.ticksync;

import com.minecrafttas.server.Client;
import com.minecrafttas.server.SecureList;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import lombok.var;
import net.minecraft.client.Minecraft;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class manages tick sync
 * German: https://1drv.ms/p/s!Av_ysXerhm5CphLvLvguvL5QYe1A?e=MHPldP
 * English: https://1drv.ms/p/s!Av_ysXerhm5Cpha7Qq2tiVebd4DY?e=pzxOva
 *
 * @author Pancake
 */
public class TickSyncClient {

	public static final AtomicBoolean shouldTick = new AtomicBoolean(true);
	
	/**
	 * Handles incoming tick packets from the server to the client
	 * This will simply tick the client as long as the tick is correct
	 *
	 * @param uuid Server UUID, null
	 * @param tick Current tick of the server
	 */
	public static void onPacket() {
		shouldTick.set(true);
	}

	/**
	 * Called after a client tick. This will send a packet
	 * to the server making it tick
	 *
	 * @param mc Instance of Minecraft
	 */
	public static void clientPostTick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		
		try {
			// notify server of tick pass
			var bufIndex = SecureList.POOL.available();
			TASmodClient.client.write(bufIndex, SecureList.POOL.lock(bufIndex).putInt(Client.ServerPackets.NOTIFY_SERVER_OF_TICK_PASS.ordinal()));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to server:", e);
		}
	}
	
}
