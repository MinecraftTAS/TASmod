package com.minecrafttas.tasmod.events.server;

import java.io.File;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.tasmod.events.TASmodEventListener;

public interface EventSavestate extends EventBase {
	
	public void onSavestateEvent(int index, File target, File current);
	
	public static void fireSavestateEvent(int index, File target, File current) {
		for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
			if(eventListener instanceof EventSavestate) {
				EventSavestate event = (EventSavestate) eventListener;
				event.onSavestateEvent(index, target, current);
			}
		}
	}
}
