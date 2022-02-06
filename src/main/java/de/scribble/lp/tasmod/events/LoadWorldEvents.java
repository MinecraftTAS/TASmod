package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;

public class LoadWorldEvents {

	public static boolean waszero = false;
	public static int cd = -1;

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
			TickrateChangerClient.pauseClientGame(true);
			TickrateChangerServer.pauseServerGame(true);
			TickSync.resetTickCounter();
			TickSyncServer.resetTickCounter();
		}
	}

	/* The following code is for integrated and dedicated server! */

	/**
	 * Executed when the server is shutting down. If the tickrate is 0 the server
	 * might freeze and never shutdown
	 */
	public static void startShutdown() {
		if (TickrateChangerServer.ticksPerSecond == 0 || TickrateChangerServer.advanceTick) {
			TickrateChangerServer.pauseGame(false);
		}
	}

	public static void doneShuttingDown() {
	}

	public static void doneLoadingClientWorld() {
		TASmod.logger.debug("Finished loading the world on the client");
		cd = 1;
	}

	public static void doneWithLoadingScreen() {
		if (cd > -1) {
			if (cd == 0) {
				TASmod.logger.debug("Finished loading screen on the client");
				if (!waszero) {
					TickrateChangerClient.pauseGame(false);
				} else {
					waszero = false;
				}
			}
			cd--;
		}
	}

}
