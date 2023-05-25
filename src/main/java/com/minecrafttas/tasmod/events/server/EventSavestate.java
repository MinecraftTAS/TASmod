package com.minecrafttas.tasmod.events.server;

import java.io.File;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.TASmodEventListener;
import com.minecrafttas.tasmod.util.LoggerMarkers;

/**
 * Fired when saving a savestate, before the savestate folder is copied
 * @author Scribble
 *
 */
public interface EventSavestate extends EventBase {
	
	/**
	 * Fired when saving a savestate, before the savestate folder is copied
	 * @param index
	 * @param target
	 * @param current
	 */
	public void onSavestateEvent(int index, File target, File current);
	
	public static void fireSavestateEvent(int index, File target, File current) {
		TASmod.logger.trace(LoggerMarkers.Event, "SavestateEvent {} {} {}", index, target, current);
		for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
			if(eventListener instanceof EventSavestate) {
				EventSavestate event = (EventSavestate) eventListener;
				event.onSavestateEvent(index, target, current);
			}
		}
	}
}
