package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;

public class LoadWorldEvents {

	public static boolean waszero = false;
	
	/**
	 * Delay after the loading screen is finished before firing "doneWithLoadingScreen"
	 */
	private static int loadingScreenDelay = -1;

	/**
	 * Executed when an integrated server is launched
	 * 
	 * @see de.scribble.lp.tasmod.mixin.events.MixinMinecraft#inject_launchIntegratedServer(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void startLaunchServer() {
		TASmod.logger.info("Starting the integrated server");
		if (TickrateChangerClient.ticksPerSecond == 0 || TickrateChangerClient.advanceTick) {
			waszero = true;
		}
	}

	/**
	 * Executed when the server is initialising
	 * Side: Integrated Server
	 * 
	 * @see de.scribble.lp.tasmod.mixin.events.MixinIntegratedServer#inject_init(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable)
	 */
	public static void initServer() {
		TASmod.logger.info("Integrated server initialised");
		TickrateChangerClient.pauseClientGame(true);
		TickrateChangerServer.pauseServerGame(true);
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
		TASmod.logger.debug("Server is done shutting down");
		ClientProxy.virtual.unpressNext();
	}

	/**
	 * When the client is done loading the world
	 */
	public static void doneLoadingClientWorld() {
		TASmod.logger.info("Finished loading the world on the client");
		if(TASmod.getServerInstance()!=null) { //Check if a server is running and if it's an integrated server
			loadingScreenDelay = 1;
		}
	}

	/**
	 * Executed a frame after the world is done loading
	 */
	public static void doneWithLoadingScreen() {
		if (loadingScreenDelay > -1) {
			if (loadingScreenDelay == 0) {
				TASmod.logger.info("Finished loading screen on the client");
				if (!waszero) {
					if(TASmod.getServerInstance()!=null) {	//Check if a server is running and if it's an integrated server
						TickrateChangerClient.pauseClientGame(false);
						TickrateChangerServer.pauseServerGame(false);
					}
				} else {
					waszero = false;
				}
			}
			loadingScreenDelay--;
		}
	}

}
