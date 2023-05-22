package com.minecrafttas.tasmod.events.server;

import java.io.File;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.tasmod.events.TASmodEventListener;

public interface EventLoadstate extends EventBase {
	
	public void onLoadstateEvent(int index, File target, File current);
	
	public static void fireSavestateEvent(int index, File target, File current) {
		for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
			if(eventListener instanceof EventLoadstate) {
				EventLoadstate event = (EventLoadstate) eventListener;
				event.onLoadstateEvent(index, target, current);
			}
		}
	}
}
