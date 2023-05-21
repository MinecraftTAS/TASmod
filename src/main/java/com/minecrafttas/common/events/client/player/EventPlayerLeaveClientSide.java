package com.minecrafttas.common.events.client.player;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.entity.EntityPlayerSP;

public interface EventPlayerLeaveClientSide extends EventBase {
	
	public void onPlayerLeaveClientSide(EntityPlayerSP player);
	
	public static void firePlayerLeaveClientSide(EntityPlayerSP player) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventPlayerLeaveClientSide) {
				EventPlayerLeaveClientSide event = (EventPlayerLeaveClientSide) eventListener;
				event.onPlayerLeaveClientSide(player);
			}
		}
	}
}
