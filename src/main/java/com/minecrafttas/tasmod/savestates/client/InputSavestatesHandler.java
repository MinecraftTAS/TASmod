package com.minecrafttas.tasmod.savestates.client;

import java.io.File;
import java.io.IOException;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackController;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.savestates.server.exceptions.SavestateException;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Creating savestates of the inputs on the client<br>
 * Side: Client
 * 
 * @author ScribbleLP
 */
@Environment(EnvType.CLIENT)
public class InputSavestatesHandler {

	private final static File savestateDirectory = new File(TASmodClient.tasdirectory + File.separator + "savestates");

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
		TASmod.logger.debug(LoggerMarkers.Savestate, "Saving client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			TASmod.logger.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}

		savestateDirectory.mkdir();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".mctas");

		PlaybackController container = TASmodClient.virtual.getContainer();
		if (container.isRecording()) {
			TASmodClient.serialiser.saveToFileV1(targetfile, container);	//If the container is recording, store it entirely
		} else if(container.isPlayingback()){
			TASmodClient.serialiser.saveToFileV1Until(targetfile, container, container.index()); //If the container is playing, store it until the current index
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
		TASmod.logger.debug(LoggerMarkers.Savestate, "Loading client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			TASmod.logger.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}

		savestateDirectory.mkdir();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".mctas");

		PlaybackController container = TASmodClient.virtual.getContainer();
		if (!container.isNothingPlaying()) { // If the file exists and the container is recording or playing, load the clientSavestate
			if (targetfile.exists()) {
				TASmodClient.virtual.loadClientSavestate(TASmodClient.serialiser.fromEntireFileV1(targetfile));
			} else {
				TASmodClient.virtual.getContainer().setTASState(TASstate.NONE, false);
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(ChatFormatting.YELLOW
						+ "Inputs could not be loaded for this savestate, since the file doesn't exist. Stopping!"));
				TASmod.logger.warn(LoggerMarkers.Savestate, "Inputs could not be loaded for this savestate, since the file doesn't exist.");
			}
		}
	}
}
