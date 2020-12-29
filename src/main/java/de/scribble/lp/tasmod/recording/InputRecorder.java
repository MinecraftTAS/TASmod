package de.scribble.lp.tasmod.recording;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouseEvent;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;
import net.minecraft.client.Minecraft;

/**
 * Takes keys from the VirtualMouseAndKeyboard and adds them to a file
 * 
 * @author ScribbleLP
 *
 */
public class InputRecorder {
	private static Minecraft mc=Minecraft.getMinecraft();
	private static StringBuilder output;
	private static Logger logger= LogManager.getLogger("InputRecorder");
	private static File fileLocation;
	private static boolean recording=false;
	private static InputRecorder instance;
	private static int mouseCoordX;
	private static int mouseCoordY;
	private static long tickCounter;
	private static boolean iskeydown;
	private static final String tasdirectory=mc.mcDataDir.getAbsolutePath()+ File.separator +"saves" + File.separator + "tasfiles";

	/**
	 * Start the recording and generate a filename consisting of recording+System.currentTimeMillis
	 */
	public static void startRecording() {
		startRecording("recording_"+System.currentTimeMillis());
	}
	/**
	 * Start a recording
	 * @param filename where this is getting saved
	 */
	public static void startRecording(String filename) {
		if(!recording) {
			makeTASDir();
			File files=interpretFilename(filename);
			if(files==null) {
				logger.error("The filename is not applicable");
				return;
			}
			fileLocation=files;
			recording=true;
			output=new StringBuilder();
			addHeader(output);
			tickCounter=0;
//			Minecraft.getMinecraft().randommanager.setEntityRandomnessAll(0);
//			RandomLogger.startRandomLogging();
//			new SavestateHandlerClient().saveState();
			TutorialHandler tutorial= ClientProxy.getPlaybackTutorial();
			if(tutorial.istutorial&&tutorial.getState()==3) {
				tutorial.advanceState();
			}
		}else {
			logger.error("There is already a recording running!");
		}
	}
	private static void addHeader(StringBuilder output2) {
		Minecraft mc=Minecraft.getMinecraft();
		output.append	("################################################# TASFile ###################################################\n"
						+"#							This file was generated using the Minecraft TASMod								#\n"
						+"#																											#\n"
						+"#	If you make a mistake in this file, the mod will notify you via the console, so it's best to keep the	#\n"
						+"#										console open at all times											#\n"
						+"#																											#\n"
						+"#------------------------------------------------ Header ---------------------------------------------------#\n"
						+"#Author:"+ mc.player.getName()+"\n"
						+"#																											#\n"
						+"#StartLocation:" + getStartLocation() +",\n"
						+"#																											#\n"
						+"#Resolution:"+ mc.displayWidth+"x"+mc.displayHeight+"\n"
						+"#																											#\n"
						+"#############################################################################################################\n");
	}
	private static String getStartLocation() {
		String pos=mc.player.getPositionVector().toString();
		pos=pos.replace("(", "");
		pos=pos.replace(")", "");
		pos=pos.replace(" ", "");
		String pitch=Float.toString(mc.player.rotationPitch);
		String yaw=Float.toString(mc.player.rotationYaw);
		return pos+","+yaw+","+pitch;
	}
	private static int getSlot() {
		return 1;
	}
	/**
	 * Takes the current keyboard and mouse events and stores them in 1 line in the output
	 */
	public static void recordTick() {
		if(recording) {
			/*Key for stopping the recording*/
			if(Keyboard.isKeyDown(Keyboard.KEY_N)) {
				stopRecording();
			}
			if(!Display.isActive()) {
				stopRecording();
			}
			tickCounter++; //Tickcounter used as a time reference, not actually used for playback
			
			/*=====Keyboard inputs=====*/
			String keyboardString="";
			String keyboardStateString="";
			String charString="";
			List<VirtualKeyboardEvent> keyboardEventList= VirtualMouseAndKeyboard.getKeyboardEvents(); //Get all the inputs currently pressed on the virtual keyboard
			
			for (int i = 0; i < keyboardEventList.size(); i++) { 	//If there are multiple keyboard events in one tick, this will add them all, seperated with comma
				String ending=",";
				if(i==keyboardEventList.size()-1) {		//Prevents a comma at the end of the input list
					ending="";
				}
				VirtualKeyboardEvent event=keyboardEventList.get(i);
				if(event.getKeyCode()==67) {		//Removing F9 from the recorded inputs
					continue;
				}
				keyboardString=keyboardString.concat(VirtualMouseAndKeyboard.getNameFromKeyCode(event.getKeyCode())+ending);	//Add everything into a string
				keyboardStateString=keyboardStateString.concat(Boolean.toString(event.isState())+ending);
				charString=charString.concat(Character.toString(event.getCharacter()));
			}
			
			/*=====MouseButton Inputs=====*/
			String mouseString="";
			String mouseStateString = "";
			String scrollString="";
			String mouseXString="";
			String mouseYString="";
			String slotString="";
			List<VirtualMouseEvent> mouseEventList = VirtualMouseAndKeyboard.getMouseEvents();
			
			for (int i = 0; i < mouseEventList.size(); i++) {
				String ending=",";
				if(i==mouseEventList.size()-1) {
					ending="";
				}
				VirtualMouseEvent event=mouseEventList.get(i);
				mouseString=mouseString.concat(VirtualMouseAndKeyboard.getNameFromKeyCode(event.getKeyCode())+ending);
				mouseStateString=mouseStateString.concat(Boolean.toString(event.isState())+ending);
				if(event.getScrollwheel()==0) {
					scrollString=scrollString.concat(" "+ending);
				}else {
					scrollString=scrollString.concat(event.getScrollwheel()+ending);
				}
				double normalizedX=PointerNormalizer.getNormalizedX(event.getMouseX());
				double normalizedY=PointerNormalizer.getNormalizedY(event.getMouseY());
				mouseXString=mouseXString.concat(normalizedX+ending);
				mouseYString=mouseYString.concat(normalizedY+ending);
				slotString=slotString.concat(event.getSlotidx()+ending);
			}
			
			/*=====Special rules=====*/
			mouseString=mouseString.replace("MOUSEMOVED", " ");			//The standard event for moving the mouse is set to blank here. Since the mouse is moved VERY often, this clutters the whole file
			charString=StringUtils.replace(charString, "\r", "\\n");	//Replacing \r with \\n for the chars to stop gaps from happening in the files
			charString=StringUtils.replace(charString, "\n", "\\n");
			mouseStateString=mouseStateString.replace("false", " ");	//Unpressing buttons makes a false appear. This is replaced by a blank to increase visibility on the 'true' strings
			keyboardStateString=keyboardStateString.replace("false", " ");
			
			/*=====Add to a line=====*/
			/*If the section is empty, tooltips like 'Keyboard:' will be skipped for more visibility*/
			output.append(Long.toString(tickCounter)+"|"		//Ticks
					
			+(keyboardString.isEmpty()?"":"Keyboard:")+keyboardString+"|"		//Keyboard
			
			+(keyboardStateString.isEmpty()?"":"KeyboardState:")+keyboardStateString+"|"		//KeyboardState
					
			+(mc.currentScreen!=null?mc.currentScreen.toString().replace("net.minecraft.client.gui.", ""):"null")+"|"		//Current Screen (Unused)
			
			+(charString.isEmpty()?"":"CharacterTyped:")+charString+"|"			//CharTyped
			
			+(mouseString.isEmpty()?"":"Mouse:")+mouseString+"|"		//Mouse
					
			+(mouseStateString.isEmpty()?"":"MouseState:")+mouseStateString+"|"		//MouseState
			
			+(scrollString.isEmpty()?"":"ScrollWheel:")+scrollString+"|"		//ScrollWheel
			
			+(mouseXString.isEmpty()?"":"MouseX/Y:")+mouseXString+";"+mouseYString+"|"		//MouseCoords
					
			+(slotString.isEmpty()?"":"SlotID:")+slotString+"\n");		//SlotID
		}
	}
	/**
	 * Records the mouse movement aka. the mouse deltas
	 */
	public static void recordSubTick() {
		if(recording) {
			VirtualSubticks subtick= VirtualMouseAndKeyboard.getSubtick();
			output.append("S"+VirtualMouseAndKeyboard.getTimeSinceLastTick()+"|"+subtick.getPitch()+"|"+subtick.getYaw()+"\n");
		}
	}
	/**
	 * Normally the yaw of the player is not contained within -180 +180 bounds and thus causes the camera to snap to the other side when switching from -180 to +180<br>
	 * This tries to prevent this
	 * @param Yaw
	 * @return
	 */
	private static Float recalcYaw(float Yaw) {
        while (Yaw >= 180) Yaw -= 360;
        while (Yaw < -180) Yaw += 360;
        return Yaw;
    }
	public File getSaveLocation() {
		return fileLocation;
	}
	/**
	 * Stops Input-Recording and everything involved with it
	 */
	public static void stopRecording() {
		if(recording) {
			recording=false;
			logger.info("Stopping the recording");
			Thread t = new Thread(new FileWriterThread(output, fileLocation, logger), "FileWriterThreadTicks");
			t.start();
			TutorialHandler tutorial= ClientProxy.getPlaybackTutorial();
			if(tutorial.istutorial&&tutorial.getState()==4) {
				tutorial.advanceState();
			}
//			Thread t2 = new Thread(new FileWriterThread(outputSubtick, fileLocationSubTick, logger), "FileWriterThreadSubtick");
//			t2.start();
//			RandomLogger.stopRandomLogging();
		}else {
			logger.error("There is no recording that can be aborted!");
		}
	}
	/**
	 * Takes a name and returns the corresponding file for that name
	 * @param name
	 * @return file
	 */
	private static File interpretFilename(String name){
		File file=new File(tasdirectory+File.separator+name+".tas");
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
        }else {
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
}
