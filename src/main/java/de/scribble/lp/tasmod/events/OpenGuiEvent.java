package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;

public class OpenGuiEvent {
	/**
	 * The state that should be used when the main menu opens
	 */
	public static TASstate stateWhenOpened = null;

	/**
	 * Called when the main menu opens
	 * @param guiMainMenu The menu that was opened
	 */
	public static void openGuiMainMenu(GuiMainMenu guiMainMenu) {
		if (stateWhenOpened != null) {
			ClientProxy.virtual.getContainer().setTASState(stateWhenOpened);
			stateWhenOpened = null;
		}
	}

	/**
	 * Called when the Ingame Menu opens
	 * @param guiIngameMenu The menu that was opened
	 */
	public static void openGuiIngameMenu(GuiIngameMenu guiIngameMenu) {
	}
	

}
