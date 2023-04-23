package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.inputcontainer.InputContainer;
import com.minecrafttas.tasmod.inputcontainer.TASstate;
import com.minecrafttas.tasmod.inputcontainer.server.ContainerStateClient;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;

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
			InputContainer container = ClientProxy.virtual.getContainer();
			if(stateWhenOpened == TASstate.RECORDING) {
				long seed = TASmod.ktrngHandler.getGlobalSeedClient();
				container.setStartSeed(seed);
			}
			container.setTASState(stateWhenOpened);
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
			ContainerStateClient.setOrSend(stateWhenOpened);
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
			TASmod.logger.info("Unpausing the game again");
			waszero = false;
			TickrateChangerClient.pauseGame(true);
		}
	}

}
