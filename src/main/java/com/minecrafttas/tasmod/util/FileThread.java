package com.minecrafttas.tasmod.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread for writing files to disc
 *
 * @author Pancake
 */
public class FileThread extends Thread {

	private final PrintWriter stream;
	private boolean end = false;

	private final ConcurrentLinkedQueue<String> output = new ConcurrentLinkedQueue<>();

	public FileThread(Path fileLocation, boolean append) throws IOException {
		super("TASmod FileWriter Thread");
		OutputStream outStream = Files.newOutputStream(fileLocation, StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);
		stream = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
	}

	public void addLine(String line) {
		output.add(line + "\n");
	}

	@Override
	public void run() {
		while (!end) {
			writeOutput();
		}
		writeOutput();		// Print any remaining lines, just to be safe...

		stream.flush();
		stream.close();
	}

	private void writeOutput() {
		String line;
		while ((line = output.poll()) != null)
			stream.print(line);
	}

	public void close() {
		end = true;
	}

	public void flush() {
		stream.flush();
	}
}

