package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.gui.GuiScreen;

public interface EventOpenGui extends EventBase{
	
	public GuiScreen onOpenGui(GuiScreen gui);
	
	public static GuiScreen fireOpenGuiEvent(GuiScreen gui) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventOpenGui) {
				EventOpenGui event = (EventOpenGui) eventListener;
				GuiScreen newGui = event.onOpenGui(gui);
				if(newGui != gui) {
					return newGui;
				}
			}
		}
		return gui;
	}
}
