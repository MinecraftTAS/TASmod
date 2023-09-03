package com.minecrafttas.tasmod.events;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;

import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;

@Deprecated
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
		if (TASmodClient.tickratechanger.ticksPerSecond == 0 || TASmodClient.tickratechanger.advanceTick) {
			LOGGER.info("Pausing game during GuiControls");
			TASmodClient.tickratechanger.pauseGame(false);
			TASmodClient.virtual.getContainer().setTASState(stateWhenOpened);
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
			LOGGER.info("Unpausing the game again");
			waszero = false;
			TASmodClient.tickratechanger.pauseGame(true);
		}
	}

}
