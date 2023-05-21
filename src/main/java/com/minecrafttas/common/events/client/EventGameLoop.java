package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.Minecraft;

public interface EventGameLoop {

	public void onRunGameLoop(Minecraft mc);
	
	public static void fireOnRunGameLoop(Minecraft mc) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventGameLoop) {
				EventGameLoop event = (EventGameLoop) eventListener;
				event.onRunGameLoop(mc);
			}
		}
	}
}
