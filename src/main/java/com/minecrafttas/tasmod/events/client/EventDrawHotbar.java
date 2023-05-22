package com.minecrafttas.tasmod.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

public interface EventDrawHotbar extends EventBase{
	
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
