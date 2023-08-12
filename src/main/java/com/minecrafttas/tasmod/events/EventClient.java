package com.minecrafttas.tasmod.events;

import com.minecrafttas.common.events.EventListenerRegistry;
import com.minecrafttas.common.events.EventListenerRegistry.EventBase;

import net.minecraft.client.Minecraft;

public interface EventClient {
	
	/**
	 * Fired when the hotbar is drawn on screen
	 * @author Scribble
	 *
	 */
	public static interface EventDrawHotbar extends EventBase{
		/**
		 * Fired when the hotbar is drawn on screen
		 */
		public void onDrawHotbar();
		
		public static void fireOnDrawHotbar() {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventDrawHotbar) {
					EventDrawHotbar event = (EventDrawHotbar) eventListener;
					event.onDrawHotbar();
				}
			}
		}
	}
	
	/**
	 * Fired at the end of a client tick
	 * @author Scribble
	 *
	 */
	public static interface EventClientTickPost extends EventBase{
		
		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickPost(Minecraft mc);
		
		public static void fireOnClientPostTick(Minecraft mc) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventClientTickPost) {
					EventClientTickPost event = (EventClientTickPost) eventListener;
					event.onClientTickPost(mc);
				}
			}
		}
	}
}
