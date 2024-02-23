package com.minecrafttas.tasmod.events;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import net.minecraft.client.Minecraft;

/**
 * TASmod specific events fired on the client side
 *
 * @author Scribble
 */
public interface EventClient {
	
	/**
	 * Fired when the hotbar is drawn on screen
	 */
	@FunctionalInterface
	public static interface EventDrawHotbar extends EventBase{
		/**
		 * Fired when the hotbar is drawn on screen
		 */
		public void onDrawHotbar();
	}
	
	/**
	 * Fired at the end of a client tick
	 */
	@FunctionalInterface
	public static interface EventClientTickPost extends EventBase{
		
		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickPost(Minecraft mc);
	}
	
	/**
	 * Fired when the tickrate changes on the client side
	 */
	@FunctionalInterface
	public static interface EventClientTickrateChange extends EventBase{
		
		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickrateChange(float tickrate);
	}
}
