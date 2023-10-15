package com.minecrafttas.common.events;

import com.minecrafttas.common.Common;
import com.minecrafttas.common.events.EventListenerRegistry.EventBase;

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
			Common.LOGGER.trace(Common.Event, "Firing ServerStartEvent");
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
		 * @param server The stopping server
		 */
		public void onServerStop(MinecraftServer server);

		public static void fireOnServerStop(MinecraftServer server) {
			Common.LOGGER.trace(Common.Event, "Firing ServerStopEvent");
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
			Common.LOGGER.trace(Common.Event, "Firing PlayerJoinedServerSide");
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
			Common.LOGGER.trace(Common.Event, "Firing PlayerLeaveServerSideEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventPlayerLeaveServerSide) {
					EventPlayerLeaveServerSide event = (EventPlayerLeaveServerSide) eventListener;
					event.onPlayerLeaveServerSide(player);
				}
			}
		}
	}
	
	public static interface EventClientCompleteAuthentication extends EventBase {
		
		/**
		 * Fired when  authentication was successful on the server side
		 */
		public void onClientCompleteAuthentication(String username);
		
		public static void fireClientCompleteAuthentication(String username) {
			Common.LOGGER.trace(Common.Event, "Firing ClientCompleteAuthenticationEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if (eventListener instanceof EventClientCompleteAuthentication) {
					EventClientCompleteAuthentication event = (EventClientCompleteAuthentication) eventListener;
					event.onClientCompleteAuthentication(username);
				}
			}
		}
	}
}
