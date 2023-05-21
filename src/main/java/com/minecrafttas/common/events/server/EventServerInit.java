package com.minecrafttas.common.events.server;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.server.MinecraftServer;

public interface EventServerInit extends EventBase{
	
	public void onServerInit(MinecraftServer server);
	
	public static void fireServerStartEvent(MinecraftServer server) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventServerInit) {
				EventServerInit event = (EventServerInit) eventListener;
				event.onServerInit(server);
			}
		}
	}
}
