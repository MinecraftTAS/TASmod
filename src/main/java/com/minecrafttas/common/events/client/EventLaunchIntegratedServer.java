package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

public interface EventLaunchIntegratedServer extends EventBase {

	public void onLaunchIntegratedServer();
	
	public static void fireOnLaunchIntegratedServer() {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventLaunchIntegratedServer) {
				EventLaunchIntegratedServer event = (EventLaunchIntegratedServer) eventListener;
				event.onLaunchIntegratedServer();
			}
		}
	}

}
