package de.scribble.lp.tasmod.savestates.client;

import java.io.File;
import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import de.scribble.lp.tasmod.virtual.container.InputContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creating savestates of the inputs on the client<br>
 * Side: Client
 * 
 * @author ScribbleLP
 */
@SideOnly(Side.CLIENT)
public class ClientSavestateHandler {

	private final static File savestateDirectory = new File(ClientProxy.tasdirectory + File.separator + "savestates");

	/**
	 * Makes a copy of the recording that is currently running. Gets triggered when
	 * a savestate is made on the server <br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws SavestateException
	 * @throws IOException
	 */
	public static void savestate(String nameOfSavestate) throws SavestateException, IOException {

		if (nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}

		createSavestateDirectory();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".tas");

		InputContainer container = ClientProxy.virtual.getContainer();
		if (container.isRecording()) {
			ClientProxy.serialiser.saveToFileV1(targetfile, container);
		} else {
			ClientProxy.serialiser.saveToFileV1Until(targetfile, container, container.index());
		}
	}

	private static void createSavestateDirectory() {
		if (!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}

	/**
	 * Makes replaces the current recording with the recording from the savestate.
	 * Gets triggered when a savestate is loaded on the server<br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate
	 * @throws IOException
	 */
	public static void loadstate(String nameOfSavestate) throws IOException {

		if (nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}

		createSavestateDirectory();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".tas");

		if (!targetfile.exists()) {
			ClientProxy.virtual.getContainer().clear();
		} else {
			ClientProxy.virtual.loadSavestate(ClientProxy.serialiser.fromEntireFileV1(targetfile));
		}
	}
}
