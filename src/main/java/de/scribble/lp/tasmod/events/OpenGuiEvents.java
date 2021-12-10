package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;

public class OpenGuiEvents {
	/**
	 * The state that should be used when the main menu opens
	 */
	public static TASstate stateWhenOpened = null;

	/**
	 * Called when the main menu opens
	 * 
	 * @param guiMainMenu The menu that was opened
	 */
	public static void openGuiMainMenu(GuiMainMenu guiMainMenu) {
		if (stateWhenOpened != null) {
			ClientProxy.virtual.getContainer().setTASState(stateWhenOpened);
			stateWhenOpened = null;
		}
		LoadWorldEvents.doneShuttingDown();
	}

	/**
	 * Called when the Ingame Menu opens
	 * 
	 * @param guiIngameMenu The menu that was opened
	 */
	public static void openGuiIngameMenu(GuiIngameMenu guiIngameMenu) {
	}

	public static boolean waszero;

	/**
	 * Called then the Controls Gui opens
	 * 
	 * @param guiControls The gui that was opened
	 */
	public static void openGuiControls(GuiControls guiControls) {
		if (TickrateChangerClient.ticksPerSecond == 0 || TickrateChangerClient.advanceTick) {
			TASmod.logger.info("Pausing game during GuiControls");
			TickrateChangerClient.pauseGame(false);
			TASstate.setOrSend(TASstate.NONE);
			waszero = true;
		}
	}

	/**
	 * Called then the Controls Gui closes
	 * 
	 * @param guiControls The gui that was opened
	 */
	public static void closeGuiControls(GuiControls guiControls) {
		if (waszero) {
			TASmod.logger.info("Unausing the game again");
			waszero = false;
			TickrateChangerClient.pauseGame(true);
		}
	}

}
