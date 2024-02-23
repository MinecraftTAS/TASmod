package com.minecrafttas.mctcommon.events;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.mctcommon.server.Client;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;

/**
 * Contains all events fired on the client side
 *
 * @author Scribble
 */
public interface EventClient {
	
	/**
	 * Fired when a gui is opened (Minecraft#displayGuiScreen)
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventOpenGui extends EventBase {
		
		/**
		 * Fired when a gui is opened (Minecraft#displayGuiScreen)
		 * @param gui The gui that is opened
		 * @return
		 */
		public GuiScreen onOpenGui(GuiScreen gui);
	}
	
	/**
	 * Fired when the integrated server is launched
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventLaunchIntegratedServer extends EventBase {
		
		/**
		 * Fired when the integrated server is launched
		 */
		public void onLaunchIntegratedServer();
	}

	/**
	 * Fired when the world is done loading, before the player joined the world
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventDoneLoadingWorld extends EventBase {

		/**
		 * Fired when the world is done loading, before the player joined the world
		 */
		public void onDoneLoadingWorld();
	}
	
	/**
	 * Fired when the client ticks
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventClientTick extends EventBase {
		
		/**
		 * Fired when the client ticks
		 * @param mc The ticking Minecraft instance
		 */
		public void onClientTick(Minecraft mc);
	}
	
	/**
	 * Fires after the client is initialised
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventClientInit extends EventBase {
		
		/**
		 * Fires after the client is initialised
		 * @param mc The initialized Minecraft instance
		 */
		public void onClientInit(Minecraft mc);
	}

	/**
	 * Fired when when the client runs a game loop, which is tick independent
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventClientGameLoop extends EventBase {
		
		/**
		 * Fired when when the client runs a game loop, which is tick independent
		 * @param mc The Minecraft instance that is looping
		 */
		public void onRunClientGameLoop(Minecraft mc);
	}

	/**
	 * Fired when the camera is updated
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventCamera extends EventBase {
		
		/**
		 * Fired when the camera is updated
		 * @param dataIn The pitch and yaw of the camera
		 * @return The changed camera data. Can be changed during the event
		 */
		public CameraData onCameraEvent(CameraData dataIn);
		
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
	@FunctionalInterface
	public static interface EventPlayerLeaveClientSide extends EventBase {
		
		/**
		 * Fired when a player leaves a server or a world
		 * @param player The player that leaves the server or the world
		 */
		public void onPlayerLeaveClientSide(EntityPlayerSP player);
	}
	
	/**
	 * Fired when a player joins a server or a world
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventPlayerJoinedClientSide extends EventBase {

		/**
		 * Fired when a player joins a server or a world
		 * @param player The player that joins the server or the world
		 */
		public void onPlayerJoinedClientSide(EntityPlayerSP player);
	}

	/**
	 * Fired when a different player other than yourself joins a server or a world
	 * @author Scribble
	 *
	 */
	@FunctionalInterface
	public static interface EventOtherPlayerJoinedClientSide extends EventBase {

		/**
		 * Fired when a different player other than yourself joins a server or a world
		 * @param profile The game profile of the player that joins the server or the world
		 */
		public void onOtherPlayerJoinedClientSide(GameProfile profile);
	}
	
	/**
	 * Fired when the connection to the custom server was closed on the client side.
	 */
	@FunctionalInterface
	public static interface EventDisconnectClient extends EventBase {
		
		/**
		 * Fired when the connection to the custom server was closed on the client side.
		 * @param client The client that is disconnecting
		 */
		public void onDisconnectClient(Client client);
	}
}
