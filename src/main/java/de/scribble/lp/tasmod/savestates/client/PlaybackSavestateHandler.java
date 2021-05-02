package de.scribble.lp.tasmod.savestates.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;

public class PlaybackSavestateHandler {
	private static File savestateDirectory = new File(ClientProxy.tasdirectory + File.separator + "savestates");

	public static void savestatePlayback(String nameOfSavestate) throws SavestateException, IOException {
		if (!ClientProxy.virtual.getContainer().isPlayingback()) {
			CommonProxy.logger.info("No recording savestate made since no recording is running");
			return;
		}

		if (nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}

		createSavestateDirectory();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".test"); // TODO Do something else other than .test ._.

		FileUtils.writeLines(targetfile, ImmutableList.of(ClientProxy.virtual.getContainer().index()), false);
	}

	private static void createSavestateDirectory() {
		if (!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}

	public static void loadPlayback(String nameOfSavestate){

		if (!ClientProxy.virtual.getContainer().isPlayingback()) {
			CommonProxy.logger.info("No recording savestate made since no recording is running");
			return;
		}

		if (nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}

		createSavestateDirectory();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".test");
		
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(targetfile, Charset.defaultCharset()); //TODO Better error handling
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int index=Integer.parseInt(lines.get(0));

		ClientProxy.virtual.getContainer().setIndex(index);
	}
}
