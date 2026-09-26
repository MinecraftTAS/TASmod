package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;

import net.minecraft.server.MinecraftServer;

/**
 * TASmod specific events fired on the server side
 *
 * @author Scribble
 */
public interface EventServer {

	/**
	 * Fired at the end of a server tick
	 */
	@FunctionalInterface
	public static interface EventServerTickPost extends EventBase {

		/**
		 * Fired at the end of a server tick
		 */
		public void onServerTickPost(MinecraftServer minecraftServer);
	}
}

