package com.minecrafttas.tasmod.events;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.File;

import com.minecrafttas.common.events.EventListenerRegistry;
import com.minecrafttas.common.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.server.MinecraftServer;

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
			LOGGER.trace(LoggerMarkers.Event, "SavestateEvent {} {} {}", index, target, current);
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
			LOGGER.trace(LoggerMarkers.Event, "LoadstateEvent {} {} {}", index, target, current);
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
			LOGGER.trace(LoggerMarkers.Event, "LoadstateCompleteEvent");
			for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
				if(eventListener instanceof EventCompleteLoadstate) {
					EventCompleteLoadstate event = (EventCompleteLoadstate) eventListener;
					event.onLoadstateComplete();
				}
			}
		}
	}
	
	/**
	 * Fired at the end of a server tick
	 * @author Scribble
	 * 
	 */
	public static interface EventServerTickPost extends EventBase{
		
		/**
		 * Fired at the end of a server tick
		 */
		public void onServerTickPost(MinecraftServer minecraftServer);
		
		public static void fireServerTickPost(MinecraftServer minecraftServer) {
			LOGGER.trace(LoggerMarkers.Event, "ServerTickPostEvent");
			for (EventBase eventListener : TASmodEventListener.getEventListeners()) {
				if(eventListener instanceof EventServerTickPost) {
					EventServerTickPost event = (EventServerTickPost) eventListener;
					event.onServerTickPost(minecraftServer);
				}
			}
		}
	}

	
	/**
	 * Fired when the tickrate changes on the server side
	 * @author Scribble
	 *
	 */
	public static interface EventServerTickrateChange extends EventBase{
		
		/**
		 * Fired at the end of a client tick
		 */
		public void onServerTickrateChange(float tickrate);
		
		public static void fireOnServerTickrateChange(float tickrate) {
			for (EventBase eventListener : EventListenerRegistry.getEventListeners()) {
				if(eventListener instanceof EventServerTickrateChange) {
					EventServerTickrateChange event = (EventServerTickrateChange) eventListener;
					event.onServerTickrateChange(tickrate);
				}
			}
		}
	}
}
