package com.minecrafttas.common.events.client.player;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;
import com.mojang.authlib.GameProfile;

public interface EventOtherPlayerJoinedClientSide extends EventBase {

	public void onOtherPlayerJoinedClientSide(GameProfile profile);
	
	public static void fireOtherPlayerJoinedClientSide(GameProfile profile) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventOtherPlayerJoinedClientSide) {
				EventOtherPlayerJoinedClientSide event = (EventOtherPlayerJoinedClientSide) eventListener;
				event.onOtherPlayerJoinedClientSide(profile);
			}
		}
	}

}
