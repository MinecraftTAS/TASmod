package com.minecrafttas.common.events.server.player;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

import net.minecraft.entity.player.EntityPlayerMP;

public interface EventPlayerJoinedServerSide extends EventBase{
	
	public void onPlayerJoinedServerSide(EntityPlayerMP player);
	
	public static void firePlayerJoinedServerSide(EntityPlayerMP player) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventPlayerJoinedServerSide) {
				EventPlayerJoinedServerSide event = (EventPlayerJoinedServerSide) eventListener;
				event.onPlayerJoinedServerSide(player);
			}
		}
	}
}
