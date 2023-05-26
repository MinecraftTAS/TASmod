package com.minecrafttas.tasmod.events;

import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.common.events.EventListener.EventBase;

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
			for (EventBase eventListener : EventListener.getEventListeners()) {
				if(eventListener instanceof EventDrawHotbar) {
					EventDrawHotbar event = (EventDrawHotbar) eventListener;
					event.onDrawHotbar();
				}
			}
		}
	}
}
