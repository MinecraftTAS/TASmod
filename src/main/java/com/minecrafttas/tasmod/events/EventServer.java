package com.minecrafttas.tasmod.events;

import java.io.File;

import com.minecrafttas.common.events.EventListener.EventBase;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.util.LoggerMarkers;

public interface EventServer {
	
	/**
	 * Fired when saving a savestate, before the savestate folder is copied
	 * @author Scribble
	 *
	 */
	public static interface EventSavestate extends EventBase {
		
		/**
		 * Fired when saving a savestate, before the savestate folder is copied
		 * @param index The savestate index for this savestate
		 * @param target Target folder, where the savestate is copied to
		 * @param current The current folder that will be copied from
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
	
	/**
	 * Fired when loading a savestate, before the savestate folder is copied
	 * @author Scribble
	 *
	 */
	public static interface EventLoadstate extends EventBase {
		
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

	
	/**
	 * Fired one tick after a loadstate was carried out
	 * @author Scribble
	 * 
	 */
	public static interface EventCompleteLoadstate extends EventBase{
		
		/**
		 * Fired one tick after a loadstate was carried out
		 */
		public void onLoadstateComplete();
		
		public static void fireLoadstateComplete() {
			TASmod.logger.trace(LoggerMarkers.Event, "LoadstateCompleteEvent");
			for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
				if(eventListener instanceof EventCompleteLoadstate) {
					EventCompleteLoadstate event = (EventCompleteLoadstate) eventListener;
					event.onLoadstateComplete();
				}
			}
		}
	}

}
