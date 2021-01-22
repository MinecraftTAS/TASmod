package de.scribble.lp.tasmod.recording;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouseEvent;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Takes keys from the VirtualMouseAndKeyboard and adds them to a file
 * 
 * @author ScribbleLP
 *
 */
public class InputRecorder {
	private static Minecraft mc = Minecraft.getMinecraft();
	// private static StringBuilder output;
	private static Logger logger = LogManager.getLogger("InputRecorder");
	private static File fileLocation;
	private static boolean recording = false;
	private static long tickCounter;
	private static final String tasdirectory = mc.mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";
	private static boolean pauseRecording = false;
	private static String filenames;
	private static File temp_names;

	private static FileThread fileThread;
	
	private static boolean rewind;
	
	/**
	 * Start the recording and generate a filename consisting of
	 * recording+System.currentTimeMillis
	 * @throws FileNotFoundException 
	 */
	public static void startRecording() throws FileNotFoundException {
		startRecording("recording_" + System.currentTimeMillis());
	}

	/**
	 * Start a recording
	 * 
	 * @param filename where this is getting saved
	 * @throws FileNotFoundException 
	 */
	public static void startRecording(String filename) throws FileNotFoundException {
		if (!recording) {
			makeTASDir();
			mc.player.sendMessage(new TextComponentString("Recording started"));
			File files = interpretFilename(filename);
			if (files == null) {
				logger.error("The filename is not applicable");
				return;
			}
			tickCounter=0;
			prepareRecording(filename, files);
			
			fileThread = new FileThread(fileLocation, false);
			fileThread.start();
			
			addHeader();
			
			TutorialHandler tutorial = ClientProxy.getPlaybackTutorial();
			if (TutorialHandler.istutorial && tutorial.getState() == 3) {
				tutorial.advanceState();
			}
		} else {
			logger.error("There is already a recording running!");
		}
	}
	
	public static void appendRecording(@Nullable String filename) throws FileNotFoundException {
		if(!recording) {
			if(filename==null) {
				filename=filenames;
			}
			File files = interpretFilename(filename);
			if (files == null) {
				logger.error("The filename is not applicable");
				return;
			}
			try {
				tickCounter=getLatestTickCounter(files);
			} catch (IOException e) {
				e.printStackTrace();
			}
			prepareRecording(filename, files);
			fileThread = new FileThread(fileLocation, true);
			fileThread.start();
		}
	}
	
	public static void startRecordingAt(@Nullable String filename, int startpoint) throws IOException {
		if(!recording) {
			if(filename==null) {
				filename=filenames;
			}
			File files= interpretFilename(filename);
			if (files == null) {
				logger.error("The filename is not applicable");
				return;
			}
			File files_temp= interpretFilename(filename+"_temp");
			if (files_temp == null) {
				logger.error("The filename is not applicable");
				return;
			}
			prepareRecording(filename, files);
			temp_names=files_temp;
			fileThread = new FileThread(files_temp, false);
			fileThread.start();
			
			tickCounter=fillUntil(startpoint, files);
		}
	}
	
	private static int fillUntil(int line, File read_from) throws IOException {
		// Create a bunch of variables
		BufferedReader buff = new BufferedReader(new FileReader(read_from));
		String wholeLine = "";
		int ticks = 0;
		int compare = 0;
		int linecounter = 0;
		while (ticks<line) {
			wholeLine = buff.readLine();
			linecounter++;
			while (wholeLine.startsWith("#")) {
				fileThread.addLine(wholeLine+"\n");
				wholeLine=buff.readLine();
			}
			String[] sections=wholeLine.split("\\|");
			ticks=Integer.parseInt(sections[0]);
			if(compare+1!=ticks) {
				buff.close();
				throw new IOException("Error while reading tickcounter in line "+linecounter+". The numbers are not consecutive");
			}
			fileThread.addLine(wholeLine+"\n");
			compare=ticks;
		}
		buff.close();
		return ticks;
	}
	
	private static int getLatestTickCounter(File siles) throws IOException {
		// Create a bunch of variables
		BufferedReader buff = new BufferedReader(new FileReader(fileLocation));
		String wholeLine = "";
		int ticks=0;
		int compare=0;
		int linecounter=0;
		// Read the lines until the line is null
		while ((wholeLine = buff.readLine()) != null) {
			linecounter++;
			if (wholeLine.startsWith("#")) {
				continue;
			}
			String[] sections=wholeLine.split("\\|");
			try {
				ticks=Integer.parseInt(sections[0]);
			}catch(NumberFormatException e) {
				buff.close();
				throw new IOException("Error while reading the tickcounter in line "+linecounter+". Not a number");
			}
			if(compare+1!=ticks) {
				buff.close();
				throw new IOException("Error while reading tickcounter in line "+linecounter+". The numbers are not consecutive");
			}
			compare=ticks;
		}
		buff.close();
		return ticks;
	}
	
	public static void prepareForRewind() {
		stopRecording(false);
		rewind=true;
	}
	
	private static void prepareRecording(String filename, File files) {
		filenames=filename;
		fileLocation = files;
		recording = true;
		pauseRecording = false;
	}
	
	private static void addHeader() {
		fileThread.addLine("################################################# TASFile ###################################################\n"
						 + "#							This file was generated using the Minecraft TASMod								#\n"
						 + "#																											#\n"
						 + "#	If you make a mistake in this file, the mod will notify you via the console, so it's best to keep the	#\n"
						 + "#										console open at all times											#\n"
						 + "#																											#\n"
						 + "#------------------------------------------------ Header ---------------------------------------------------#\n"
						 + "#Author:" + mc.player.getName() + "\n"
						 + "#																											#\n"
						 + "#StartLocation:" + getStartLocation() + ",\n"
						 + "#																											#\n"
						 + "#Resolution:" + mc.displayWidth + "x" + mc.displayHeight + "\n"
						 + "#																											#\n"
						 + "#############################################################################################################\n");
	}

	private static String getStartLocation() {
		String pos = mc.player.getPositionVector().toString();
		pos = pos.replace("(", "");
		pos = pos.replace(")", "");
		pos = pos.replace(" ", "");
		String pitch = Float.toString(mc.player.rotationPitch);
		String yaw = Float.toString(mc.player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}

	/**
	 * Takes the current keyboard and mouse events and stores them in 1 line in the
	 * output
	 */
	public static void recordTick() {
		if(recording) {
			if(!Display.isActive()) {
				stopRecording(true);
			}
			if(VirtualKeybindings.isKeyDown(ClientProxy.stopkey)) {
				stopRecording(true);
			}
			if (pauseRecording) {
				return;
			}
			
			tickCounter++; // Tickcounter used as a time reference, not actually used for playback

			/* =====Keyboard inputs===== */
			String keyboardString = "";
			String keyboardStateString = "";
			String charString = "";
			List<VirtualKeyboardEvent> keyboardEventList = VirtualMouseAndKeyboard.getKeyboardEvents(); // Get all the inputs currently pressed on the virtual keyboard

			for (int i = 0; i < keyboardEventList.size(); i++) { // If there are multiple keyboard events in one tick, this will add them all, seperated with comma
				String ending = ",";
				if (i == keyboardEventList.size() - 1) { // Prevents a comma at the end of the input list
					ending = "";
				}
				VirtualKeyboardEvent event = keyboardEventList.get(i);
				if (VirtualKeybindings.isKeyCodeBlockedDuringRecording(event.getKeyCode())) { // Removing TASmod keybindings from the recording
					continue;
				}
				keyboardString = keyboardString.concat(VirtualMouseAndKeyboard.getNameFromKeyCode(event.getKeyCode()) + ending); // Add everything into a string
				keyboardStateString = keyboardStateString.concat(Boolean.toString(event.isState()) + ending);
				charString = charString.concat(Character.toString(event.getCharacter()));
			}

			/* =====MouseButton Inputs===== */
			String mouseString = "";
			String mouseStateString = "";
			String scrollString = "";
			String mouseXString = "";
			String mouseYString = "";
			String slotString = "";
			List<VirtualMouseEvent> mouseEventList = VirtualMouseAndKeyboard.getMouseEvents();

			for (int i = 0; i < mouseEventList.size(); i++) {
				String ending = ",";
				if (i == mouseEventList.size() - 1) {
					ending = "";
				}
				VirtualMouseEvent event = mouseEventList.get(i);
				mouseString = mouseString.concat(VirtualMouseAndKeyboard.getNameFromKeyCode(event.getKeyCode()) + ending);
				mouseStateString = mouseStateString.concat(Boolean.toString(event.isState()) + ending);
				if (event.getScrollwheel() == 0) {
					scrollString = scrollString.concat(" " + ending);
				} else {
					scrollString = scrollString.concat(event.getScrollwheel() + ending);
				}
				double normalizedX = PointerNormalizer.getNormalizedX(event.getMouseX());
				double normalizedY = PointerNormalizer.getNormalizedY(event.getMouseY());
				mouseXString = mouseXString.concat(normalizedX + ending);
				mouseYString = mouseYString.concat(normalizedY + ending);
				slotString = slotString.concat(event.getSlotidx() + ending);
			}
			/* =====Subticks===== */
			// Subticks describe camera movement, like rotationPitch, rotationYaw.
			// This is normally dependant on the framerate hence the name subticks.
			// Before, this was seperated from the ticks

			VirtualSubticks subtick = VirtualMouseAndKeyboard.getSubtick();
			String pitch = Float.toString(subtick.getPitch());
			String yaw = Float.toString(subtick.getYaw());

			/* =====Special rules===== */
			mouseString = mouseString.replace("MOUSEMOVED", " "); // The standard event for moving the mouse is set to blank here. Since the mouse is moved VERY often, this clutters the whole file
			charString = StringUtils.replace(charString, "\r", "\\n"); // Replacing \r with \\n for the chars to stop gaps from happening in the files
			charString = StringUtils.replace(charString, "\n", "\\n");
			mouseStateString = mouseStateString.replace("false", " "); // Unpressing buttons makes a false appear. This is replaced by a blank to increase visibility on the 'true' strings
			keyboardStateString = keyboardStateString.replace("false", " ");

			/* =====Add to a line===== */
			/*
			 * If the section is empty, tooltips like 'Keyboard:' will be skipped for more
			 * visibility
			 */
			fileThread.addLine(Long.toString(tickCounter) + "|" // Ticks

					+ (keyboardString.isEmpty() ? "" : "Keyboard:") + keyboardString + "|" // Keyboard

					+ (keyboardStateString.isEmpty() ? "" : "KeyboardState:") + keyboardStateString + "|" // KeyboardState

					+ (mc.currentScreen != null ? mc.currentScreen.toString().replace("net.minecraft.client.gui.", "")
							: "null")
					+ "|" // Current Screen (Unused)

					+ (charString.isEmpty() ? "" : "CharacterTyped:") + charString + "|" // CharTyped

					+ (mouseString.isEmpty() ? "" : "Mouse:") + mouseString + "|" // Mouse

					+ (mouseStateString.isEmpty() ? "" : "MouseState:") + mouseStateString + "|" // MouseState

					+ (scrollString.isEmpty() ? "" : "ScrollWheel:") + scrollString + "|" // ScrollWheel

					+ (mouseXString.isEmpty() ? "" : "MouseX/Y:") + mouseXString + ";" + mouseYString + "|" // MouseCoords

					+ (pitch.isEmpty() ? "" : "Pitch:") + pitch + "|" // Pitch

					+ (yaw.isEmpty() ? "" : "Yaw:") + yaw + "|" // Yaw

					+ (slotString.isEmpty() ? "" : "SlotID:") + slotString + "\n"); // SlotID (Unused)
		}
	}

	public File getSaveLocation() {
		return fileLocation;
	}

	/**
	 * Stops Input-Recording and everything involved with it
	 * @param log 
	 */
	public static void stopRecording(boolean log) {
		if (recording) {
			recording = false;
			
			TutorialHandler tutorial = ClientProxy.getPlaybackTutorial();
			if (TutorialHandler.istutorial && tutorial.getState() == 4) {
				tutorial.advanceState();
			}
			if(log) {
				logger.info("Stopping the recording");
				mc.player.sendMessage(new TextComponentString("Recording stopped"));
			}
			fileThread.close();
			
			if(rewind==true) {
				try {
					FileUtils.moveFile(temp_names, fileLocation);
				} catch (IOException e) {
					logger.error("Can't rename :(");
					e.printStackTrace();
				}
			}
		} else {
			logger.error("There is no recording that can be aborted!");
		}
	}

	/**
	 * Takes a name and returns the corresponding file for that name
	 * 
	 * @param name
	 * @return file
	 */
	private static File interpretFilename(String name) {
		File file = new File(tasdirectory + File.separator + name + ".tas");
		if (name.contains("/") 
				|| name.contains(".") 
				|| name.contains("\r")
				|| name.contains("\t")
				|| name.contains("\0")
				|| name.contains("\f")
				|| name.contains("`")
				|| name.contains("?")
				|| name.contains("*")
				|| name.contains("\\")
				|| name.contains("<")
				|| name.contains(">")
				|| name.contains("|")
				|| name.contains("\"")
				|| name.contains(":")) {
			return null;
		} else {
			return file;
		}
	}

	/**
	 * Makes a directory under .minecraft/saves/tasfiles
	 */
	private static void makeTASDir() {
		new File(tasdirectory).mkdir();
	}

	public static boolean isRecording() {
		return recording;
	}

	public static boolean isPaused() {
		return pauseRecording;
	}

	public static void setPause(boolean pause) {
		pauseRecording = pause;
	}

	public static File getFileLocation() {
		return fileLocation;
	}

	public static String getFilename() {
		return filenames;
	}

	public static void saveFile() {
		fileThread.flush();
	}

	public static long getTickCounter() {
		return tickCounter;
	}

	public static boolean isRewind() {
		return rewind;
	}
}
