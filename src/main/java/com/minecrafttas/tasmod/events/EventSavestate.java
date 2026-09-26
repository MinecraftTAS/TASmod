package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.SavestatePaths;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;

public interface EventSavestate {

	/**
	 * Fired when saving a savestate, before the savestate folder is copied
	 */
	@FunctionalInterface
	interface EventServerSavestate extends EventBase {

		/**
		 * Fired when saving a savestate, before the savestate folder is copied
		 * 
		 * @param server The server instance
		 * @param paths The {@link SavestatePaths} object
		 */
		public void onServerSavestate(MinecraftServer server, SavestatePaths paths);
	}

	/**
	 * Fired when loading a savestate, before the savestate folder is copied
	 */
	@FunctionalInterface
	interface EventServerLoadstate extends EventBase {

		/**
		 * Fired when loading a savestate, before the savestate folder is copied
		 * 
		 * @param server The server instance
		 * @param paths The {@link SavestatePaths} object
		 */
		public void onServerLoadstate(MinecraftServer server, SavestatePaths paths);
	}

	/**
	 * Fired one tick after a loadstate was carried out
	 */
	@FunctionalInterface
	interface EventServerCompleteLoadstate extends EventBase {

		/**
		 * Fired one tick after a loadstate was carried out
		 */
		public void onServerLoadstateComplete();
	}

	/**
	 * Fired when saving a savestate
	 */
	@FunctionalInterface
	interface EventClientSavestate extends EventBase {

		public void onClientSavestate();
	}

	/**
	 * Fired when loading a savestate
	 */
	@FunctionalInterface
	interface EventClientLoadstate extends EventBase {

		public void onClientLoadstate();
	}

	/**
	 * Fired one tick after a loadstate was carried out
	 */
	@FunctionalInterface
	interface EventClientCompleteLoadstate extends EventBase {

		/**
		 * Fired one tick after a loadstate was carried out
		 */
		public void onClientLoadstateComplete();
	}

	/**
	 * Fired during loadstating, after the player is loaded on the client
	 */
	@FunctionalInterface
	interface EventClientLoadPlayer extends EventBase {

		/**
		 * Fired during loadstating, after the player is loaded on the client
		 */
		public void onClientLoadPlayer(LocalPlayer player);
	}
}

