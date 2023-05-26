package com.minecrafttas.tasmod.events.client;

import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.common.events.EventListener.EventBase;

public interface EventDrawHotbar extends EventBase{
	
	public void onDrawHotbar();
	
	public static void fireOnDrawHotbar() {
		// No logging, because it is literally rendered every frame... This would spam the console even more
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventDrawHotbar) {
				EventDrawHotbar event = (EventDrawHotbar) eventListener;
				event.onDrawHotbar();
			}
		}
	}
}
