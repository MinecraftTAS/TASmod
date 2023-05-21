package com.minecrafttas.common.events.server;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.server.MinecraftServer;

public interface EventServerStop extends EventBase{
	
	public void onServerStop(MinecraftServer server);
	
	public static void fireOnServerStop(MinecraftServer server) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventServerStop) {
				EventServerStop event = (EventServerStop) eventListener;
				event.onServerStop(server);
			}
		}
	}
}
