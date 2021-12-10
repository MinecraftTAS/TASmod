package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;

public class LoadWorldEvents {

	public static boolean waszero = false;

	/**
	 * Executed when an integrated server is launched
	 */
	public static void startLaunchServer() {
		if (TickrateChangerClient.ticksPerSecond == 0 || TickrateChangerClient.advanceTick) {
			waszero = true;
		}
	}

	public static void doneLaunchingServer() {
		if (!TASmod.getServerInstance().isDedicatedServer()) {
			if (waszero) { // If the server is dedicated: waszero=false;
				TickrateChangerClient.pauseClientGame(true);
				TickrateChangerServer.pauseServerGame(true);
				waszero = false;
			}else {
				TickSync.resetTickCounter();
				TickSyncServer.resetTickCounter();
				TickrateChangerServer.changeServerTickrate(TickrateChangerClient.ticksPerSecond);
			}
		}
	}

	/* The following code is for integrated and dedicated server! */

	public static void startShutdown() {
		if (TickrateChangerServer.ticksPerSecond == 0 || TickrateChangerServer.advanceTick) {
			TickrateChangerServer.pauseGame(false);
			waszero = true;
		}
	}

	public static void doneShuttingDown() {
		if (waszero) {
			TickrateChangerClient.pauseGame(true);
			waszero = false;
		}
	}

}
