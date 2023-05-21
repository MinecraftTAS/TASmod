package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.Minecraft;

public interface EventClientTick extends EventBase{
	
	public void onClientTick(Minecraft mc);
	
	public static void fireOnClientTick(Minecraft mc) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventClientTick) {
				EventClientTick event = (EventClientTick) eventListener;
				event.onClientTick(mc);
			}
		}
	}
}
