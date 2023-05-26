package com.minecrafttas.tasmod.events.server;

import java.io.File;

import com.minecrafttas.common.events.EventListener.EventBase;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.TASmodEventListener;
import com.minecrafttas.tasmod.util.LoggerMarkers;

/**
 * Fired when loading a savestate, before the savestate folder is copied
 * @author Scribble
 *
 */
public interface EventLoadstate extends EventBase {
	
	/**
	 * Fired when loading a savestate, before the savestate folder is copied
	 * @param index The savestate index for this loadstate
	 * @param target Target folder, where the savestate is copied to
	 * @param current The current folder that will be copied from
	 */
	public void onLoadstateEvent(int index, File target, File current);
	
	public static void fireLoadstateEvent(int index, File target, File current) {
		TASmod.logger.trace(LoggerMarkers.Event, "LoadstateEvent {} {} {}", index, target, current);
		for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
			if(eventListener instanceof EventLoadstate) {
				EventLoadstate event = (EventLoadstate) eventListener;
				event.onLoadstateEvent(index, target, current);
			}
		}
	}
}
