package com.minecrafttas.tasmod.playback;

import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_CLEAR_INPUTS;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_FULLPLAY;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_FULLRECORD;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_LOAD;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_RESTARTANDPLAY;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_SAVE;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_STATE;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.mctcommon.events.EventClient.EventClientInit;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.networking.ByteBufferBuilder;
import com.minecrafttas.mctcommon.networking.Client.Side;
import com.minecrafttas.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.config.TASmodClientConfig;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPre;
import com.minecrafttas.tasmod.events.EventClient.EventDrawScreen;
import com.minecrafttas.tasmod.events.EventPlaybackClient;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventControllerStateChange;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventPlaybackJoinedWorld;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventPlaybackTick;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventPlaybackTickPre;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventRecordTick;
import com.minecrafttas.tasmod.events.EventVirtualInput;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata;
import com.minecrafttas.tasmod.playback.tasfile.PlaybackSerialiser;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackSaveException;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.util.DebugWriter;
import com.minecrafttas.tasmod.util.Ducks.GuiScreenDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler.Task;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

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
 * Information about the author etc. get stored in the playback controller too
 * and will be printed out in chat when the player loads into a world <br>
 * Inputs are saved and loaded to/from file via the
 * {@linkplain PlaybackSerialiser} TODO Update with new {@link PlaybackMetadata}
 * 
 * @author Scribble
 *
 */
public class PlaybackControllerClient implements
//@formatter:off
	ClientPacketHandler,
	
	EventClientInit,
	EventClientTickPre,
	EventClientTickPost,
	EventDrawScreen,
	
	EventVirtualInput.EventVirtualKeyboardTick,
	EventVirtualInput.EventVirtualMouseTick,
	EventVirtualInput.EventVirtualCameraAngleTick
	
//@formatter:on
{
	private final Logger logger;

	/**
	 * The current state of the controller.
	 */
	private TASstate state = TASstate.NONE;

	/**
	 * The {@link #state} that this controller will return to, after a pause
	 */
	private TASstate stateAfterPause = TASstate.NONE;

	/**
	 * The current index of the inputs
	 */
	private long index;

	/**
	 * The virtual input instance
	 */
	private final VirtualInput virtual;

	/**
	 * <p>The current keyboard used in the {@link PlaybackControllerClient PlaybackController}
	 * <p>Used during recording to store incoming inputs from the {@link VirtualInput#KEYBOARD}<br>
	 * or stores inputs that are sent to the {@link VirtualInput#KEYBOARD} during playback
	 */
	private VirtualKeyboard currentPlaybackKeyboard = new VirtualKeyboard();
	/**
	 * <p>The current mouse used in the {@link PlaybackControllerClient PlaybackController}
	 * <p>Used during recording to store incoming inputs from the {@link VirtualInput#MOUSE}<br>
	 * or stores inputs that are sent to the {@link VirtualInput#MOUSE} during playback
	 */
	private VirtualMouse currentPlaybackMouse = new VirtualMouse();
	/**
	 * <p>The current camera angle used in the {@link PlaybackControllerClient PlaybackController}
	 * <p>Used during recording to store incoming inputs from the {@link VirtualInput#CAMERA_ANGLE}<br>
	 * or stores inputs that are sent to the {@link VirtualInput#CAMERA_ANGLE} during playback
	 */
	private VirtualCameraAngle currentPlaybackCameraAngle = new VirtualCameraAngle();

	/**
	 * <p>The keyboard in the next playback tick
	 * <p>These inputs will be fed into {@link #currentPlaybackKeyboard} after a tick
	 */
	private VirtualKeyboard nextPlaybackKeyboard = new VirtualKeyboard();
	/**
	 * <p>The mouse in the next playback tick
	 * <p>These inputs will be fed into {@link #currentPlaybackMouse} after a tick
	 */
	private VirtualMouse nextPlaybackMouse = new VirtualMouse();
	/**
	 * <p>The camera angle in the next playback tick
	 * <p>These inputs will be fed into {@link #currentPlaybackCameraAngle} after a tick
	 */
	private VirtualCameraAngle nextPlaybackCameraAngle = new VirtualCameraAngle();
	/**
	 * The directory where to store the tasfiles
	 */
	public final Path tasFileDirectory;
	/**
	 * The file ending of the TASfiles
	 */
	public final Path fileEnding = Paths.get(".mctas");

	/**
	 * The place where all inputs get stored
	 */
	private BigArrayList<InputContainer> inputs;

//	private long startSeed = TASmod.ktrngHandler.getGlobalSeedClient(); // TODO Replace with Metadata extension

	// =====================================================================================================

	public PlaybackControllerClient(VirtualInput virtual, Path tasFileDirectory, Logger logger) {
		this.virtual = virtual;
		this.logger = logger;
		this.tasFileDirectory = tasFileDirectory;
		inputs = new BigArrayList<InputContainer>(tasFileDirectory.resolve("temp").toAbsolutePath().toString());
	}

	/**
	 * Sets the current {@link TASstate}
	 * 
	 * First sends the state to the server.
	 * 
	 * To set the client state, see {@link #setTASStateClient(TASstate)}
	 * 
	 * @param stateIn The new state for all players
	 */
	public void setTASState(TASstate stateIn) {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(PLAYBACK_STATE).writeEnum(stateIn));
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * Starts or stops a recording/playback
	 * 
	 * @param stateIn stateIn The desired state of the container
	 * @return
	 */
	public String setTASStateClient(TASstate stateIn) {
		return setTASStateClient(stateIn, true);
	}

	/**
	 * Starts or stops a recording/playback
	 * 
	 * @param stateIn The desired state of the container
	 * @param verbose Whether the output should be printed in the chat
	 * @return The message printed in the chat
	 */
	public String setTASStateClient(TASstate stateIn, boolean verbose) {
		EventListenerRegistry.fireEvent(EventControllerStateChange.class, stateIn, state);

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
					startPlayback();
					return verbose ? TextFormatting.GREEN + "Starting playback" : "";
				case RECORDING:
					startRecording();
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
					logger.debug(LoggerMarkers.Playback, "Pausing a recording");
					state = TASstate.PAUSED;
					stateAfterPause = TASstate.RECORDING;
					return verbose ? TextFormatting.GREEN + "Pausing a recording" : "";
				case NONE:
					stopRecording();
					return verbose ? TextFormatting.GREEN + "Stopping the recording" : "";
			}
		} else if (state == TASstate.PLAYBACK) { // If the container is currently playing back
			switch (stateIn) {
				case PLAYBACK:
					return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Playback)";
				case RECORDING:
					stopPlayback(false);
					startRecording();
					return verbose ? TextFormatting.GREEN + "Switching from playback to recording" : "";
				case PAUSED:
					logger.debug(LoggerMarkers.Playback, "Pausing a playback");
					state = TASstate.PAUSED;
					stateAfterPause = TASstate.PLAYBACK;
					virtual.clearNext();
					return verbose ? TextFormatting.GREEN + "Pausing a playback" : "";
				case NONE:
					stopPlayback(true);
					return verbose ? TextFormatting.GREEN + "Stopping the playback" : "";
			}
		} else if (state == TASstate.PAUSED) {
			switch (stateIn) {
				case PLAYBACK:
					logger.debug(LoggerMarkers.Playback, "Resuming a playback");
					state = TASstate.PLAYBACK;
					stateAfterPause = TASstate.NONE;
					return verbose ? TextFormatting.GREEN + "Resuming a playback" : "";
				case RECORDING:
					logger.debug(LoggerMarkers.Playback, "Resuming a recording");
					state = TASstate.RECORDING;
					stateAfterPause = TASstate.NONE;
					return verbose ? TextFormatting.GREEN + "Resuming a recording" : "";
				case PAUSED:
					return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Paused)";
				case NONE:
					logger.debug(LoggerMarkers.Playback, "Aborting pausing");
					state = TASstate.NONE;
					TASstate stateAfterPauseTemp = stateAfterPause;
					stateAfterPause = TASstate.NONE;
					return TextFormatting.GREEN + "Aborting a " + stateAfterPauseTemp.toString().toLowerCase() + " that was paused";
			}
		}
		return "Something went wrong ._.";
	}

	private void startRecording() {
		logger.debug(LoggerMarkers.Playback, "Starting recording");
		state = TASstate.RECORDING;
		if (this.inputs.isEmpty()) {
			virtual.preloadInputs();
			inputs.add(new InputContainer(this.currentPlaybackKeyboard.clone(), this.currentPlaybackMouse.clone(), this.currentPlaybackCameraAngle.clone()));
		}
	}

	private void stopRecording() {
		logger.debug(LoggerMarkers.Playback, "Stopping a recording");
		virtual.clearNext();
		state = TASstate.NONE;
	}

	private void startPlayback() {
		logger.debug(LoggerMarkers.Playback, "Starting playback");
		if (inputs.isEmpty())
			return;

		state = TASstate.PLAYBACK;

		InputContainer initialContainer = inputs.get(0);
		this.currentPlaybackKeyboard = initialContainer.getKeyboard().clone();
		this.currentPlaybackMouse = initialContainer.getMouse().clone();
		this.currentPlaybackCameraAngle = initialContainer.getCameraAngle().clone();
		virtual.preloadInputs(initialContainer);

		if (Minecraft.getMinecraft() != null)
			Minecraft.getMinecraft().gameSettings.chatLinks = false; // #119
		index = 0;
	}

	private void stopPlayback(boolean clearInputs) {
		logger.debug(LoggerMarkers.Playback, "Stopping a playback");
		if (Minecraft.getMinecraft() != null)
			Minecraft.getMinecraft().gameSettings.chatLinks = true;
		if (clearInputs) {
			virtual.clearNext();
		}
		state = TASstate.NONE;
	}

	/**
	 * Switches between the paused state and the state it was in before the pause
	 * 
	 * @return The new state
	 */
	public TASstate togglePause() {
		if (state != TASstate.PAUSED) {
			setTASStateClient(TASstate.PAUSED);
		} else {
			setTASStateClient(stateAfterPause);
		}
		return state;
	}

	/**
	 * Forces the playback to pause or unpause
	 * 
	 * @param pause True, if it should be paused
	 */
	public void pause(boolean pause) {
		logger.trace(LoggerMarkers.Playback, "Pausing {}", pause);
		if (pause) {
			if (state != TASstate.NONE) {
				setTASStateClient(TASstate.PAUSED, false);
			}
		} else {
			if (state == TASstate.PAUSED) {
				setTASStateClient(stateAfterPause, false);
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

	public TASstate getStateAfterPause() {
		return stateAfterPause;
	}

	// =====================================================================================================
	// Methods to update the temporary variables of the container.
	// These act as an input and output, depending if a recording or a playback is
	// running

	@Override
	public VirtualKeyboard onVirtualKeyboardTick(VirtualKeyboard vkeyboard) {
		if (state == TASstate.RECORDING) {
			this.currentPlaybackKeyboard.deepCopyFrom(vkeyboard);
		} else if (state == TASstate.PLAYBACK) {
			vkeyboard.deepCopyFrom(this.currentPlaybackKeyboard);
		}
		return vkeyboard.clone();
	}

	@Override
	public VirtualMouse onVirtualMouseTick(VirtualMouse vmouse) {
		if (state == TASstate.RECORDING) {
			this.currentPlaybackMouse.deepCopyFrom(vmouse);
		} else if (state == TASstate.PLAYBACK) {
			vmouse.deepCopyFrom(this.currentPlaybackMouse);
		}
		return vmouse.clone();
	}

	@Override
	public VirtualCameraAngle onVirtualCameraTick(VirtualCameraAngle vcamera) {
		if (state == TASstate.RECORDING) {
			this.currentPlaybackCameraAngle.deepCopyFrom(vcamera);
		} else if (state == TASstate.PLAYBACK) {
			vcamera.deepCopyFrom(this.currentPlaybackCameraAngle);
		}
		return vcamera.clone();
	}

	/**
	 * {@inheritDoc}
	 * <p>Updates the cursor location on screen
	 */
	@Override
	public void onDrawScreen(GuiScreen screen, int x, int y) {
		if (!isPlayingback())
			return;

		Minecraft mc = Minecraft.getMinecraft();
		if (!mc.gameSettings.pauseOnLostFocus && !Display.isActive()) // If pause on lost focus is on and the display is not active don't set the cursor position
			return;

		GuiScreenDuck duckedScreen = (GuiScreenDuck) screen;
		Mouse.setCursorPosition(duckedScreen.rescaleX(x), duckedScreen.rescaleY(y));
	}

	@Override
	public void onClientTickPre(Minecraft mc) {
		if (state == TASstate.PLAYBACK) {
			/* Tick the next playback*/
			playbackNextTick();
		}
	}

	@Override
	public void onClientTickPost(Minecraft mc) {
		/* Stop the playback while player is still loading */
		if (mc != null) {	// Mc can be null during Unit Tests
			EntityPlayerSP player = mc.player;
			if (player != null && player.addedToChunk) {
				if (isPaused() && stateAfterPause != TASstate.NONE) { // TODO Find a better solution...
					setTASState(stateAfterPause); // The recording is paused in LoadWorldEvents#startLaunchServer
					pause(false);
					EventListenerRegistry.fireEvent(EventPlaybackJoinedWorld.class, state);
				}
			}
		}

		/* Tick the next recording */
		if (state == TASstate.RECORDING) {
			recordNextTick();
		}

		if (mc != null)
			DebugWriter.writeDebugFile(this);
	}

	/**
	 * Records the inputs from {@link #currentPlaybackKeyboard} etc. into {@link #inputs}
	 */
	private void recordNextTick() {
		index++;
		InputContainer container = new InputContainer(currentPlaybackKeyboard.clone(), currentPlaybackMouse.clone(), currentPlaybackCameraAngle.clone());
		if (inputs.size() <= index) {
			if (inputs.size() < index) {
				logger.warn("Index is {} inputs bigger than the container!", index - inputs.size());
			}
			inputs.add(container);
		} else {
			inputs.set(index, container);
		}

		EventListenerRegistry.fireEvent(EventRecordTick.class, index, container);
	}

	/**
	 * Retrieves the inputs from {@link #inputs} and adds them to {@link #currentPlaybackKeyboard} etc.
	 * Also updates {@link #nextPlaybackCameraAngle} which is only used for visualizing what the input in the next tick is
	 */
	private void playbackNextTick() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null && !Display.isActive() && mc.gameSettings.pauseOnLostFocus) { // Stops the playback when you tab out of minecraft, for once as a failsafe,
																					// secondly as potential exploit protection
			logger.info(LoggerMarkers.Playback, "Stopping a {} since the user tabbed out of the game", state);
			setTASState(TASstate.NONE);
			return;
		}

		/* Stop condition */
		if (index + 1 == inputs.size() || inputs.isEmpty()) {
			unpressContainer();
			setTASState(TASstate.NONE);
			return;
		}

		index++; // Increase the index and load the next inputs

		EventListenerRegistry.fireEvent(EventPlaybackTickPre.class, index);

		/* Continue condition */

		if (index + 1 < inputs.size()) {
			InputContainer nextContainer = inputs.get(index + 1);

			this.nextPlaybackKeyboard = nextContainer.getKeyboard().clone();
			this.nextPlaybackMouse = nextContainer.getMouse().clone();
			this.nextPlaybackCameraAngle = nextContainer.getCameraAngle().clone();
		} else {
			nextPlaybackKeyboard.clear();
			nextPlaybackMouse.clear();
			nextPlaybackCameraAngle.clear();
		}

		InputContainer container = inputs.get(index); // Loads the new inputs from the container
		this.currentPlaybackKeyboard = container.getKeyboard().clone();
		this.currentPlaybackMouse = container.getMouse().clone();
		this.currentPlaybackCameraAngle = container.getCameraAngle().clone();

		EventListenerRegistry.fireEvent(EventPlaybackTick.class, index, container);
	}
	// =====================================================================================================
	// Methods to manipulate inputs

	public int size() {
		return (int) inputs.size();
	}

	public boolean isEmpty() {
		return inputs.isEmpty();
	}

	public long index() {
		return index;
	}

	public void remove(long index) {
		inputs.remove(index);
		EventListenerRegistry.fireEvent(EventPlaybackClient.EventInputDelete.class, index);
	}

	public BigArrayList<InputContainer> getInputs() {
		return inputs;
	}

	public void setInputs(BigArrayList<InputContainer> inputs) {
		this.setInputs(inputs, 0);
	}

	public void setInputs(BigArrayList<InputContainer> inputs, long index) {
		clearInputList();
		this.inputs.addAll(inputs);
		setIndex(index);
	}

	public void setIndex(long index) throws IndexOutOfBoundsException {
		if (index <= size()) {
			this.index = index;
			if (state == TASstate.PLAYBACK) {
				InputContainer inputcontainer = inputs.get(index);
				this.currentPlaybackKeyboard = inputcontainer.getKeyboard();
				this.currentPlaybackMouse = inputcontainer.getMouse();
				this.currentPlaybackCameraAngle = inputcontainer.getCameraAngle();
			}
		} else {
			throw new IndexOutOfBoundsException("Index is bigger than the container");
		}
	}

	public InputContainer get(long index) {
		InputContainer inputcontainer = null;
		try {
			inputcontainer = inputs.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return inputcontainer;
	}

	/**
	 * @return The {@link InputContainer} at the current index
	 */
	public InputContainer get() {
		return get(index);
	}

	public void clear() {
		logger.info(LoggerMarkers.Playback, "Clearing playback controller");
		clearInputList();
		EventListenerRegistry.fireEvent(EventPlaybackClient.EventRecordClear.class);

		index = 0;
	}

	private void clearInputList() {
		inputs.clear();
	}

	public VirtualKeyboard getNextPlaybackKeyboard() {
		return nextPlaybackKeyboard;
	}

	public VirtualMouse getNextPlaybackMouse() {
		return nextPlaybackMouse;
	}

	public VirtualCameraAngle getNextPlaybackCameraAngle() {
		return nextPlaybackCameraAngle;
	}

	public List<String> getNextKeyboardPresses() {
		return nextPlaybackKeyboard.getCurrentPresses();
	}

	public List<String> getNextMousePresses() {
		return nextPlaybackMouse.getCurrentPresses();
	}

	public VirtualMouse getNextMouse() {
		return nextPlaybackMouse;
	}

	/**
	 * Used for displaying the rought contents of the input container
	 */
	@Override
	public String toString() {
		if (inputs.isEmpty()) {
			return "null";
		}
		List<String> out = new LinkedList<>();
		for (int i = 0; i < inputs.size(); i++) {
			InputContainer input = inputs.get(i);
			out.add(input.toString(i));
		}
		return String.join("\n", out);
	}

	// ==============================================================

	/**
	 * Clears {@link #currentPlaybackKeyboard} and {@link #currentPlaybackMouse}
	 */
	public void unpressContainer() {
		logger.trace(LoggerMarkers.Playback, "Unpressing container");
		currentPlaybackKeyboard.clear();
		currentPlaybackMouse.clear();
	}

	// ==============================================================

	/**
	 * Storage class which stores the keyboard, mouse, subticks and comments of a given tick.
	 * 
	 * @author Scribble
	 *
	 */
	public static class InputContainer implements Serializable {

		private VirtualKeyboard keyboard;

		private VirtualMouse mouse;

		private VirtualCameraAngle cameraAngle;

		private CommentContainer comments;

		public InputContainer(VirtualKeyboard keyboard, VirtualMouse mouse, VirtualCameraAngle subticks) {
			this(keyboard, mouse, subticks, new CommentContainer());
		}

		public InputContainer(VirtualKeyboard keyboard, VirtualMouse mouse, VirtualCameraAngle camera, CommentContainer comments) {
			this.keyboard = keyboard;
			this.mouse = mouse;
			this.cameraAngle = camera;
			this.comments = comments;
		}

		public InputContainer() {
			this(new VirtualKeyboard(), new VirtualMouse(), new VirtualCameraAngle());
		}

		@Override
		public String toString() {
			return toString(-1);
		}

		public String toString(int tick) {
			List<String> out = new LinkedList<>();
			out.addAll(comments.inlineComments);

			Queue<String> keyboardQueue = new LinkedBlockingQueue<>(Arrays.asList(keyboard.toString().split("\n")));
			Queue<String> mouseQueue = new LinkedBlockingQueue<>(Arrays.asList(mouse.toString().split("\n")));
			Queue<String> cameraAngleQueue = new LinkedBlockingQueue<>(Arrays.asList(cameraAngle.toString().split("\n")));
			Queue<String> endlineCommentQueue = new LinkedBlockingQueue<>(comments.endlineComments);

			String kb = getOrEmpty(keyboardQueue.poll());
			String ms = getOrEmpty(mouseQueue.poll());
			String ca = getOrEmpty(cameraAngleQueue.poll());

			String elc = getOrEmpty(endlineCommentQueue.poll());
			if (!elc.isEmpty()) {
				elc = "\t\t" + elc;
			}

			out.add(String.format("%s|%s|%s|%s%s", tick == -1 ? "undefined" : tick, kb, ms, ca, elc));

			// Add subtick lines, indented
			int currentSubtick = 0;
			while (!keyboardQueue.isEmpty() || !mouseQueue.isEmpty() || !cameraAngleQueue.isEmpty()) {
				currentSubtick++;
				kb = getOrEmpty(keyboardQueue.poll());
				ms = getOrEmpty(mouseQueue.poll());
				ca = getOrEmpty(cameraAngleQueue.poll());
				elc = getOrEmpty(endlineCommentQueue.poll());
				if (!elc.isEmpty()) {
					elc = "\t\t" + elc;
				}

				out.add(String.format("\t%s|%s|%s|%s%s", currentSubtick, kb, ms, ca, elc));
			}
			return String.join("\n", out);
		}

		private String getOrEmpty(String string) {
			return string != null ? string : "";
		}

		public VirtualKeyboard getKeyboard() {
			return keyboard;
		}

		public VirtualMouse getMouse() {
			return mouse;
		}

		public VirtualCameraAngle getCameraAngle() {
			return cameraAngle;
		}

		/**
		 * @return The comment container of this controller. If {@link #comments} is null, returns an empty container.
		 */
		public CommentContainer getComments() {
			if (comments == null) {
				return new CommentContainer();
			}
			return comments;
		}

		@Override
		public InputContainer clone() {
			return new InputContainer(keyboard, mouse, cameraAngle);
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof InputContainer) {
				InputContainer container = (InputContainer) other;
				return keyboard.equals(container.keyboard) && mouse.equals(container.mouse) && cameraAngle.equals(container.cameraAngle) && comments.equals(container.comments);
			}
			return super.equals(other);
		}
	}

	/**
	 * Storage class for storing {@link CommentContainer#inlineComments inline} and {@link CommentContainer#endlineComments endline} comments
	 * 
	 * @author Scribble
	 */
	public static class CommentContainer implements Serializable {

		/**
		 * List of all inline comments in a tick.<br>
		 * These comments take the form:
		 * 
		 * <pre>
		 * // This is an inline comment
		 * // This is a second inline comment
		 * 1|W;w|;0;0;0|0.0;0.0
		 * 	1|||1.0;1.0
		 * </pre>
		 * 
		 * Inline comments are supposed to describe the tick as a whole and therefore
		 * can not be attached to subticks.<br>
		 * like so:
		 * 
		 * <pre>
		 * 1|W;w|;0;0;0|0.0;0.0
		 * // This is not allowed. This comment won't be saved
		 * 	1|||1.0;1.0
		 * </pre>
		 */
		private List<String> inlineComments;

		/**
		 * List of all endline comments.<br>
		 * These comments take the form:
		 * 
		 * <pre>
		 * 1|W;w|;0;0;0|0.0;0.0		// This is an endline comment
		 * 	1|||1.0;1.0		// This is a second endline comment
		 * </pre>
		 * 
		 * Endline comments are supposed to describe individual subticks.<br>
		 */
		private List<String> endlineComments;

		public CommentContainer() {
			this(new ArrayList<>(), new ArrayList<>());
		}

		public CommentContainer(List<String> inlineComments, List<String> endlineComments) {
			this.inlineComments = inlineComments;
			this.endlineComments = endlineComments;
		}

		public void addInlineComment(String inlineComment) {
			inlineComments.add(inlineComment);
		}

		public void addEndlineComment(String endlineComment) {
			endlineComments.add(endlineComment);
		}

		public List<String> getInlineComments() {
			return inlineComments;
		}

		public List<String> getEndlineComments() {
			return endlineComments;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CommentContainer) {
				CommentContainer other = (CommentContainer) obj;
				return inlineComments.equals(other.inlineComments) && endlineComments.equals(other.endlineComments);
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			return inlineComments.toString() + "\n\n" + endlineComments.toString();
		}
	}

	/**
	 * State of the input recorder
	 * 
	 * @author Scribble
	 *
	 */
	public static enum TASstate {
		/**
		 * The game is neither recording, playing back or paused, is also set when
		 * aborting all mentioned states.
		 */
		NONE,
		/**
		 * The game plays back the inputs loaded in {@link InputContainer} and locks
		 * user interaction.
		 */
		PLAYBACK,
		/**
		 * The game records inputs to the {@link InputContainer}.
		 */
		RECORDING,
		/**
		 * The playback or recording is paused and may be resumed. Note that the game
		 * isn't paused, only the playback. Useful for debugging things.
		 */
		PAUSED; // #124
	}

	public void setStateWhenOpened(TASstate state) {
		TASmodClient.openMainMenuScheduler.add(() -> {
//			PlaybackControllerClient container = TASmodClient.controller;	// Replace with event
//			if (state == TASstate.RECORDING) {
//				long seed = TASmod.ktrngHandler.getGlobalSeedClient();
//				container.setStartSeed(seed);
//			}
			setTASStateClient(state);
		});
	}

	// ====================================== Networking

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		//@formatter:off
		return new TASmodPackets[] {
				PLAYBACK_SAVE,
				PLAYBACK_LOAD,
				PLAYBACK_FULLPLAY,
				PLAYBACK_FULLRECORD,
				PLAYBACK_RESTARTANDPLAY,
				PLAYBACK_CLEAR_INPUTS,
				PLAYBACK_STATE

		};
		//@formatter:on
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		String filename = null;
		String flavor = null;
		Minecraft mc = Minecraft.getMinecraft();

		switch (packet) {

			case PLAYBACK_SAVE:
				filename = TASmodBufferBuilder.readString(buf);
				flavor = TASmodBufferBuilder.readString(buf);

				try {
					PlaybackSerialiser.saveToFile(tasFileDirectory.resolve(filename), this, flavor);
				} catch (PlaybackSaveException e) {
					if (mc.world != null)
						mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
					logger.catching(e);
					return;
				} catch (Exception e) {
					if (mc.world != null)
						mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + "Saving failed, something went very wrong"));
					logger.catching(e);
					return;
				}

				if (mc.world != null) {
					TextComponentString confirm = new TextComponentString(TextFormatting.GREEN + "Saved inputs to " + filename + TextFormatting.RESET + " [" + TextFormatting.YELLOW + "Open folder" + TextFormatting.RESET + "]");
					confirm.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/folder tasfiles"));
					mc.ingameGUI.getChatGUI().printChatMessage(confirm);
				} else
					logger.debug(LoggerMarkers.Playback, "Saved inputs to " + filename);
				break;

			case PLAYBACK_LOAD:
				filename = TASmodBufferBuilder.readString(buf);
				flavor = TASmodBufferBuilder.readString(buf);
				String name = FilenameUtils.getBaseName(filename);
				String extension = FilenameUtils.getExtension(filename);
				if (extension.isEmpty())
					extension += fileEnding;

				try {
					TASmodClient.controller.setInputs(PlaybackSerialiser.loadFromFile(tasFileDirectory.resolve(String.format("%s.%s", name, extension)), flavor));
				} catch (PlaybackLoadException e) {
					if (mc.world != null) {
						TextComponentString textComponent = new TextComponentString(e.getMessage());
						mc.ingameGUI.getChatGUI().printChatMessage(textComponent);
					}
					logger.catching(e);
					return;
				} catch (Exception e) {
					if (mc.world != null)
						mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + "Loading failed, something went very wrong"));
					logger.catching(e);
					return;
				}

				if (mc.world != null)
					mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Loaded inputs from " + filename));
				else
					logger.debug(LoggerMarkers.Playback, "Loaded inputs from " + filename + ".mctas");
				break;

			case PLAYBACK_FULLPLAY:
				setStateWhenOpened(TASstate.PLAYBACK); // Set the state to PLAYBACK when the main menu is opened

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
				setStateWhenOpened(TASstate.RECORDING); // Set the state to RECORDING when the main menu is opened

				TASmodClient.controller.clear(); // Clear inputs

				// Schedule code to be executed on the next tick
				TASmodClient.tickSchedulerClient.add(() -> {
					TASmodClient.startpositionMetadataExtension.updateStartPosition();
					if (mc.world != null) { // Exit the server if you are in one
						mc.world.sendQuittingDisconnectingPacket();
						mc.loadWorld((WorldClient) null);
					}
					mc.displayGuiScreen(new GuiMainMenu());
				});
				break;

			case PLAYBACK_RESTARTANDPLAY:
				String tasFilename = ByteBufferBuilder.readString(buf);

				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Minecraft.getMinecraft().addScheduledTask(() -> {
					TASmodClient.config.set(TASmodClientConfig.FileToOpen, tasFilename);
					System.exit(0);
				});
				break;

			case PLAYBACK_CLEAR_INPUTS:
				TASmodClient.controller.clear();
				break;

			case PLAYBACK_TELEPORT:
				throw new WrongSideException(packet, Side.CLIENT);

			case PLAYBACK_STATE:
				TASstate networkState = TASmodBufferBuilder.readEnum(TASstate.class, buf);
				boolean verbose = TASmodBufferBuilder.readBoolean(buf);
				Task task = () -> {
					PlaybackControllerClient container = TASmodClient.controller;
					if (networkState != container.getState()) {

						String message = container.setTASStateClient(networkState, verbose);

						if (!message.isEmpty()) {
							if (Minecraft.getMinecraft().world != null)
								Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
							else
								logger.debug(LoggerMarkers.Playback, message);
						}
					}

				};

				if (networkState == TASstate.RECORDING && TASmodClient.tickratechanger.ticksPerSecond != 0) {
					TASmodClient.tickSchedulerClient.add(task); // Starts a recording in the next tick
				} else {
					TASmodClient.gameLoopSchedulerClient.add(task); // Starts a recording in the next frame
				}
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.CLIENT);
		}
	}

	/**
	 * Runs on client initialization, used for loading the TASfile after /restartandplay
	 */
	@Override
	public void onClientInit(Minecraft mc) {
		// Execute /restartandplay. Load the file to start from the config. If it exists load the playback file on start.
		String fileOnStart = TASmodClient.config.get(TASmodClientConfig.FileToOpen);
		if (fileOnStart.isEmpty()) {
			return;
		} else {
			TASmodClient.config.reset(TASmodClientConfig.FileToOpen);
		}

		try {
			TASmodClient.controller.setInputs(PlaybackSerialiser.loadFromFile(tasFileDirectory.resolve(fileOnStart + fileEnding)));
		} catch (PlaybackLoadException | IOException e) {
			logger.catching(e);
		}

		setTASState(TASstate.PLAYBACK);
	}
}
