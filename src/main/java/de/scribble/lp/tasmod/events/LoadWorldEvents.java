package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;

public class LoadWorldEvents {

	public static boolean waszero = false;
	public static int cd = -1;

	/**
	 * Executed when an integrated server is launched
	 * 
	 * @see de.scribble.lp.tasmod.mixin.events.MixinMinecraft#inject_launchIntegratedServer(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void startLaunchServer() {
		TASmod.logger.debug("Starting the integrated server");
		if (TickrateChangerClient.ticksPerSecond == 0 || TickrateChangerClient.advanceTick) {
			waszero = true;
		}
	}

	/**
	 * Executed when the server is initialising
	 * Side: Integrated Server
	 * 
	 * @see de.scribble.lp.tasmod.mixin.events.MixinMinecraftServer#inject_run(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void initServer() {
		if (!TASmod.getServerInstance().isDedicatedServer()) {
			TASmod.logger.info("Integrated server initialised");
			TickrateChangerClient.pauseClientGame(true);
			TickrateChangerServer.pauseServerGame(true);
		}
	}

	/* The following code is for integrated and dedicated server! */

	/**
	 * Executed when the server is shutting down. If the tickrate is 0 the server
	 * might freeze and never shutdown
	 * 
	 * @see de.scribble.lp.tasmod.mixin.events.MixinMinecraftServer#inject_initiateShutDown(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void startShutdown() {
		if (TickrateChangerServer.ticksPerSecond == 0 || TickrateChangerServer.advanceTick) {
			TickrateChangerServer.pauseGame(false);
		}
	}

	/**
	 * Executed when the player is in the main menu on the client
	 */
	public static void doneShuttingDown() {
		TASmod.logger.info("Server is done shutting down");
	}

	/**
	 * When the client is done loading the world
	 */
	public static void doneLoadingClientWorld() {
		TASmod.logger.info("Finished loading the world on the client");
		if(TASmod.getServerInstance()!=null) {
			cd = 1;
		}
	}

	/**
	 * Executed a frame after the world is done loading
	 */
	public static void doneWithLoadingScreen() {
		if (cd > -1) {
			if (cd == 0) {
				TASmod.logger.info("Finished loading screen on the client");
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
