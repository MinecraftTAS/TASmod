package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.Minecraft;

public interface EventClientGameLoop extends EventBase {

	public void onRunClientGameLoop(Minecraft mc);
	
	public static void fireOnClientGameLoop(Minecraft mc) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventClientGameLoop) {
				EventClientGameLoop event = (EventClientGameLoop) eventListener;
				event.onRunClientGameLoop(mc);
			}
		}
	}
}
