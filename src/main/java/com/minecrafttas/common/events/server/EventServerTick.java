package com.minecrafttas.common.events.server;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.server.MinecraftServer;

public interface EventServerTick extends EventBase{
	
	public void onServerTick(MinecraftServer server);
	
	public static void fireOnServerTick(MinecraftServer server) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventServerTick) {
				EventServerTick event = (EventServerTick) eventListener;
				event.onServerTick(server);
			}
		}
	}
}
