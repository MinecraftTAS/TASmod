package com.minecrafttas.mctcommon.events;

import com.minecrafttas.mctcommon.MCTCommon;
import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.mctcommon.server.Client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public interface EventServer {

	/**
	 * Fired, when the server is initialised, for both integrated and dedicated server.
	 * @author Scribble
	 *
	 */
	public static interface EventServerInit extends EventBase {

		/**
		 * Fired, when the server is initialised, for both integrated and dedicated server.
		 * @param server The server 
		 */
		public void onServerInit(MinecraftServer server);

		public static void fireServerStartEvent(MinecraftServer server) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing ServerStartEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventServerInit) {
					EventServerInit event = (EventServerInit) eventListener;
					event.onServerInit(server);
				}
			}
		}
	}
	
	/**
	 * Fired when the server ticks.
	 * @author Scribble
	 *
	 */
	public static interface EventServerTick extends EventBase {
		
		/**
		 * Fired when the server ticks.
		 * @param server The ticking server
		 */
		public void onServerTick(MinecraftServer server);

		public static void fireOnServerTick(MinecraftServer server) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventServerTick) {
					EventServerTick event = (EventServerTick) eventListener;
					event.onServerTick(server);
				}
			}
		}
	}

	/**
	 * Fired when the server is about to stop
	 * @author Scribble
	 *
	 */
	public static interface EventServerStop extends EventBase {

		/**
		 * Fired when the server is about to stop
		 * <h2>WARNING!</h2>
		 * This method may run twice!<br><br>
		 * 
		 * @param server The stopping server
		 */
		public void onServerStop(MinecraftServer server);

		public static void fireOnServerStop(MinecraftServer server) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing ServerStopEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventServerStop) {
					EventServerStop event = (EventServerStop) eventListener;
					event.onServerStop(server);
				}
			}
		}
	}


	/**
	 * Fired on a server game loop, which is independent from ticks
	 * @author Scribble
	 *
	 */
	public static interface EventServerGameLoop extends EventBase {
		/**
		 * Fired on a server game loop, which is independent from ticks
		 * @param server The server this event is fired on
		 */
		public void onRunServerGameLoop(MinecraftServer server);

		public static void fireOnServerGameLoop(MinecraftServer server) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventServerGameLoop) {
					EventServerGameLoop event = (EventServerGameLoop) eventListener;
					event.onRunServerGameLoop(server);
				}
			}
		}
	}

	public static interface EventPlayerJoinedServerSide extends EventBase {

		public void onPlayerJoinedServerSide(EntityPlayerMP player);

		public static void firePlayerJoinedServerSide(EntityPlayerMP player) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing PlayerJoinedServerSide");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventPlayerJoinedServerSide) {
					EventPlayerJoinedServerSide event = (EventPlayerJoinedServerSide) eventListener;
					event.onPlayerJoinedServerSide(player);
				}
			}
		}
	}
	
	/**
	 * Fired when a player leaves the server.
	 * @author Scribble
	 *
	 */
	public static interface EventPlayerLeaveServerSide extends EventBase {

		/**
		 * Fired when a player leaves the server.
		 * @param player The player that is leaving
		 */
		public void onPlayerLeaveServerSide(EntityPlayerMP player);

		public static void firePlayerLeaveServerSide(EntityPlayerMP player) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing PlayerLeaveServerSideEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventPlayerLeaveServerSide) {
					EventPlayerLeaveServerSide event = (EventPlayerLeaveServerSide) eventListener;
					event.onPlayerLeaveServerSide(player);
				}
			}
		}
	}
	
	/**
	 * Fired when  authentication was successful on the server side
	 */
	public static interface EventClientCompleteAuthentication extends EventBase {
		
		/**
		 * Fired when  authentication was successful on the server side
		 * @param username The username of the client that is connecting
		 */
		public void onClientCompleteAuthentication(String username);
		
		public static void fireClientCompleteAuthentication(String username) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing ClientCompleteAuthenticationEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventClientCompleteAuthentication) {
					EventClientCompleteAuthentication event = (EventClientCompleteAuthentication) eventListener;
					event.onClientCompleteAuthentication(username);
				}
			}
		}
	}
	
	/**
	 * Fired when the connection to the custom server was closed on the server side.
	 */
	public static interface EventDisconnectServer extends EventBase {
		
		/**
		 * Fired when the connection to the custom server was closed on the server side.
		 * @param client The client that is disconnecting
		 */
		public void onDisconnectServer(Client client);
		
		public static void fireDisconnectServer(Client client) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing CustomServerClientDisconnect");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventDisconnectServer) {
					EventDisconnectServer event = (EventDisconnectServer) eventListener;
					event.onDisconnectServer(client);
				}
			}
		}
	}
}