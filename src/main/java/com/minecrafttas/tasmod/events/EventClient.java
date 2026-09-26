package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * TASmod specific events fired on the client side
 *
 * @author Scribble
 */
public interface EventClient {

	/**
	 * Fired when the hotbar is drawn on screen
	 */
	@FunctionalInterface
	public static interface EventDrawHotbar extends EventBase {
		/**
		 * Fired when the hotbar is drawn on screen
		 */
		public void onDrawHotbar();
	}

	/**
	 * Fired when a screen in a gui is drawn
	 */
	@FunctionalInterface
	public static interface EventDrawScreen extends EventBase {
		/**
		 * Fired when a screen in a gui is drawn
		 */
		public void onDrawScreen(Screen screen, int xCoordinate, int yCoordinate);
	}

	/**
	 * Fired when drawing something on screen. Ignores F1
	 */
	@FunctionalInterface
	public static interface EventDrawHotbarAlways extends EventBase {
		/**
		 * Fired when the gui is drawn on screen. Ignores F1
		 */
		public void onDrawHotbarAlways();
	}

	/**
	 * Fired at the end of a client tick
	 */
	@FunctionalInterface
	public static interface EventClientTickPost extends EventBase {

		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickPost(Minecraft mc);
	}
}

