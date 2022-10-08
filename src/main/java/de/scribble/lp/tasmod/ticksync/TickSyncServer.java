package de.scribble.lp.tasmod.ticksync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.Server;
import de.scribble.lp.tasmod.networking.packets.ClientTickSyncPacket;

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
	 * A multithreadable boolean that tells the MixinMinecraftServer to tick the server or not.
	 */
	public static AtomicBoolean shouldTick = new AtomicBoolean(true);

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
				
				if(synchronizedList.size() == TASmod.getServerInstance().getCurrentPlayerCount()) {
					shouldTick.set(true);
					synchronizedList.clear();
				}
			}
		}
	}

	/**
	 * Called after a server tick. This will send a packet
	 * to all clients making them tick
	 */
	public static void serverPostTick() {
		Server.sendPacket(new ClientTickSyncPacket());
	}

}
