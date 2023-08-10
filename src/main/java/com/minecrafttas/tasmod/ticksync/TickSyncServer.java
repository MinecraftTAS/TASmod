package com.minecrafttas.tasmod.ticksync;

import com.minecrafttas.server.Client;
import com.minecrafttas.server.SecureList;
import com.minecrafttas.tasmod.TASmod;
import lombok.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class manages tick sync
 * German: https://1drv.ms/p/s!Av_ysXerhm5CphLvLvguvL5QYe1A?e=MHPldP
 * English: https://1drv.ms/p/s!Av_ysXerhm5Cpha7Qq2tiVebd4DY?e=pzxOva
 *
 * @author Pancake
 */
public class TickSyncServer {
	
	private static List<UUID> synchronizedList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Handles incoming tick packets from the client to the server
	 * This will put the uuid into a list of ticked clients and once every client
	 * is in that list, tick the server.
	 *
	 * @param uuid Player UUID
	 * @param tick Current tick of the player
	 */
	public static void onPacket(UUID uuid) {
		synchronized (synchronizedList) {
			if(!synchronizedList.contains(uuid)) {
				synchronizedList.add(uuid);
			}
		}
	}

	public static boolean shouldTick() {
		synchronized (synchronizedList) {
			int acknowledged = synchronizedList.size();
			int totalConnections = TASmod.server.getClients().size();
			if(acknowledged >= totalConnections) {
				return true;
			}else {
				return false;
			}
		}
	}
	
	/**
	 * Called after a server tick. This will send a packet
	 * to all clients making them tick
	 */
	public static void serverPostTick() {
		try {
			// tick clients
			var bufIndex = SecureList.POOL.available();
			TASmod.server.writeAll(bufIndex, SecureList.POOL.lock(bufIndex).putInt(Client.ClientPackets.TICK_CLIENT.ordinal()));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to all clients:", e);
		}
		if(synchronizedList.size()>0)
			synchronizedList.clear();
	}

	public static void clearList() {
		synchronizedList.clear();
	}
}
