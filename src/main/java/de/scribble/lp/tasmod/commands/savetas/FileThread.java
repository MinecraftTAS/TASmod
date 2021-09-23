package de.scribble.lp.tasmod.commands.savetas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileThread extends Thread {
	
	private PrintWriter stream;
	private boolean end = false;
	
	private List<String> output = new ArrayList<String>();
	
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
