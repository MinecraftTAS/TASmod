package de.scribble.lp.tasmod.savestates.client;

import java.io.File;
import java.io.IOException;

import com.mojang.realmsclient.gui.ChatFormatting;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creating savestates of the inputs on the client<br>
 * Side: Client
 * 
 * @author ScribbleLP
 */
@SideOnly(Side.CLIENT)
public class InputSavestatesHandler {

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

		savestateDirectory.mkdir();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".tas");

		InputContainer container = ClientProxy.virtual.getContainer();
		if (container.isRecording()) {
			ClientProxy.serialiser.saveToFileV1(targetfile, container);	//If the container is recording, store it entirely
		} else if(container.isPlayingback()){
			ClientProxy.serialiser.saveToFileV1Until(targetfile, container, container.index()); //If the container is playing, store it until the current index
		}
	}

	/**
	 * Replaces the current recording with the recording from the savestate.
	 * Gets triggered when a savestate is loaded on the server<br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws IOException
	 */
	public static void loadstate(String nameOfSavestate) throws IOException {

		if (nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}

		savestateDirectory.mkdir();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".tas");

		InputContainer container = ClientProxy.virtual.getContainer();
		if (!container.isNothingPlaying()) { // If the file exists and the container is recording or playing, load the clientSavestate
			if (targetfile.exists()) {
				ClientProxy.virtual.loadClientSavestate(ClientProxy.serialiser.fromEntireFileV1(targetfile));
			} else {
				ClientProxy.virtual.getContainer().setTASState(TASstate.NONE, false);
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(ChatFormatting.YELLOW
						+ "Inputs could not be loaded for this savestate, since the file doesn't exist. Stopping!"));
				TASmod.logger.warn("Inputs could not be loaded for this savestate, since the file doesn't exist.");
			}
		}
	}
}
