package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.TASmod;

public class TickEvents {
	
	public static void onClientTick() {
		TASmod.ktrngHandler.updateClient();
	}
	
	public static void onServerTick() {
		TASmod.ktrngHandler.updateServer();
	}
}
