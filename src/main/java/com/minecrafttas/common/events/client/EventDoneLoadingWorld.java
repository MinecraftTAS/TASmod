package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

public interface EventDoneLoadingWorld extends EventBase {

	public void onDoneLoadingWorld();
	
	public static void fireOnDoneLoadingWorld() {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventDoneLoadingWorld) {
				EventDoneLoadingWorld event = (EventDoneLoadingWorld) eventListener;
				event.onDoneLoadingWorld();
			}
		}
	}

}
