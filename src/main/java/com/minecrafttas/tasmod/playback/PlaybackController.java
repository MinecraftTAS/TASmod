package com.minecrafttas.tasmod.playback;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lwjgl.opengl.Display;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.common.Configuration.ConfigOptions;
import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.monitoring.DesyncMonitoring;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;
import com.minecrafttas.tasmod.virtual.VirtualSubticks;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * A controller where the inputs are stored.<br>
 * <br>
 * Filling this controller is accomplished by setting the state to "recording"
 * via {@linkplain #setRecording(boolean)},<br>
 * or by loading inputs from file.<br>
 * <br>
 * These inputs can be played back at any time by setting
 * {@linkplain #setPlayback(boolean)} to true. <br>
 * <br>
 * Information about the author etc. get stored in the playback controller too and
 * will be printed out in chat when the player loads into a world <br>
 * Inputs are saved and loaded to/from file via the
 * {@linkplain PlaybackSerialiser}
 * 
 * @author Scribble
 *
 */
public class PlaybackController implements ServerPacketHandler, ClientPacketHandler{

	/**
	 * The current state of the controller.
	 */
	private TASstate state = TASstate.NONE;

	/**
	 * The state of the controller when the state is paused
	 */
	private TASstate tempPause = TASstate.NONE;
	/**
	 * The current index of the inputs
	 */
	private int index;
	
	private VirtualKeyboard keyboard = new VirtualKeyboard();

	private VirtualMouse mouse = new VirtualMouse();

	private VirtualSubticks subticks = new VirtualSubticks();

	public final File directory = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles");

	/**
	 * The place where all inputs get stored
	 */
	private BigArrayList<TickInputContainer> inputs = new BigArrayList<TickInputContainer>(directory + File.separator + "temp");

	/**
	 * A map of control bytes. Used to change settings during playback via the playback file.
	 * <p>
	 * A full list of changes can be found in {@link ControlByteHandler}
	 * <p>
	 * The values are as follows:<p>
	 * <code>Map(int playbackLine, List(Pair(String controlCommand, String[] arguments))</code>"
	 */
	private Map<Integer, List<Pair<String, String[]>>> controlBytes = new HashMap<Integer, List<Pair<String, String[]>>>();
	
	/**
	 * The comments in the file, used to store them again later
	 */
	private Map<Integer, List<String>> comments = new HashMap<>();
	
	public DesyncMonitoring desyncMonitor = new DesyncMonitoring(this);
	
	// =====================================================================================================

	private String title = "Insert TAS category here";
	
	private String authors = "Insert author here";

	private String playtime = "00:00.0";
	
	private int rerecords = 0;

	private String startLocation = "";
	
	private long startSeed = TASmod.ktrngHandler.getGlobalSeedClient();

	// =====================================================================================================

	private boolean creditsPrinted=false;

	private Integer playUntil = null;
	
	/**
	 * Starts or stops a recording/playback
	 * 
	 * @param stateIn stateIn The desired state of the container
	 * @return
	 */
	public String setTASState(TASstate stateIn) {
		return setTASState(stateIn, true);
	}

	/**
	 * Starts or stops a recording/playback
	 * 
	 * @param stateIn The desired state of the container
	 * @param verbose Whether the output should be printed in the chat
	 * @return The message printed in the chat
	 */
	public String setTASState(TASstate stateIn, boolean verbose) {
		ControlByteHandler.reset();
		if (state == stateIn) {
			switch (stateIn) {
			case PLAYBACK:
				return verbose ? TextFormatting.RED + "A playback is already running" : "";
			case RECORDING:
				return verbose ? TextFormatting.RED + "A recording is already running" : "";
			case PAUSED:
				return verbose ? TextFormatting.RED + "The game is already paused" : "";
			case NONE:
				return verbose ? TextFormatting.RED + "Nothing is running" : "";
			}

		} else if (state == TASstate.NONE) { // If the container is currently doing nothing
			switch (stateIn) {
			case PLAYBACK:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Starting playback");
				if (Minecraft.getMinecraft().player != null && !startLocation.isEmpty()) {
					try {
						tpPlayer(startLocation);
					} catch (NumberFormatException e) {
						state = TASstate.NONE;
						e.printStackTrace();
						return verbose ? TextFormatting.RED + "An error occured while reading the start location of the TAS. The file might be broken" : "";
					}
				}
				Minecraft.getMinecraft().gameSettings.chatLinks = false; // #119
				index = 0;
				state = TASstate.PLAYBACK;
				creditsPrinted=false;
				TASmod.ktrngHandler.setInitialSeed(startSeed);
				return verbose ? TextFormatting.GREEN + "Starting playback" : "";
			case RECORDING:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Starting recording");
				if (Minecraft.getMinecraft().player != null && startLocation.isEmpty()) {
					startLocation = getStartLocation(Minecraft.getMinecraft().player);
				}
				if(this.inputs.isEmpty()) {
					inputs.add(new TickInputContainer(index));
					desyncMonitor.recordNull(index);
				}
				state = TASstate.RECORDING;
				return verbose ? TextFormatting.GREEN + "Starting a recording" : "";
			case PAUSED:
				return verbose ? TextFormatting.RED + "Can't pause anything because nothing is running" : "";
			case NONE:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: None)";
			}
		} else if (state == TASstate.RECORDING) { // If the container is currently recording
			switch (stateIn) {
			case PLAYBACK:
				return verbose ? TextFormatting.RED + "A recording is currently running. Please stop the recording first before starting a playback" : "";
			case RECORDING:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Recording)";
			case PAUSED:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Pausing a recording");
				state = TASstate.PAUSED;
				tempPause = TASstate.RECORDING;
				return verbose ? TextFormatting.GREEN + "Pausing a recording" : "";
			case NONE:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Stopping a recording");
				TASmodClient.virtual.unpressEverything();
				state = TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Stopping the recording" : "";
			}
		} else if (state == TASstate.PLAYBACK) { // If the container is currently playing back
			switch (stateIn) {
			case PLAYBACK:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Playback)";
			case RECORDING:
				return verbose ? TextFormatting.RED + "A playback is currently running. Please stop the playback first before starting a recording" : "";
			case PAUSED:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Pausing a playback");
				state = TASstate.PAUSED;
				tempPause = TASstate.PLAYBACK;
				TASmodClient.virtual.unpressEverything();
				return verbose ? TextFormatting.GREEN + "Pausing a playback" : "";
			case NONE:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Stopping a playback");
				Minecraft.getMinecraft().gameSettings.chatLinks = true;
				TASmodClient.virtual.unpressEverything();
				state = TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Stopping the playback" : "";
			}
		} else if (state == TASstate.PAUSED) {
			switch (stateIn) {
			case PLAYBACK:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Resuming a playback");
				state=TASstate.PLAYBACK;
				tempPause=TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Resuming a playback" : "";
			case RECORDING:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Resuming a recording");
				state=TASstate.RECORDING;
				tempPause=TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Resuming a recording" : "";
			case PAUSED:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Paused)";
			case NONE:
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Aborting pausing");
				state=TASstate.NONE;
				TASstate statey=tempPause;
				tempPause=TASstate.NONE;
				return TextFormatting.GREEN + "Aborting a "+statey.toString().toLowerCase()+" that was paused";
			}
		}
		return "Something went wrong ._.";
	}

	/**
	 * Switches between the paused state and the state it was in before the pause
	 * @return The new state
	 */
	public TASstate togglePause() {
		if(state!=TASstate.PAUSED) {
			setTASState(TASstate.PAUSED);
		}else {
			setTASState(tempPause);
		}
		return state;
	}

	/**
	 * Forces the playback to pause or unpause
	 * @param pause True, if it should be paused
	 */
	public void pause(boolean pause) {
		TASmod.LOGGER.trace(LoggerMarkers.Playback, "Pausing {}", pause);
		if(pause) {
			if(state!=TASstate.NONE) {
				setTASState(TASstate.PAUSED, false);
			}
		}else {
			if(state == TASstate.PAUSED) {
				setTASState(tempPause, false);
			}
		}
	}
	
	public boolean isPlayingback() {
		return state == TASstate.PLAYBACK;
	}

	public boolean isRecording() {
		return state == TASstate.RECORDING;
	}
	
	public boolean isPaused() {
		return state == TASstate.PAUSED;
	}

	public boolean isNothingPlaying() {
		return state == TASstate.NONE;
	}

	/**
	 * @return The current state of the playback
	 */
	public TASstate getState() {
		return state;
	}

	// =====================================================================================================
	// Methods to update the temporary variables of the container.
	// These act as an input and output, depending if a recording or a playback is
	// running

	/**
	 * Adds or retrives a keyboard to the input container, depends on whether a
	 * recording or a playback is running
	 * 
	 * @param keyboard Keyboard to add
	 * @return Keyboard to retrieve
	 */
	public VirtualKeyboard addKeyboardToContainer(VirtualKeyboard keyboard) {
		if (state == TASstate.RECORDING) {
			this.keyboard = keyboard.clone();
		} else if (state == TASstate.PLAYBACK) {
			keyboard = this.keyboard.clone();
		}
		return keyboard;
	}

	/**
	 * Adds or retrives a mouse to the input container, depends on whether a
	 * recording or a playback is running
	 * 
	 * @param mouse Mouse to add
	 * @return Mouse to retrieve
	 */
	public VirtualMouse addMouseToContainer(VirtualMouse mouse) {
		if (state == TASstate.RECORDING) {
			this.mouse = mouse.clone();
		} else if (state == TASstate.PLAYBACK) {
			mouse = this.mouse.clone();
		}
		return mouse;
	}

	/**
	 * Adds or retrives the angle of the camera to the input container, depends on
	 * whether a recording or a playback is running
	 * 
	 * @param subticks Subticks to add
	 * @return Subticks to retrieve
	 */
	public VirtualSubticks addSubticksToContainer(VirtualSubticks subticks) {
		if (state == TASstate.RECORDING) {
			this.subticks = subticks.clone();
		} else if (state == TASstate.PLAYBACK) {
			subticks = this.subticks.clone();
		}
		return subticks;
	}

	/**
	 * Updates the input container.<br>
	 * <br>
	 * During a recording this adds the {@linkplain #keyboard}, {@linkplain #mouse}
	 * and {@linkplain #subticks} to {@linkplain #inputs} and increases the
	 * {@linkplain #index}.<br>
	 * <br>
	 * During playback the opposite is happening, getting the inputs from
	 * {@linkplain #inputs} and temporarily storing them in {@linkplain #keyboard},
	 * {@linkplain #mouse} and {@linkplain #subticks}.<br>
	 * <br>
	 * Then in {@linkplain VirtualInput}, {@linkplain #keyboard},
	 * {@linkplain #mouse} and {@linkplain #subticks} are retrieved and emulated as
	 * the next inputs
	 */
	public void nextTick() {
		/*Stop the playback while player is still loading*/
		EntityPlayerSP player=Minecraft.getMinecraft().player;
		
		if(player!=null && player.addedToChunk) {
			if(isPaused() && tempPause != TASstate.NONE) {
				TASstateClient.setOrSend(tempPause);	// The recording is paused in LoadWorldEvents#startLaunchServer
				pause(false);
				printCredits();
			}
		}
		
		/*Tick the next playback or recording*/
		if (state == TASstate.RECORDING) {
			recordNextTick();
		} else if (state == TASstate.PLAYBACK) {
			playbackNextTick();
		}
	}
	
	private void recordNextTick() {
		index++;
		if(inputs.size()<=index) {
			if(inputs.size()<index) {
				TASmod.LOGGER.warn("Index is {} inputs bigger than the container!", index-inputs.size());
			}
			inputs.add(new TickInputContainer(index, keyboard.clone(), mouse.clone(), subticks.clone()));
		} else {
			inputs.set(index, new TickInputContainer(index, keyboard.clone(), mouse.clone(), subticks.clone()));
		}
		desyncMonitor.recordMonitor(index); // Capturing monitor values
	}

	private void playbackNextTick() {
		
		if (!Display.isActive()) { // Stops the playback when you tab out of minecraft, for once as a failsafe, secondly as potential exploit protection
			TASmod.LOGGER.info(LoggerMarkers.Playback, "Stopping a {} since the user tabbed out of the game", state);
			setTASState(TASstate.NONE);
		}
		
		index++;	// Increase the index and load the next inputs
		
		/*Playuntil logic*/
		if(playUntil!=null && playUntil == index) {
			TASmodClient.tickratechanger.pauseGame(true);
			playUntil = null;
			TASstateClient.setOrSend(TASstate.NONE);
			for(long i = inputs.size()-1; i >= index; i--) {
				inputs.remove(i);
			}
			index--;
			TASstateClient.setOrSend(TASstate.RECORDING);
			return;
		}
		
		/*Stop condition*/
		if (index == inputs.size()) {
			unpressContainer();
			TASstateClient.setOrSend(TASstate.NONE);
		}
		/*Continue condition*/
		else {
			TickInputContainer tickcontainer = inputs.get(index);	//Loads the new inputs from the container
			this.keyboard = tickcontainer.getKeyboard().clone();
			this.mouse = tickcontainer.getMouse().clone();
			this.subticks = tickcontainer.getSubticks().clone();
			// check for control bytes
			ControlByteHandler.readCotrolByte(controlBytes.get(index));
		}
		desyncMonitor.playMonitor(index);
	}
	// =====================================================================================================
	// Methods to manipulate inputs

	public int size() {
		return (int) inputs.size();
	}

	public boolean isEmpty() {
		return inputs.isEmpty();
	}

	public int index() {
		return index;
	}

	public BigArrayList<TickInputContainer> getInputs() {
		return inputs;
	}

	public Map<Integer, List<Pair<String, String[]>>> getControlBytes() {
		return controlBytes;
	}
	
	public Map<Integer, List<String>> getComments() {
		return comments;
	}
	
	public void setIndex(int index) throws IndexOutOfBoundsException{
		if(index<=size()) {
			this.index = index;
			if (state == TASstate.PLAYBACK) {
				TickInputContainer tickcontainer = inputs.get(index);
				this.keyboard = tickcontainer.getKeyboard();
				this.mouse = tickcontainer.getMouse();
				this.subticks = tickcontainer.getSubticks();
			}
		}else {
			throw new IndexOutOfBoundsException("Index is bigger than the container");
		}
	}

	public TickInputContainer get(int index) {
		TickInputContainer tickcontainer = null;
		try {
			tickcontainer = inputs.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return tickcontainer;
	}
	
	/**
	 * @return The {@link TickInputContainer} at the current index
	 */
	public TickInputContainer get() {
		return get(index);
	}

	public void clear() {
		TASmod.LOGGER.debug(LoggerMarkers.Playback, "Clearing playback controller");
		inputs = new BigArrayList<TickInputContainer>(directory + File.separator + "temp");
		controlBytes.clear();
		comments.clear();
		index = 0;
		startLocation="";
		desyncMonitor.clear();
		clearCredits();
	}
	
	private void clearCredits() {
		title="Insert Author here";
		authors = "Insert author here";
		playtime = "00:00.0";
		rerecords = 0;
	}

	/**
	 * Used for serializing the input container
	 */
	@Override
	public String toString() {
		if (inputs.isEmpty()) {
			return "null";
		}
		String out = "";
		for (int i = 0; i < inputs.size(); i++) {
			out = out.concat(inputs.get(i).toString() + "\n");
		}
		return out;
	}

	// =====================================================================================================
	// Methods to set and retrieve author, title etc

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRerecords() {
		return rerecords;
	}

	public void setRerecords(int rerecords) {
		this.rerecords = rerecords;
	}

	public String getPlaytime() {
		return playtime;
	}

	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}

	public void setSavestates(String playtime) {
		this.playtime = playtime;
	}

	public void fixTicks() {
		for (int i = 0; i < inputs.size(); i++) {
			inputs.get(i).setTick(i + 1);
		}
	}
	
	public long getStartSeed() {
		return startSeed;
	}

	public void setStartSeed(long startSeed) {
		this.startSeed = startSeed;
	}

	// =====================================================================================================
	// Methods and classes related to the start location of a TAS

	/**
	 * @return The start location of the TAS
	 */
	public String getStartLocation() {
		return startLocation;
	}

	/**
	 * Updates the start location of the input container
	 * 
	 * @param startLocation The start location of the TAS
	 */
	public void setStartLocation(String startLocation) {
		TASmod.LOGGER.debug(LoggerMarkers.Playback, "Setting start location");
		this.startLocation = startLocation;
	}

	/**
	 * Generates a start location from the players position and angle
	 * 
	 * @param player The player of the TAS
	 * @return The start location from the player
	 */
	private String getStartLocation(EntityPlayerSP player) {
		TASmod.LOGGER.debug(LoggerMarkers.Playback, "Retrieving player start location");
		String pos = player.posX + "," + player.posY + "," + player.posZ;
		String pitch = Float.toString(player.rotationPitch);
		String yaw = Float.toString(player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}

	/**
	 * Teleports the player to the start location
	 * 
	 * @param startLocation The start location where the player should be teleported
	 *                      to
	 * @throws NumberFormatException If the location can't be parsed
	 */
	private void tpPlayer(String startLocation) throws NumberFormatException {
		TASmod.LOGGER.debug(LoggerMarkers.Playback, "Teleporting the player to the start location");
		String[] section = startLocation.split(",");
		double x = Double.parseDouble(section[0]);
		double y = Double.parseDouble(section[1]);
		double z = Double.parseDouble(section[2]);

		float angleYaw = Float.parseFloat(section[3]);
		float anglePitch = Float.parseFloat(section[4]);

		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.PLAYBACK_TELEPORT)
					.writeDouble(x)
					.writeDouble(y)
					.writeDouble(z)
					.writeFloat(angleYaw)
					.writeFloat(anglePitch)
					);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ==============================================================

	/**
	 * Clears {@link #keyboard} and {@link #mouse}
	 */
	public void unpressContainer() {
		TASmod.LOGGER.trace(LoggerMarkers.Playback, "Unpressing container");
		keyboard.clear();
		mouse.clear();
	}
	
	// ==============================================================

	public void printCredits() {
		TASmod.LOGGER.trace(LoggerMarkers.Playback, "Printing credits");
		if (state == TASstate.PLAYBACK&&!creditsPrinted) {
			creditsPrinted=true;
			printMessage(title, ChatFormatting.GOLD);
			printMessage("", null);
			printMessage("by " + authors, ChatFormatting.AQUA);
			printMessage("", null);
			printMessage("in " + playtime, null);
			printMessage("", null);
			printMessage("Rerecords: " + rerecords, null);
		}
	}
	
	private void printMessage(String msg, ChatFormatting format) {
		String formatString="";
		if(format!=null)
			formatString=format.toString();
		
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(formatString + msg));
	}

	public void setPlayUntil(int until) {
		this.playUntil  = until;
	}
	
	// ==============================================================
	
	/**
	 * Storage class which stores the keyboard, mouse and subticks of a given tick.
	 * @author Scribble
	 *
	 */
	public static class TickInputContainer implements Serializable {

		private static final long serialVersionUID = -3420565284438152474L;

		private int tick;

		private VirtualKeyboard keyboard;

		private VirtualMouse mouse;

		private VirtualSubticks subticks;

		public TickInputContainer(int tick, VirtualKeyboard keyboard, VirtualMouse mouse, VirtualSubticks subticks) {
			this.tick = tick;
			this.keyboard = keyboard;
			this.mouse = mouse;
			this.subticks = subticks;
		}

		public TickInputContainer(int tick) {
			this.tick = tick;
			this.keyboard = new VirtualKeyboard();
			this.mouse = new VirtualMouse();
			this.subticks = new VirtualSubticks(0, 0);
		}

		@Override
		public String toString() {
			return tick + "|" + keyboard.toString() + "|" + mouse.toString() + "|" + subticks.toString();
		}

		public VirtualKeyboard getKeyboard() {
			return keyboard;
		}

		public VirtualMouse getMouse() {
			return mouse;
		}

		public VirtualSubticks getSubticks() {
			return subticks;
		}

		public int getTick() {
			return tick;
		}

		public void setTick(int tick) {
			this.tick = tick;
		}
		
		@Override
		public TickInputContainer clone() {
			return new TickInputContainer(tick, keyboard, mouse, subticks);
		}
	}
	
	/**
	 * State of the input recorder
	 * @author Scribble
	 *
	 */
	public static enum TASstate {
		/**
		 * The game is neither recording, playing back or paused, is also set when aborting all mentioned states.
		 */
		NONE,
		/**
		 * The game plays back the inputs loaded in {@link InputContainer} and locks user interaction.
		 */
		PLAYBACK,
		/**
		 * The game records inputs to the {@link InputContainer}.
		 */
		RECORDING,
		/**
		 * The playback or recording is paused and may be resumed. Note that the game isn't paused, only the playback. Useful for debugging things.
		 */
		PAUSED;	// #124
	}

	
	public void setStateWhenOpened(TASstate state) {
		TASmodClient.openMainMenuScheduler.add(()->{
				PlaybackController container = TASmodClient.virtual.getContainer();
				if(state == TASstate.RECORDING) {
					long seed = TASmod.ktrngHandler.getGlobalSeedClient();
					container.setStartSeed(seed);
				}
				TASstateClient.setOrSend(state);
		});
	}
	
	// ====================================== Networking
	
	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {
				TASmodPackets.PLAYBACK_SAVE,
				TASmodPackets.PLAYBACK_LOAD,
				TASmodPackets.PLAYBACK_FULLPLAY,
				TASmodPackets.PLAYBACK_FULLRECORD,
				TASmodPackets.PLAYBACK_RESTARTANDPLAY,
				TASmodPackets.PLAYBACK_PLAYUNTIL,
				TASmodPackets.PLAYBACK_TELEPORT,
				TASmodPackets.CLEAR_INNPUTS
				
		};
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		String name = null;
		Minecraft mc = Minecraft.getMinecraft();

		switch (packet) {
		
		case PLAYBACK_SAVE:
			name = TASmodBufferBuilder.readString(buf);
			try {
				TASmodClient.virtual.saveInputs(name);
			} catch (IOException e) {
				if (mc.world != null)
					mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
				else
					e.printStackTrace();
				return;
			}
			if (mc.world != null)
				mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Saved inputs to " + name + ".mctas"));
			else
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Saved inputs to " + name + ".mctas");
			break;

		case PLAYBACK_LOAD:
			name = TASmodBufferBuilder.readString(buf);
			try {
				TASmodClient.virtual.loadInputs(name);
			} catch (IOException e) {
				if (mc.world != null)
					mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
				else
					e.printStackTrace();
				return;
			}
			if (mc.world != null)
				mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Loaded inputs from " + name + ".mctas"));
			else
				TASmod.LOGGER.debug(LoggerMarkers.Playback, "Loaded inputs from " + name + ".mctas");
			break;

		case PLAYBACK_FULLPLAY:
			setStateWhenOpened(TASstate.PLAYBACK);		// Set the state to PLAYBACK when the main menu is opened

			TASmodClient.tickSchedulerClient.add(() -> { // Schedule code to be executed on the next tick
				// Exit the server if you are in one
				if (mc.world != null) {
					mc.world.sendQuittingDisconnectingPacket();
					mc.loadWorld((WorldClient) null);
				}
				mc.displayGuiScreen(new GuiMainMenu());
			});
			break;

		case PLAYBACK_FULLRECORD:
			setStateWhenOpened(TASstate.RECORDING); 	// Set the state to RECORDING when the main menu is opened
			
			TASmodClient.virtual.getContainer().clear();	// Clear inputs
			
			// Schedule code to be executed on the next tick
			TASmodClient.tickSchedulerClient.add(() -> {
				if (mc.world != null) {	// Exit the server if you are in one
					mc.world.sendQuittingDisconnectingPacket();
					mc.loadWorld((WorldClient) null);
				}
				mc.displayGuiScreen(new GuiMainMenu());
			});
			break;

		case PLAYBACK_RESTARTANDPLAY:
			final String finalname = ByteBufferBuilder.readString(buf);
			
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Minecraft.getMinecraft().addScheduledTask(() -> {
				TASmodClient.config.set(ConfigOptions.FileToOpen, finalname);
				System.exit(0);
			});
			break;

		case PLAYBACK_PLAYUNTIL:
			int until = ByteBufferBuilder.readInt(buf);
			TASmodClient.virtual.getContainer().setPlayUntil(until);
			break;
			
		case CLEAR_INNPUTS:
			TASmodClient.virtual.getContainer().clear();
			break;
			
		case PLAYBACK_TELEPORT:
			throw new WrongSideException(packet, Side.CLIENT);

		default:
			throw new PacketNotImplementedException(packet, this.getClass());
		}
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		
		
		//TODO #181 Permissions
		switch (packet) {

		case PLAYBACK_TELEPORT:
			double x = TASmodBufferBuilder.readDouble(buf);
			double y = TASmodBufferBuilder.readDouble(buf);
			double z = TASmodBufferBuilder.readDouble(buf);
			float angleYaw = TASmodBufferBuilder.readFloat(buf);
			float anglePitch = TASmodBufferBuilder.readFloat(buf);
			
			EntityPlayerMP player = TASmod.getServerInstance().getPlayerList().getPlayerByUUID(clientID);
			player.getServerWorld().addScheduledTask(() -> {
				player.rotationPitch = anglePitch;
				player.rotationYaw = angleYaw;

				player.setPositionAndUpdate(x, y, z);
			});
			break;
			
		case CLEAR_INNPUTS:
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_INNPUTS));

		case PLAYBACK_FULLPLAY:
		case PLAYBACK_FULLRECORD:
		case PLAYBACK_RESTARTANDPLAY:
		case PLAYBACK_PLAYUNTIL:
		case PLAYBACK_SAVE:
		case PLAYBACK_LOAD:
			TASmod.server.sendToAll(new TASmodBufferBuilder(buf));
			break;
		default:
			throw new PacketNotImplementedException(packet, this.getClass());
		}
	}
}
