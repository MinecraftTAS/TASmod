package com.minecrafttas.mctcommon.events;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.mctcommon.server.Client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Contains all events fired on the server side
 *
 * @author Scribble
 */
public interface EventServer {

	/**
	 * Fired, when the server is initialised, for both integrated and dedicated server.
	 */
	@FunctionalInterface
	public static interface EventServerInit extends EventBase {

		/**
		 * Fired, when the server is initialised, for both integrated and dedicated server.
		 * @param server The server 
		 */
		public void onServerInit(MinecraftServer server);
	}
	
	/**
	 * Fired when the server ticks.
	 */
	@FunctionalInterface
	public static interface EventServerTick extends EventBase {
		
		/**
		 * Fired when the server ticks.
		 * @param server The ticking server
		 */
		public void onServerTick(MinecraftServer server);
	}

	/**
	 * Fired when the server is about to stop
	 */
	@FunctionalInterface
	public static interface EventServerStop extends EventBase {

		/**
		 * Fired when the server is about to stop
		 * <h2>WARNING!</h2>
		 * This method may run twice!<br><br>
		 * 
		 * @param server The stopping server
		 */
		public void onServerStop(MinecraftServer server);
	}

	/**
	 * Fired on a server game loop, which is independent from ticks
	 */
	@FunctionalInterface
	public static interface EventServerGameLoop extends EventBase {
		/**
		 * Fired on a server game loop, which is independent from ticks
		 * @param server The server this event is fired on
		 */
		public void onRunServerGameLoop(MinecraftServer server);
	}

	/**
	 * Fired on the server side when a player joins
	 */
	@FunctionalInterface
	public static interface EventPlayerJoinedServerSide extends EventBase {

		/**
		 * Fired when a player joins on the server side
		 * @param player The player that is joining
		 */
		public void onPlayerJoinedServerSide(EntityPlayerMP player);
	}
	
	/**
	 * Fired when a player leaves the server.
	 */
	@FunctionalInterface
	public static interface EventPlayerLeaveServerSide extends EventBase {

		/**
		 * Fired when a player leaves the server.
		 * @param player The player that is leaving
		 */
		public void onPlayerLeaveServerSide(EntityPlayerMP player);
	}
	
	/**
	 * Fired when  authentication was successful on the server side
	 */
	@FunctionalInterface
	public static interface EventClientCompleteAuthentication extends EventBase {
		
		/**
		 * Fired when  authentication was successful on the server side
		 * @param username The username of the client that is connecting
		 */
		public void onClientCompleteAuthentication(String username);
	}
	
	/**
	 * Fired when the connection to the custom server was closed on the server side.
	 */
	@FunctionalInterface
	public static interface EventDisconnectServer extends EventBase {
		
		/**
		 * Fired when the connection to the custom server was closed on the server side.
		 * @param client The client that is disconnecting
		 */
		public void onDisconnectServer(Client client);
	}
}
