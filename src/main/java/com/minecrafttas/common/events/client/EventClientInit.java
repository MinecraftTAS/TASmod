package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.Minecraft;

public interface EventClientInit extends EventBase {
	
	/**
	 * Fires after the client is initialised
	 * @param mc
	 */
	public void onClientInit(Minecraft mc);
	
	public static void fireOnClientInit(Minecraft mc) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventClientInit) {
				EventClientInit event = (EventClientInit) eventListener;
				event.onClientInit(mc);
			}
		}
	}
}
