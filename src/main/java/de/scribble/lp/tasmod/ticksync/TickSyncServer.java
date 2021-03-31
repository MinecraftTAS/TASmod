package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.ModLoader;

public class TickSyncServer {
	private static int serverticksync = 0;
	private static boolean enabled = true;

	public static void sync(boolean enable) {
		enabled = enable;
	}

	public static void incrementServerTickCounter() {
		serverticksync++;
	}

	public static void resetTickCounter() {
		ModLoader.getServerInstance().getServer().tickCounter = 0;
		serverticksync = 0;
	}

	public static int getServertickcounter() {
		return serverticksync;
	}

	public static boolean isEnabled() {
		return enabled;
	}
}
