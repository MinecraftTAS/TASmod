package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.gui.GuiMainMenu;

public class OpenGuiEvent {
	/**
	 * The state that should be used when the main menu opens
	 */
	public static TASstate stateWhenOpened=null;
	
	public static void openGuiMainMenu(GuiMainMenu gui) {
		if(stateWhenOpened!=null) {
			ClientProxy.virtual.getContainer().setTASState(stateWhenOpened);
			stateWhenOpened=null;
		}
	}
}
