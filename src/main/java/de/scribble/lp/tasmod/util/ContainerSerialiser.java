package de.scribble.lp.tasmod.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.dselent.bigarraylist.BigArrayList;

import de.scribble.lp.tasmod.commands.savetas.FileThread;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.inputcontainer.TickInputContainer;
import de.scribble.lp.tasmod.monitoring.DesyncMonitoring;
import de.scribble.lp.tasmod.virtual.VirtualKey;
import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualMouse.PathNode;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;

/**
 * Saves a given {@linkplain InputContainer} to a file. Is also able to read an input container from a file. <br>
 * <br>
 * I plan to be backwards compatible so all the save functions have a V1 in their name by the time of writing this<br>
 * <br>
 * It also serializes the {@linkplain DesyncMonitoring} from the input container<br>
 * <br>
 * Side: Client
 * 
 * @author ScribbleLP
 *
 */
public class ContainerSerialiser {
	
	/**
	 * Saves all inputs of the input container
	 * @param file Where to save the container
	 * @param container The container to save
	 * @throws IOException When the input container is empty
	 */
	public void saveToFileV1(File file, InputContainer container) throws IOException {
		saveToFileV1Until(file, container, -1);
	}
	
	/**
	 * Saves inputs up to a certain index of the input container
	 * @param file Where to save the container
	 * @param container The container to save
	 * @param index index until the inputs get saved
	 * @throws IOException When the input container is empty
	 */
	public void saveToFileV1Until(File file, InputContainer container, int index) throws IOException{
		if (container.size() == 0) {
			throw new IOException("There are no inputs to save to a file");
		}
		FileThread fileThread = new FileThread(file, false);
		FileThread monitorThread= new FileThread(new File(file, "../"+file.getName().replace(".tas", "")+".mon"), false);

		fileThread.start();
		monitorThread.start();
		
		fileThread.addLine("################################################# TASFile ###################################################\n"
				 + "#												Version:1													#\n"
				 + "#							This file was generated using the Minecraft TASMod								#\n"
				 + "#																											#\n"
				 + "#			Any errors while reading this file will be printed out in the console and the chat				#\n"
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
				 + "#StartPosition:"+container.getStartLocation()+"\n"
				 + "#############################################################################################################\n");
		
		BigArrayList<TickInputContainer> ticks = container.getInputs();
		for (int i = 0; i < ticks.size(); i++) {
			if(i==index) {
				break;
			}
			TickInputContainer tick = ticks.get(i);
			fileThread.addLine(tick.toString() + "\n");
			monitorThread.addLine(container.dMonitor.get(i) + "\n");
		}
		fileThread.close();
		monitorThread.close();
	}

	public int getFileVersion(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		for (String line : lines) {
			if (line.contains("Version")) {
				String trimmed = line.replaceAll("#|\t", "");
				int tick=0;
				try {
					tick=Integer.parseInt(trimmed.split(":")[1]);
				} catch (NumberFormatException e) {
					throw new IOException("Can't read the file version: "+trimmed);
				}
				return tick;
			}
		}
		return 0;
	}

	public InputContainer fromEntireFileV1(File file) throws IOException {

		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		
		File monitorFile=new File(file, "../"+file.getName().replace(".tas", "")+".mon");
		
		List<String> monitorLines=null;
		
		if(monitorFile.exists()) {
			monitorLines = FileUtils.readLines(monitorFile, Charset.defaultCharset());
		}

		InputContainer container = new InputContainer();

		String author = "Insert author here"; //TODO Make this print out in chat

		String title = "Insert TAS category here";

		String playtime = "00:00.0";

		int rerecords = 0;
		
		String startLocation="";

		container.clear();

		int linenumber = 0;
		for (String line : lines) {
			linenumber++;
			//Read out the data
			if (line.startsWith("#")) {
				if (line.startsWith("#Author:")) {
					author = line.split(":")[1];
				} else if (line.startsWith("#Title:")) {
					title = line.split(":")[1];
				} else if (line.startsWith("#Playing Time:")) {
					playtime = line.split(":")[1];
				} else if (line.startsWith("#Rerecords:")) {
					rerecords = Integer.parseInt(line.split(":")[1]);
				} else if (line.startsWith("#StartPosition:")) {
					startLocation = line.replace("#StartPosition:", "");
				}
			} else {
				String[] sections = line.split("\\|");

				if (sections.length != 4) {
					throw new IOException("Error in line " + linenumber + ". Cannot read the line correctly");
				}

				container.getInputs().add(new TickInputContainer(readTicks(sections[0], linenumber), readKeyboard(sections[1], linenumber), readMouse(sections[2], linenumber), readSubtick(sections[3], linenumber)));
			}
		}
		container.setAuthors(author);
		container.setTitle(title);
		container.setPlaytime(playtime);
		container.setRerecords(rerecords);
		container.setStartLocation(startLocation);
		if(monitorLines!=null) {
			container.dMonitor.setPos(monitorLines);
		}
		
		return container;
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
		VirtualKeyboard keyboard = new VirtualKeyboard();

		// Remove the prefix
		section = section.replace("Keyboard:", "");

		// Split in keys and characters
		String[] keys = section.split(";");

		// If there is nothing, return the empty keyboard
		if (keys.length == 0) {
			return keyboard;
		}

		// Check if the keylist is empty
		if (!keys[0].isEmpty()) {

			// Split multiple keys
			String[] splitKeys = keys[0].split(",");

			for (String key : splitKeys) {

				VirtualKey vkey = null;
				// Check if the key is a keycode
				if (isNumeric(key)) {
					vkey = keyboard.get(Integer.parseInt(key));
				} else {
					vkey = keyboard.get(key);
				}

				if (vkey == null) {
					throw new IOException(key + " is not a recognised keyboard key in line " + linenumber);
				}

				vkey.setPressed(true);
			}
		}
		
		char[] chars = {};
		//Check if the characterlist is empty
		if (keys.length == 2) {
			chars = keys[1].replace("\\n", "\n").toCharArray(); //Replacing the "\n" in lines to the character \n
		}
		
		for (char onechar : chars) {
			keyboard.addChar(onechar);
		}
		return keyboard;
	}

	private VirtualMouse readMouse(String section, int linenumber) throws IOException {
		VirtualMouse mouse = new VirtualMouse();
		
		// Remove the prefix
		section = section.replace("Mouse:", "");
		
		//Split into buttons and paths...
		String buttons = section.split(";")[0];
		String path = section.split(";")[1];
		
		//Check whether the button is empty
		if(!buttons.isEmpty()) {
			
			//Splitting multiple buttons
			String[] splitButtons=buttons.split(",");
			for (String button : splitButtons) {
				
				VirtualKey vkey = null;
				// Check if the key is a keycode
				if (isNumeric(button)) {
					vkey = mouse.get(Integer.parseInt(button));
				} else {
					vkey = mouse.get(button);
				}
				if (vkey == null) {
					throw new IOException(button + " is not a recognised mouse key in line " + linenumber);
				}
				mouse.get(button).setPressed(true);
			}
		}
		mouse.setPath(readPath(path, linenumber, mouse));

		return mouse;
	}

	private List<PathNode> readPath(String section, int linenumber, VirtualMouse mouse) throws IOException {
		List<PathNode> path = new ArrayList<VirtualMouse.PathNode>();
		
		section = section.replace("[", "").replace("]", "");
		String[] pathNodes = section.split("->");

		for (String pathNode : pathNodes) {
			String[] split = pathNode.split(",");
			
			int length=split.length;
			int scrollWheel = 0;
			int cursorX = 0;
			int cursorY = 0;
			try {
				scrollWheel = Integer.parseInt(split[length-3]);
				cursorX = Integer.parseInt(split[length-2]);
				cursorY = Integer.parseInt(split[length-1]);
			} catch (NumberFormatException e) {
				throw new IOException("'" + pathNode + "' couldn't be read in line " + linenumber+": Something is not a number");
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new IOException("'" + pathNode + "' couldn't be read in line " + linenumber+": Something is missing or is too much");
			}
			PathNode node = mouse.new PathNode();
			for (int i=0; i<length-3; i++) {
				String key= split[i];
				node.get(key).setPressed(true);
			}
			node.scrollwheel = scrollWheel;
			node.cursorX = cursorX;
			node.cursorY = cursorY;
			path.add(node);
		}
		return path;
	}
	
	private VirtualSubticks readSubtick(String section, int linenumber) throws IOException {
		section = section.replace("Camera:", "");
		String[] split=section.split(";");
		
		float x=0F;
		float y=0F;
		
		try {
			x=Float.parseFloat(split[0]);
			y=Float.parseFloat(split[1]);
		} catch (NumberFormatException e){
			throw new IOException(split[0]+" or/and "+split[1]+" are not float numbers in line "+ linenumber);
		}
		
		return new VirtualSubticks(x, y);
	}

//	private String getStartLocation() {
//		Minecraft mc = Minecraft.getMinecraft();
//		String pos = mc.player.getPositionVector().toString();
//		pos = pos.replace("(", "");
//		pos = pos.replace(")", "");
//		pos = pos.replace(" ", "");
//		String pitch = Float.toString(mc.player.rotationPitch);
//		String yaw = Float.toString(mc.player.rotationYaw);
//		return pos + "," + yaw + "," + pitch;
//	}
	
	private boolean isNumeric(String in){
		try {
			Integer.parseInt(in);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}
