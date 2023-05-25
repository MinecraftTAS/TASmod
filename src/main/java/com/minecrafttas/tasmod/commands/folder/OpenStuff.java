package com.minecrafttas.tasmod.commands.folder;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

public class OpenStuff {

	public static void openTASFolder() {
		File file = new File(TASmodClient.tasdirectory);
		try {
			if (!file.exists())
				file.mkdir();
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			TASmod.logger.error("Something went wrong while opening ", file.getPath());
			e.printStackTrace();
		}
	}

	public static void openSavestates() {
		File file = new File(TASmodClient.savestatedirectory);
		try {
			if (!file.exists())
				file.mkdir();
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			TASmod.logger.error("Something went wrong while opening ", file.getPath());
			e.printStackTrace();
		}
	}
}
