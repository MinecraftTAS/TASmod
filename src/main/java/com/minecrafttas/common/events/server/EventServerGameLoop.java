package com.minecrafttas.common.events.server;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.server.MinecraftServer;

public interface EventServerGameLoop extends EventBase {

	public void onRunServerGameLoop(MinecraftServer server);
	
	public static void fireOnServerGameLoop(MinecraftServer server) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventServerGameLoop) {
				EventServerGameLoop event = (EventServerGameLoop) eventListener;
				event.onRunServerGameLoop(server);
			}
		}
	}
}
