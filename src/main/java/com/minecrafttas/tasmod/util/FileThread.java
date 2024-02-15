package com.minecrafttas.tasmod.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread for writing files to disc
 *
 * @author Pancake
 */
public class FileThread extends Thread {
	
	private final PrintWriter stream;
	private boolean end = false;
	
	private final List<String> output = new ArrayList<>();
	
	public FileThread(File fileLocation, boolean append) throws FileNotFoundException {
		stream = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileLocation, append), StandardCharsets.UTF_8));
	}
	
	public void addLine(String line) {
		synchronized (output) {
			output.add(line);
		}
	}
	
	@Override
	public void run() {
		while (!end) {
			synchronized (output) {
				ArrayList<String> newList = new ArrayList<String>(output);
				output.clear();
				for (String line : newList) {
					stream.print(line);
				}
			}
		}
		stream.flush();
		stream.close();
	}
	
	public void close() {
		end = true;
	}
	public void flush() {
		stream.flush();
	}
}
