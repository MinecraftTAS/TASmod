package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;

public class TickEvents {
	
	public static void onClientTick() {
		TASmod.ktrngHandler.updateClient();
	}
	
	public static void onServerTick() {
	}
}
