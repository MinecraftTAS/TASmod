package com.minecrafttas.tasmod.util;

import java.nio.file.Path;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.tasfile.PlaybackSerialiser;

/**
 * Prints the current {@link PlaybackControllerClient#inputs} content to {@link TASmodClient#tasfiledirectory}/debug.mctas
 * 
 * @author Scribble
 */
public class DebugWriter {

	private static Path debugTASFile = TASmodClient.getTasFileDirectory().resolve("debug.mctas");

	public static void writeDebugFile(PlaybackControllerClient controller) {
		PlaybackSerialiser.saveToFile(debugTASFile, controller, null);
	}
}

