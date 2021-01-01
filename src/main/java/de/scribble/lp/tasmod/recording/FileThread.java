package de.scribble.lp.tasmod.recording;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileThread extends Thread {
	
	private PrintWriter stream;
	private boolean end = false;
	
	private List<String> output = new ArrayList<String>();
	
	public FileThread(File fileLocation) throws FileNotFoundException {
		stream = new PrintWriter(new FileOutputStream(fileLocation));
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
	
}
