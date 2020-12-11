package de.scribble.lp.tasmod.recording;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

public class FileWriterThread implements Runnable{
	StringBuilder output;
	File file;
	Logger logger;
	public FileWriterThread(StringBuilder output, File fileLocation, Logger logger) {
		this.output=output;
		this.file=fileLocation;
		this.logger=logger;
	}
	@Override
	public void run() {
		try {
			Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			logger.error("Something went wrong while trying to save the recording!");
			e.printStackTrace();
		}
	}
}
