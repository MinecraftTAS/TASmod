package de.scribble.lp.tasmod.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.dselent.bigarraylist.BigArrayList;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.ItemRandom;
import de.scribble.lp.tasmod.recording.FileThread;
import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualMouse.PathNode;
import de.scribble.lp.tasmod.virtual.container.InputContainer;
import de.scribble.lp.tasmod.virtual.container.TickInputContainer;
import net.minecraft.client.Minecraft;

public class ContainerSerialiser {
	
	public void saveToFileV1(File file, InputContainer container) throws FileNotFoundException {
		if (container.size() == 0) {
			return;
		}
		FileThread fileThread = new FileThread(file, false);

		fileThread.start();
		
		fileThread.addLine("################################################# TASFile ###################################################\n"
				 + "#												Version:1													#\n"
				 + "#							This file was generated using the Minecraft TASMod								#\n"
				 + "#																											#\n"
				 + "#	If you make a mistake in this file, the mod will notify you via the console, so it's best to keep the	#\n"
				 + "#										console open at all times											#\n"
				 + "#																											#\n"
				 + "#------------------------------------------------ Header ---------------------------------------------------#\n"
				 + "#Author:" + container.getAuthors() + "\n"
				 + "#																											#\n"
				 + "#Title:" + container.getTitle() + "\n"
				 + "#																											#\n"
				 + "#Playing Time:" + container.getPlaytime() + "\n"
				 + "#																											#\n"
				 + "#Rerecords:"+container.getRerecords() + "\n"
				 + "#																											#\n"
				 + "#----------------------------------------------- Settings --------------------------------------------------#\n"
				 + "#Entity Seed:" + EntityRandom.currentSeed.get() + "\n"
				 + "#Item Seed:" + ItemRandom.currentSeed.get() + "\n"
				 + "#StartPosition:\n"
				 + "#############################################################################################################\n");
		
		BigArrayList<TickInputContainer> ticks = container.getInputs();
		for (int i = 0; i < ticks.size(); i++) {
			TickInputContainer tick = ticks.get(i);
			fileThread.addLine(tick.toString() + "\n");
		}
		fileThread.close();
	}

	public int getFileVersion(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		for (String line : lines) {
			if (line.contains("Version")) {
				String trimmed = line.replaceAll("#| ", "");
				return Integer.parseInt(trimmed.split(":")[1]);
			}
		}
		return 0;
	}

	public InputContainer fromEntireFileV1(File file) throws IOException {

		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

		InputContainer container = new InputContainer();

		String author = "Insert author here";

		String title = "Insert TAS category here";

		String playtime = "00:00.0";

		int rerecords = 0;

		container.clear();

		int linenumber = 0;
		for (String line : lines) {
			linenumber++;
			if (line.startsWith("#")) {
				if (line.startsWith("#Author:")) {
					author = line.split(":")[1];
				} else if (line.startsWith("#Title:")) {
					title = line.split(":")[1];
				} else if (line.startsWith("#Playing Time:")) {
					playtime = line.split(":")[1];
				} else if (line.startsWith("#Rerecords:")) {
					rerecords = Integer.parseInt(line.split(":")[1]);
				}
			} else {
				String[] sections = line.split("|");

				if (sections.length != 4) {
					throw new IOException("Error in line " + linenumber + ". Cannot read the line correctly");
				}

//				container.getInputs().add(element)
			}
		}
		container.setAuthors(author);
		container.setTitle(title);
		container.setPlaytime(playtime);
		container.setRerecords(rerecords);
		return null;
	}

	private int readTicks(String section, int linenumber) throws IOException {
		int ticks = 0;
		try {
			ticks = Integer.parseInt(section);
		} catch (NumberFormatException e) {
			throw new IOException(section + " is not a recognised number in line " + linenumber);
		}
		return ticks;
	}
	
	private VirtualKeyboard readKeyboard(String section, int linenumber) throws IOException {
		section=section.replace("Keyboard:", "");
		String[] keys = section.split(";")[0].split(",");
		char[] chars = section.split(";")[1].replace("\\n", "\n").toCharArray();

		VirtualKeyboard keyboard = new VirtualKeyboard();
		for (String key : keys) {
			if (keyboard.get(key) == null) {
				throw new IOException(key + " is not a recognised keyboard key in line " + linenumber);
			}
			keyboard.get(key).setPressed(true);
		}
		for (char onechar : chars) {
			keyboard.addChar(onechar);
		}
		return keyboard;
	}

	private VirtualMouse readMouse(String section, int linenumber) throws IOException {
		section = section.replace("Mouse:", "");
		String[] buttons = section.split(";")[0].split(",");
		String path=section.split(";")[1];

		VirtualMouse mouse = new VirtualMouse();
		for (String button : buttons) {
			if (mouse.get(button) == null) {
				throw new IOException(button + " is not a recognised mouse key in line " + linenumber);
			}
			mouse.get(button).setPressed(true);
		}

		mouse.setPath(readPath(path, linenumber, mouse));
		
		return mouse;
	}

	private List<PathNode> readPath(String section, int linenumber, VirtualMouse mouse) throws IOException {
		section = section.replace("[", "").replace("]", "");
		String[] pathNodes = section.split("->");

		List<PathNode> path = new ArrayList<VirtualMouse.PathNode>();

		for (String pathNode : pathNodes) {
			String[] split = pathNode.split(";");
			String key = split[0];
			int scrollWheel = 0;
			int cursorX = 0;
			int cursorY = 0;
			try {
				scrollWheel = Integer.parseInt(split[1]);
				cursorX = Integer.parseInt(split[2]);
				cursorY = Integer.parseInt(split[3]);
			} catch (NumberFormatException e) {
				throw new IOException("'" + pathNode + "' couldn't be read in line " + linenumber);
			}
			PathNode node = mouse.new PathNode();
			node.get(key).setPressed(true);
			node.scrollwheel = scrollWheel;
			node.cursorX = cursorX;
			node.cursorY = cursorY;
			path.add(node);
		}
		return path;
	}

	private static String getStartLocation() {
		Minecraft mc = Minecraft.getMinecraft();
		String pos = mc.player.getPositionVector().toString();
		pos = pos.replace("(", "");
		pos = pos.replace(")", "");
		pos = pos.replace(" ", "");
		String pitch = Float.toString(mc.player.rotationPitch);
		String yaw = Float.toString(mc.player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}
}