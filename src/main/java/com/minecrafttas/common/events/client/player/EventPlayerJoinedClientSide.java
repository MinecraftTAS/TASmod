package com.minecrafttas.common.events.client.player;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.client.entity.EntityPlayerSP;

public interface EventPlayerJoinedClientSide extends EventBase {

	public void onPlayerJoinedClientSide(EntityPlayerSP player);
	
	public static void firePlayerJoinedClientSide(EntityPlayerSP player) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventPlayerJoinedClientSide) {
				EventPlayerJoinedClientSide event = (EventPlayerJoinedClientSide) eventListener;
				event.onPlayerJoinedClientSide(player);
			}
		}
	}

}
