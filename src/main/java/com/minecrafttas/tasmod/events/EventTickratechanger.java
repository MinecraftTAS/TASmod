package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;

public interface EventTickratechanger {

	/**
	 * Fired when the tickrate changes on the client side
	 */
	@FunctionalInterface
	interface EventClientTickrateChange extends EventBase {

		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickrateChange(float tickrate);
	}

	/**
	 * Fired when the tickrate changes on the server side
	 */
	@FunctionalInterface
	interface EventServerTickrateChange extends EventBase {

		/**
		 * Fired at the end of a client tick
		 */
		public void onServerTickrateChange(float tickrate);
	}

}

