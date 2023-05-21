package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.gui.Gui;

public interface EventOpenGui extends EventBase{
	
	public void onOpenGui(Gui gui);
	
	public static void fireOpenGuiEvent(Gui gui) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventOpenGui) {
				EventOpenGui event = (EventOpenGui) eventListener;
				event.onOpenGui(gui);
			}
		}
	}
}
