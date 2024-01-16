package com.minecrafttas.mctcommon.events;

import com.minecrafttas.mctcommon.MCTCommon;
import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.mctcommon.server.Client;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;

public interface EventClient {
	
	/**
	 * Fired when a gui is opened (Minecraft#displayGuiScreen)
	 * @author Scribble
	 *
	 */
	public static interface EventOpenGui extends EventBase {
		
		/**
		 * Fired when a gui is opened (Minecraft#displayGuiScreen)
		 * @param gui The gui that is opened
		 * @return
		 */
		public GuiScreen onOpenGui(GuiScreen gui);
		
		public static GuiScreen fireOpenGuiEvent(GuiScreen gui) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing OpenGuiEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventOpenGui) {
					EventOpenGui event = (EventOpenGui) eventListener;
					GuiScreen newGui = event.onOpenGui(gui);
					if(newGui != gui) {
						return newGui;
					}
				}
			}
			return gui;
		}
	}
	
	/**
	 * Fired when the integrated server is launched
	 * @author Scribble
	 *
	 */
	public static interface EventLaunchIntegratedServer extends EventBase {
		
		/**
		 * Fired when the integrated server is launched
		 */
		public void onLaunchIntegratedServer();
		
		public static void fireOnLaunchIntegratedServer() {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing LaunchIntegratedServer");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventLaunchIntegratedServer) {
					EventLaunchIntegratedServer event = (EventLaunchIntegratedServer) eventListener;
					event.onLaunchIntegratedServer();
				}
			}
		}
	}

	/**
	 * Fired when the world is done loading, before the player joined the world
	 * @author Scribble
	 *
	 */
	public static interface EventDoneLoadingWorld extends EventBase {

		/**
		 * Fired when the world is done loading, before the player joined the world
		 */
		public void onDoneLoadingWorld();
		
		public static void fireOnDoneLoadingWorld() {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing DoneLoadingWorld");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventDoneLoadingWorld) {
					EventDoneLoadingWorld event = (EventDoneLoadingWorld) eventListener;
					event.onDoneLoadingWorld();
				}
			}
		}
	}
	
	/**
	 * Fired when the client ticks
	 * @author Scribble
	 *
	 */
	public static interface EventClientTick extends EventBase {
		
		/**
		 * Fired when the client ticks
		 * @param mc The ticking Minecraft instance
		 */
		public void onClientTick(Minecraft mc);
		
		public static void fireOnClientTick(Minecraft mc) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventClientTick) {
					EventClientTick event = (EventClientTick) eventListener;
					event.onClientTick(mc);
				}
			}
		}
	}
	
	/**
	 * Fires after the client is initialised
	 * @author Scribble
	 *
	 */
	public static interface EventClientInit extends EventBase {
		
		/**
		 * Fires after the client is initialised
		 * @param mc The initialized Minecraft instance
		 */
		public void onClientInit(Minecraft mc);
		
		public static void fireOnClientInit(Minecraft mc) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing ClientInit");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventClientInit) {
					EventClientInit event = (EventClientInit) eventListener;
					event.onClientInit(mc);
				}
			}
		}
	}

	/**
	 * Fired when when the client runs a game loop, which is tick independent
	 * @author Scribble
	 *
	 */
	public static interface EventClientGameLoop extends EventBase {
		
		/**
		 * Fired when when the client runs a game loop, which is tick independent
		 * @param mc The Minecraft instance that is looping
		 */
		public void onRunClientGameLoop(Minecraft mc);
		
		public static void fireOnClientGameLoop(Minecraft mc) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventClientGameLoop) {
					EventClientGameLoop event = (EventClientGameLoop) eventListener;
					event.onRunClientGameLoop(mc);
				}
			}
		}
	}

	/**
	 * Fired when the camera is updated
	 * @author Scribble
	 *
	 */
	public static interface EventCamera extends EventBase {
		
		/**
		 * Fired when the camera is updated
		 * @param dataIn The pitch and yaw of the camera
		 * @return The changed camera data. Can be changed during the event
		 */
		public CameraData onCameraEvent(CameraData dataIn);
		
		public static CameraData fireCameraEvent(CameraData dataIn) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventCamera) {
					EventCamera event = (EventCamera) eventListener;
					CameraData data = event.onCameraEvent(dataIn);
					if(!data.equals(dataIn)) {
						return data;
					}
				}
			}
			return dataIn;
		}
		
		public static class CameraData{
			public float pitch;
			public float yaw;
			public float roll;
			
			public CameraData(float pitch, float yaw) {
				this(pitch, yaw, 0f);
			}
			
			public CameraData(float pitch, float yaw, float roll) {
				this.pitch = pitch;
				this.yaw = yaw;
				this.roll = roll;
			}
			
			@Override
			public boolean equals(Object obj) {
				if(obj instanceof CameraData) {
					CameraData b = (CameraData) obj;
					return b.pitch == pitch && b.yaw == yaw;
				}
				return super.equals(obj);
			}
		}
	}

	/**
	 * Fired when a player leaves a server or a world
	 * @author Scribble
	 *
	 */
	public static interface EventPlayerLeaveClientSide extends EventBase {
		
		/**
		 * Fired when a player leaves a server or a world
		 * @param player The player that leaves the server or the world
		 */
		public void onPlayerLeaveClientSide(EntityPlayerSP player);
		
		public static void firePlayerLeaveClientSide(EntityPlayerSP player) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing PlayerLeaveClientSideEvent");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventPlayerLeaveClientSide) {
					EventPlayerLeaveClientSide event = (EventPlayerLeaveClientSide) eventListener;
					event.onPlayerLeaveClientSide(player);
				}
			}
		}
	}
	
	/**
	 * Fired when a player joins a server or a world
	 * @author Scribble
	 *
	 */
	public static interface EventPlayerJoinedClientSide extends EventBase {

		/**
		 * Fired when a player joins a server or a world
		 * @param player The player that joins the server or the world
		 */
		public void onPlayerJoinedClientSide(EntityPlayerSP player);
		
		public static void firePlayerJoinedClientSide(EntityPlayerSP player) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing PlayerJoinedClientSide");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventPlayerJoinedClientSide) {
					EventPlayerJoinedClientSide event = (EventPlayerJoinedClientSide) eventListener;
					event.onPlayerJoinedClientSide(player);
				}
			}
		}

	}

	/**
	 * Fired when a different player other than yourself joins a server or a world
	 * @author Scribble
	 *
	 */
	public static interface EventOtherPlayerJoinedClientSide extends EventBase {

		/**
		 * Fired when a different player other than yourself joins a server or a world
		 * @param player The game profile of the player that joins the server or the world
		 */
		public void onOtherPlayerJoinedClientSide(GameProfile profile);
		

		public static void fireOtherPlayerJoinedClientSide(GameProfile profile) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing OtherPlayerJoinedClientSide");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventOtherPlayerJoinedClientSide) {
					EventOtherPlayerJoinedClientSide event = (EventOtherPlayerJoinedClientSide) eventListener;
					event.onOtherPlayerJoinedClientSide(profile);
				}
			}
		}
		
	}
	
	/**
	 * Fired when the connection to the custom server was closed on the client side.
	 */
	public static interface EventDisconnectClient extends EventBase {
		
		/**
		 * Fired when the connection to the custom server was closed on the client side.
		 * @param client The client that is disconnecting
		 */
		public void onDisconnectClient(Client client);
		
		public static void fireDisconnectClient(Client client) {
			MCTCommon.LOGGER.trace(MCTCommon.Event, "Firing EventDisconnectClient");
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventDisconnectClient) {
					EventDisconnectClient event = (EventDisconnectClient) eventListener;
					event.onDisconnectClient(client);
				}
			}
		}
	}
}