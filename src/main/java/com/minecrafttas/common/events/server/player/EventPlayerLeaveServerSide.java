package com.minecrafttas.common.events.server.player;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.entity.player.EntityPlayerMP;

public interface EventPlayerLeaveServerSide extends EventBase {
	
	public void onPlayerLeaveServerSide(EntityPlayerMP player);
	
	public static void firePlayerLeaveServerSide(EntityPlayerMP player) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventPlayerLeaveServerSide) {
				EventPlayerLeaveServerSide event = (EventPlayerLeaveServerSide) eventListener;
				event.onPlayerLeaveServerSide(player);
			}
		}
	}
}
