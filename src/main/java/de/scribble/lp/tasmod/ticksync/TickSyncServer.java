package de.scribble.lp.tasmod.ticksync;

public class TickSyncServer {
	private static int serverticksync=0;
	private static boolean enabled=true;
	
	public static void sync(boolean enable) {
		enabled=enable;
	}
	public static void incrementServerTickCounter() {
		serverticksync++;
	}
	public static void resetTickCounter(){
		serverticksync=0;
	}
	public static int getServertickcounter() {
		return serverticksync;
	}
	public static boolean isEnabled() {
		return enabled;
	}
}
