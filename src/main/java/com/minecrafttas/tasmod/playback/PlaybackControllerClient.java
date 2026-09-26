package com.minecrafttas.tasmod.playback;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
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

import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.Window;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientInit;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.ByteBufferBuilder;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
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
import com.minecrafttas.tasmod.registries.TASmodConfig;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.util.Ducks.ScreenDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler.Task;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualCameraAngleInput;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;

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
	EventClientTickPost,
	EventDrawScreen,
	
	EventVirtualInput.EventVirtualKeyboardTick,
	EventVirtualInput.EventVirtualMouseTick,
	EventVirtualInput.EventVirtualCameraAngleTick
	
//@formatter:on
{
	private Logger logger = TASmod.LOGGER;

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

	public PlaybackControllerClient() {
		tasFileDirectory = TASmodClient.getTasFileDirectory();

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
					return verbose ? ChatFormatting.RED + "A playback is already running" : "";
				case RECORDING:
					return verbose ? ChatFormatting.RED + "A recording is already running" : "";
				case PAUSED:
					return verbose ? ChatFormatting.RED + "The game is already paused" : "";
				case NONE:
					return verbose ? ChatFormatting.RED + "Nothing is running" : "";
			}

		} else if (state == TASstate.NONE) { // If the container is currently doing nothing
			switch (stateIn) {
				case PLAYBACK:
					startPlayback();
					state = TASstate.PLAYBACK;
					return verbose ? ChatFormatting.GREEN + "Starting playback" : "";
				case RECORDING:
					startRecording();
					state = TASstate.RECORDING;
					return verbose ? ChatFormatting.GREEN + "Starting a recording" : "";
				case PAUSED:
					return verbose ? ChatFormatting.RED + "Can't pause anything because nothing is running" : "";
				case NONE:
					return ChatFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: None)";
			}
		} else if (state == TASstate.RECORDING) { // If the container is currently recording
			switch (stateIn) {
				case PLAYBACK:
					return verbose ? ChatFormatting.RED + "A recording is currently running. Please stop the recording first before starting a playback" : "";
				case RECORDING:
					return ChatFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Recording)";
				case PAUSED:
					LOGGER.debug(LoggerMarkers.Playback, "Pausing a recording");
					state = TASstate.PAUSED;
					stateAfterPause = TASstate.RECORDING;
					return verbose ? ChatFormatting.GREEN + "Pausing a recording" : "";
				case NONE:
					stopRecording();
					state = TASstate.NONE;
					return verbose ? ChatFormatting.GREEN + "Stopping the recording" : "";
			}
		} else if (state == TASstate.PLAYBACK) { // If the container is currently playing back
			switch (stateIn) {
				case PLAYBACK:
					return ChatFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Playback)";
				case RECORDING:
					stopPlayback(false);
					startRecording();
					state = TASstate.RECORDING;
					return verbose ? ChatFormatting.GREEN + "Switching from playback to recording" : "";
				case PAUSED:
					LOGGER.debug(LoggerMarkers.Playback, "Pausing a playback");
					state = TASstate.PAUSED;
					stateAfterPause = TASstate.PLAYBACK;
					TASmodClient.virtual.clearNext();
					return verbose ? ChatFormatting.GREEN + "Pausing a playback" : "";
				case NONE:
					stopPlayback(true);
					state = TASstate.NONE;
					return verbose ? ChatFormatting.GREEN + "Stopping the playback" : "";
			}
		} else if (state == TASstate.PAUSED) {
			switch (stateIn) {
				case PLAYBACK:
					LOGGER.debug(LoggerMarkers.Playback, "Resuming a playback");
					state = TASstate.PLAYBACK;
					stateAfterPause = TASstate.NONE;
					return verbose ? ChatFormatting.GREEN + "Resuming a playback" : "";
				case RECORDING:
					LOGGER.debug(LoggerMarkers.Playback, "Resuming a recording");
					state = TASstate.RECORDING;
					stateAfterPause = TASstate.NONE;
					return verbose ? ChatFormatting.GREEN + "Resuming a recording" : "";
				case PAUSED:
					return ChatFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Paused)";
				case NONE:
					LOGGER.debug(LoggerMarkers.Playback, "Aborting pausing");
					state = TASstate.NONE;
					TASstate statey = stateAfterPause;
					stateAfterPause = TASstate.NONE;
					return ChatFormatting.GREEN + "Aborting a " + statey.toString().toLowerCase() + " that was paused";
			}
		}
		return "Something went wrong ._.";
	}

	private void startRecording() {
		LOGGER.debug(LoggerMarkers.Playback, "Starting recording");
		if (this.inputs.isEmpty()) {
			VirtualCameraAngleInput CAMERA_ANGLE = TASmodClient.virtual.CAMERA_ANGLE;
			Float pitch = CAMERA_ANGLE.getCurrentPitch();
			Float yaw = CAMERA_ANGLE.getCurrentYaw();
			this.currentPlaybackCameraAngle.set(pitch, yaw);

			inputs.add(new InputContainer());
		}
	}

	private void stopRecording() {
		LOGGER.debug(LoggerMarkers.Playback, "Stopping a recording");
		TASmodClient.virtual.clearNext();
	}

private void startPlayback() {
		LOGGER.debug(LoggerMarkers.Playback, "Starting playback");
		// Minecraft.getInstance().options.chatLinks = false; // #119 - private field, using reflection
		try {
			java.lang.reflect.Field field = Minecraft.getInstance().options.getClass().getDeclaredField("chatLinks");
			field.setAccessible(true);
			field.set(Minecraft.getInstance().options, false);
		} catch (Exception ignored) {}
		index = 0;
		//		TASmod.ktrngHandler.setInitialSeed(startSeed);
	}

	private void stopPlayback(boolean clearInputs) {
		LOGGER.debug(LoggerMarkers.Playback, "Stopping a playback");
		// Minecraft.getInstance().options.chatLinks = true; - private field, using reflection
		try {
			java.lang.reflect.Field field = Minecraft.getInstance().options.getClass().getDeclaredField("chatLinks");
			field.setAccessible(true);
			field.set(Minecraft.getInstance().options, true);
		} catch (Exception ignored) {}
		if (clearInputs) {
			TASmodClient.virtual.clearNext();
		}
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
		LOGGER.trace(LoggerMarkers.Playback, "Pausing {}", pause);
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
	public void onDrawScreen(Screen screen, int x, int y) {
		if (!isPlayingback())
			return;

		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.pauseOnLostFocus && !mc.getWindow().isFocused()) // If pause on lost focus is on and the display is not active don't set the cursor position
			return;

		ScreenDuck duckedScreen = (ScreenDuck) screen;
		// Mouse.setCursorPosition(duckedScreen.rescaleX(x), duckedScreen.rescaleY(y));
		// TODO: Rewrite for LWJGL3 - use GLFW.glfwSetCursorPos()
	}

	/**
	 * Updates the input container.<br>
	 * <br>
	 * During a recording this adds the {@linkplain #currentPlaybackKeyboard}, {@linkplain #currentPlaybackMouse}
	 * and {@linkplain #currentPlaybackCameraAngle} to {@linkplain #inputs} and increases the
	 * {@linkplain #index}.<br>
	 * <br>
	 * During playback the opposite is happening, getting the inputs from
	 * {@linkplain #inputs} and temporarily storing them in {@linkplain #currentPlaybackKeyboard},
	 * {@linkplain #currentPlaybackMouse} and {@linkplain #currentPlaybackCameraAngle}.<br>
	 * <br>
	 * Then in {@linkplain VirtualInput}, {@linkplain #currentPlaybackKeyboard},
	 * {@linkplain #currentPlaybackMouse} and {@linkplain #currentPlaybackCameraAngle} are retrieved and emulated as
	 * the next inputs
	 */
	@Override
	public void onClientTickPost(Minecraft mc) {
		/* Stop the playback while player is still loading */
		LocalPlayer player = mc.player;
		if (player != null) { // TODO: addedToChunk field not available in newer mappings
			if (isPaused() && stateAfterPause != TASstate.NONE) { // TODO Find a better solution...
				setTASState(stateAfterPause); // The recording is paused in LoadWorldEvents#startLaunchServer
				pause(false);
				EventListenerRegistry.fireEvent(EventPlaybackJoinedWorld.class, state);
			}
		}

		/* Tick the next playback or recording */
		if (state == TASstate.RECORDING) {
			recordNextTick();
		} else if (state == TASstate.PLAYBACK) {
			playbackNextTick();
		}

//		if (TASmod.isDevEnvironment) {
//			DebugWriter.writeDebugFile(this);
//		}
	}

	private void recordNextTick() {
		index++;
		InputContainer container = new InputContainer(currentPlaybackKeyboard.clone(), currentPlaybackMouse.clone(), currentPlaybackCameraAngle.clone());
		if (inputs.size() <= index) {
			if (inputs.size() < index) {
				LOGGER.warn("Index is {} inputs bigger than the container!", index - inputs.size());
			}
			inputs.add(container);
		} else {
			inputs.set(index, container);
		}

		EventListenerRegistry.fireEvent(EventRecordTick.class, index, container);
	}

	private void playbackNextTick() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.getWindow().isFocused() && mc.options.pauseOnLostFocus) { // Stops the playback when you tab out of minecraft, for once as a failsafe,
																		// secondly as potential exploit protection
			LOGGER.info(LoggerMarkers.Playback, "Stopping a {} since the user tabbed out of the game", state);
			setTASState(TASstate.NONE);
		}

		index++; // Increase the index and load the next inputs

		EventListenerRegistry.fireEvent(EventPlaybackTickPre.class, index);

		/* Stop condition */
		if (index == inputs.size() || inputs.isEmpty()) {
			index--;
			unpressContainer();
			setTASState(TASstate.NONE);
		}
		/* Continue condition */
		else {
			InputContainer container = null;
			if (index + 1 < inputs.size()) {
				container = inputs.get(index + 1); // Loads the new inputs from the container

				this.currentPlaybackKeyboard = this.nextPlaybackKeyboard.clone();
				this.currentPlaybackMouse = this.nextPlaybackMouse.clone();
				this.currentPlaybackCameraAngle = this.nextPlaybackCameraAngle.clone();

				this.nextPlaybackKeyboard = container.getKeyboard().clone();
				this.nextPlaybackMouse = container.getMouse().clone();
				this.nextPlaybackCameraAngle = container.getCameraAngle().clone();
			} else {
				container = inputs.get(index); // Loads the new inputs from the container
				this.currentPlaybackKeyboard = container.getKeyboard().clone();
				this.currentPlaybackMouse = container.getMouse().clone();
				this.currentPlaybackCameraAngle = container.getCameraAngle().clone();
			}

			EventListenerRegistry.fireEvent(EventPlaybackTick.class, index, container);
		}
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
		LOGGER.info(LoggerMarkers.Playback, "Clearing playback controller");
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
		LOGGER.trace(LoggerMarkers.Playback, "Unpressing container");
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
		TASmodClient.openTitleScreenScheduler.add(() -> {
//			PlaybackControllerClient container = TASmodClient.controller;	// Replace with event
//			if (state == TASstate.RECORDING) {
//				long seed = TASmod.ktrngHandler.getGlobalSeedClient();
//				container.setStartSeed(seed);
//			}
			setTASState(state);
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
		String name = null;
		String flavor = null;
		Minecraft mc = Minecraft.getInstance();

		switch (packet) {

			case PLAYBACK_SAVE:
				name = TASmodBufferBuilder.readString(buf);
				flavor = TASmodBufferBuilder.readString(buf);

				try {
					PlaybackSerialiser.saveToFile(tasFileDirectory.resolve(name + fileEnding), this, flavor);
				} catch (PlaybackSaveException e) {
					if (mc.level != null) {
						try {
							mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), Component.literal(ChatFormatting.RED + e.getMessage()));
						} catch (Exception ignored) {}
					}
					LOGGER.catching(e);
					return;
				} catch (Exception e) {
					if (mc.level != null) {
						try {
							mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), Component.literal(ChatFormatting.RED + "Saving failed, something went very wrong"));
						} catch (Exception ignored) {}
					}
					LOGGER.catching(e);
					return;
				}

				if (mc.level != null) {
					Component confirm = Component.literal(ChatFormatting.GREEN + "Saved inputs to " + name + ".mctas" + ChatFormatting.RESET + " [" + ChatFormatting.YELLOW + "Open folder" + ChatFormatting.RESET + "]");
					// TODO: ClickEvent builder not available in newer mappings
					// confirm.getStyle().setClickEvent(ClickEvent.builder(ClickEvent.Action.RUN_COMMAND, "/folder tasfiles").build());
					try {
						mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), confirm);
					} catch (Exception ignored) {}
				} else
					LOGGER.debug(LoggerMarkers.Playback, "Saved inputs to " + name + ".mctas");
				break;

			case PLAYBACK_LOAD:
				name = TASmodBufferBuilder.readString(buf);
				flavor = TASmodBufferBuilder.readString(buf);

				try {
					TASmodClient.controller.setInputs(PlaybackSerialiser.loadFromFile(tasFileDirectory.resolve(name + fileEnding), flavor));
				} catch (PlaybackLoadException e) {
					if (mc.level != null) {
						Component textComponent = Component.literal(e.getMessage());
						try {
							mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), textComponent);
						} catch (Exception ignored) {}
					}
					LOGGER.catching(e);
					return;
				} catch (Exception e) {
					if (mc.level != null) {
						try {
							mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), Component.literal(ChatFormatting.RED + "Loading failed, something went very wrong"));
						} catch (Exception ignored) {}
					}
					LOGGER.catching(e);
					return;
				}

				if (mc.level != null) {
					try {
						mc.gui.getClass().getMethod("getChat").invoke(mc.gui).getClass().getMethod("addMessage", Component.class).invoke(mc.gui.getClass().getMethod("getChat").invoke(mc.gui), Component.literal(ChatFormatting.GREEN + "Loaded inputs from " + name + ".mctas"));
					} catch (Exception ignored) {}
				} else
					LOGGER.debug(LoggerMarkers.Playback, "Loaded inputs from " + name + ".mctas");
				break;

			case PLAYBACK_FULLPLAY:
				setStateWhenOpened(TASstate.PLAYBACK); // Set the state to PLAYBACK when the main menu is opened

				TASmodClient.tickSchedulerClient.add(() -> { // Schedule code to be executed on the next tick
					// Exit the server if you are in one
					if (mc.level != null) {
						// mc.level.sendQuittingDisconnectingPacket(); // Not available in newer mappings
						// mc.loadWorld((ClientLevel) null); // Not available
					}
					try {
						mc.getClass().getMethod("setScreen", Screen.class).invoke(mc, new TitleScreen());
					} catch (Exception e) {
						try {
							mc.getClass().getMethod("displayScreen", Screen.class).invoke(mc, new TitleScreen());
						} catch (Exception ignored) {}
					}
				});
				break;

			case PLAYBACK_FULLRECORD:
				setStateWhenOpened(TASstate.RECORDING); // Set the state to RECORDING when the main menu is opened

				TASmodClient.controller.clear(); // Clear inputs

				// Schedule code to be executed on the next tick
				TASmodClient.tickSchedulerClient.add(() -> {
					TASmodClient.startpositionMetadataExtension.updateStartPosition();
					if (mc.level != null) { // Exit the server if you are in one
						// mc.level.sendQuittingDisconnectingPacket(); // Not available
						// mc.loadWorld((ClientLevel) null); // Not available
					}
					try {
						mc.getClass().getMethod("setScreen", Screen.class).invoke(mc, new TitleScreen());
					} catch (Exception e) {
						try {
							mc.getClass().getMethod("displayScreen", Screen.class).invoke(mc, new TitleScreen());
						} catch (Exception ignored) {}
					}
				});
				break;

			case PLAYBACK_RESTARTANDPLAY:
				String tasFilename = ByteBufferBuilder.readString(buf);

				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mc.execute(() -> {
					TASmodClient.config.set(TASmodConfig.FileToOpen, tasFilename);
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
							Minecraft mcInstance = Minecraft.getInstance();
							if (mcInstance.level != null) {
								try {
									mcInstance.gui.getClass().getMethod("getChat").invoke(mcInstance.gui).getClass().getMethod("addMessage", Component.class).invoke(mcInstance.gui.getClass().getMethod("getChat").invoke(mcInstance.gui), Component.literal(message));
								} catch (Exception ignored) {}
							} else
								LOGGER.debug(LoggerMarkers.Playback, message);
						}
					}
				};

				if ((networkState == TASstate.RECORDING || networkState == TASstate.PLAYBACK) && TASmodClient.tickratechanger.ticksPerSecond != 0) {
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
		String fileOnStart = TASmodClient.config.get(TASmodConfig.FileToOpen);
		if (fileOnStart.isEmpty()) {
			return;
		} else {
			TASmodClient.config.reset(TASmodConfig.FileToOpen);
		}

		try {
			TASmodClient.controller.setInputs(PlaybackSerialiser.loadFromFile(tasFileDirectory.resolve(fileOnStart + fileEnding)));
		} catch (PlaybackLoadException | IOException e) {
			logger.catching(e);
		}

		setTASState(TASstate.PLAYBACK);
	}
}

