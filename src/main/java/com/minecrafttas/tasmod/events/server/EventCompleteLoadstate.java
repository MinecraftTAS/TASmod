package com.minecrafttas.tasmod.events.server;

import com.minecrafttas.common.events.EventListener.EventBase;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.TASmodEventListener;
import com.minecrafttas.tasmod.util.LoggerMarkers;

public interface EventCompleteLoadstate extends EventBase{
	
	/**
	 * Fired one tick after a loadstate was carried out
	 */
	public void onLoadstateComplete();
	
	public static void fireLoadstateComplete() {
		TASmod.logger.trace(LoggerMarkers.Event, "LoadstateCompleteEvent");
		for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
			if(eventListener instanceof EventCompleteLoadstate) {
				EventCompleteLoadstate event = (EventCompleteLoadstate) eventListener;
				event.onLoadstateComplete();
			}
		}
	}
}
