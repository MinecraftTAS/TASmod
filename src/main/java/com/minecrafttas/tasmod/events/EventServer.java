package com.minecrafttas.tasmod.events;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import net.minecraft.server.MinecraftServer;

import java.io.File;

/**
 * TASmod specific events fired on the server side
 *
 * @author Scribble
 */
public interface EventServer {
	
	/**
	 * Fired when saving a savestate, before the savestate folder is copied
	 */
	@FunctionalInterface
	public static interface EventSavestate extends EventBase {
		
		/**
		 * Fired when saving a savestate, before the savestate folder is copied
		 * @param index The savestate index for this savestate
		 * @param target Target folder, where the savestate is copied to
		 * @param current The current folder that will be copied from
		 */
		public void onSavestateEvent(int index, File target, File current);
	}
	
	/**
	 * Fired when loading a savestate, before the savestate folder is copied
	 */
	@FunctionalInterface
	public static interface EventLoadstate extends EventBase {
		
		/**
		 * Fired when loading a savestate, before the savestate folder is copied
		 * @param index The savestate index for this loadstate
		 * @param target Target folder, where the savestate is copied to
		 * @param current The current folder that will be copied from
		 */
		public void onLoadstateEvent(int index, File target, File current);
	}

	
	/**
	 * Fired one tick after a loadstate was carried out
	 */
	@FunctionalInterface
	public static interface EventCompleteLoadstate extends EventBase{
		
		/**
		 * Fired one tick after a loadstate was carried out
		 */
		public void onLoadstateComplete();
	}
	
	/**
	 * Fired at the end of a server tick
	 */
	@FunctionalInterface
	public static interface EventServerTickPost extends EventBase{
		
		/**
		 * Fired at the end of a server tick
		 */
		public void onServerTickPost(MinecraftServer minecraftServer);
	}

	
	/**
	 * Fired when the tickrate changes on the server side
	 */
	@FunctionalInterface
	public static interface EventServerTickrateChange extends EventBase{
		
		/**
		 * Fired at the end of a client tick
		 */
		public void onServerTickrateChange(float tickrate);
	}
}
