package de.scribble.lp.tasmod.folder;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;

public class OpenStuff {
	
	public static void openTASFolder() {
		File file=new File(ClientProxy.tasdirectory);
		try {
			if(!file.exists())file.mkdir();
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			CommonProxy.logger.fatal("Something went wrong while opening ", file.getPath());
			e.printStackTrace();
		}
	}
	
	public static void openSavestates() {
		File file=new File(ClientProxy.savestatedirectory);
		try {
			if(!file.exists())file.mkdir();
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			CommonProxy.logger.fatal("Something went wrong while opening ", file.getPath());
			e.printStackTrace();
		}
	}
}
