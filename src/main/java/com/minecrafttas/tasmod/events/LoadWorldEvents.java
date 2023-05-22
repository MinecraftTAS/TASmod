package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

public class LoadWorldEvents {

	/**
	 * Delay after the loading screen is finished before firing "doneWithLoadingScreen"
	 */
	private static int loadingScreenDelay = -1;

	/**
	 * Executed when an integrated server is launched
	 * 
	 * @see com.minecrafttas.tasmod.mixin.events.MixinMinecraft#inject_launchIntegratedServer(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void startLaunchServer() {

	}

	/**
	 * Executed when the server is initialising
	 * Side: Integrated Server
	 * 
	 * @see com.minecrafttas.tasmod.mixin.events.MixinIntegratedServer#inject_init(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable)
	 */
	public static void initServer() {
		TASmod.logger.info("Integrated server initialised");
	}

	/* The following code is for integrated and dedicated server! */

	/**
	 * Executed when the server is shutting down. If the tickrate is 0 the server
	 * might freeze and never shutdown
	 * 
	 * @see com.minecrafttas.tasmod.mixin.events.MixinMinecraftServer#inject_initiateShutDown(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static void startShutdown() {
	}

	/**
	 * Executed when the player is in the main menu on the client
	 */
	public static void doneShuttingDown() {
		TASmod.logger.debug("Server is done shutting down");
		TASmodClient.virtual.unpressNext();
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
}
