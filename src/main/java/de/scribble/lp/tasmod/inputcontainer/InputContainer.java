package de.scribble.lp.tasmod.inputcontainer;

import java.io.File;

import org.lwjgl.opengl.Display;

import com.dselent.bigarraylist.BigArrayList;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.monitoring.DesyncMonitoring;
import de.scribble.lp.tasmod.util.ContainerSerialiser;
import de.scribble.lp.tasmod.util.TASstate;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A container where the inputs are stored.<br>
 * <br>
 * Filling this container is accomplished by setting the state to "recording"
 * via {@linkplain #setRecording(boolean)},<br>
 * or by loading inputs from file.<br>
 * <br>
 * These inputs can be played back at any time by setting
 * {@linkplain #setPlayback(boolean)} to true. <br>
 * <br>
 * Information about the author etc. get stored in the input container too and
 * will be printed out in chat when the player loads into a world <br>
 * Inputs are saved and loaded to/from file via the
 * {@linkplain ContainerSerialiser}
 * 
 * @author ScribbleLP
 *
 */
public class InputContainer {

	/**
	 * The current state of the container.
	 */
	private TASstate state = TASstate.NONE;

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

	public DesyncMonitoring dMonitor = new DesyncMonitoring();
	
	// =====================================================================================================

	private String authors = "Insert author here";

	private String title = "Insert TAS category here";

	private int rerecords = 0;

	private String playtime = "00:00.0";

	private String startLocation = "";

	// =====================================================================================================

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
		if (state == stateIn) {
			switch (stateIn) {
			case PLAYBACK:
				return verbose ? TextFormatting.RED + "A playback is already running" : "";
			case RECORDING:
				return verbose ? TextFormatting.RED + "A recording is already running" : "";
			case NONE:
				return verbose ? TextFormatting.RED + "Nothing is running" : "";
			}

		} else if (state == TASstate.NONE) { // If the container is currently doing nothing
			switch (stateIn) {
			case PLAYBACK:
				if (Minecraft.getMinecraft().player != null && !startLocation.isEmpty()) {
					try {
						tpPlayer(startLocation); // TODO #99 Make this a secondary command
					} catch (NumberFormatException e) {
						state = TASstate.NONE;
						e.printStackTrace();
						return verbose ? TextFormatting.RED + "An error occured while reading the start location of the TAS. The file might be broken" : "";
					}
				}
				Minecraft.getMinecraft().gameSettings.chatLinks = false; // #119
				index = 0;
				state = TASstate.PLAYBACK;
				return verbose ? TextFormatting.GREEN + "Starting playback" : "";
			case RECORDING:
				if (Minecraft.getMinecraft().player != null) {
					startLocation = getStartLocation(Minecraft.getMinecraft().player);
				}
				state = TASstate.RECORDING;
				return verbose ? TextFormatting.GREEN + "Starting a recording" : "";
			case NONE:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: None)";
			}
		} else if (state == TASstate.RECORDING) { // If the container is currently recording
			switch (stateIn) {
			case PLAYBACK:
				return verbose ? TextFormatting.RED + "A recording is currently running. Please stop the recording first before starting a playback" : "";
			case RECORDING:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Recording)";
			case NONE:
				ClientProxy.virtual.unpressEverything();
				state = TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Stopping the recording" : "";
			}
		} else if (state == TASstate.PLAYBACK) { // If the container is currently playing back
			switch (stateIn) {
			case PLAYBACK:
				return TextFormatting.RED + "Please report this message to the mod author, because you should never be able to see this (Error: Playback)";
			case RECORDING:
				return verbose ? TextFormatting.RED + "A playback is currently running. Please stop the playback first before starting a recording" : "";
			case NONE:
				ClientProxy.virtual.unpressEverything();
				state = TASstate.NONE;
				return verbose ? TextFormatting.GREEN + "Stopping the playback" : "";
			}
		}
		return "Something went wrong ._.";
	}

	public boolean isPlayingback() {
		return state == TASstate.PLAYBACK;
	}

	public boolean isRecording() {
		return state == TASstate.RECORDING;
	}

	public boolean isNothingPlaying() {
		return state == TASstate.NONE;
	}

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
		if (state == TASstate.RECORDING) {
			index++;
			inputs.add(new TickInputContainer(index, keyboard.clone(), mouse.clone(), subticks.clone()));
			dMonitor.capturePosition(); // Capturing the current position of the player
		} else if (state == TASstate.PLAYBACK) {
			if (!Display.isActive()) { // Stops the playback when you tab out of minecraft, for once as a failsafe,
										// secondly as potential exploit protection
				setTASState(TASstate.NONE);
			}
			index++;
			if (index == inputs.size()) { // When the last input is supposed to occur
				this.keyboard = new VirtualKeyboard();
				this.mouse = new VirtualMouse();
			} else if (index > inputs.size()) {
				TASstate.setOrSend(TASstate.NONE);
//				TickrateChangerServer.changeServerTickrate(0);	//Tickrate 0 once the playback is done
//				TickrateChangerServer.changeClientTickrate(0);
				index--;
			} else {
				TickInputContainer tickcontainer = inputs.get(index);
				this.keyboard = tickcontainer.getKeyboard().clone();
				this.mouse = tickcontainer.getMouse().clone();
				this.subticks = tickcontainer.getSubticks().clone();
			}
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

	public int index() {
		return index;
	}

	public BigArrayList<TickInputContainer> getInputs() {
		return inputs;
	}

	public void setIndex(int index) {
		this.index = index;
		if (state == TASstate.PLAYBACK) {
			TickInputContainer tickcontainer = inputs.get(index);
			this.keyboard = tickcontainer.getKeyboard();
			this.mouse = tickcontainer.getMouse();
			this.subticks = tickcontainer.getSubticks();
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

	public void clear() {
		inputs = new BigArrayList<TickInputContainer>(directory + File.separator + "temp");
		index = 0;
		dMonitor.getPos().clear();
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
		this.startLocation = startLocation;
	}

	/**
	 * Generates a start location from the players position and angle
	 * 
	 * @param player The player of the TAS
	 * @return The start location from the player
	 */
	private String getStartLocation(EntityPlayerSP player) {
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
		String[] section = startLocation.split(",");
		double x = Double.parseDouble(section[0]);
		double y = Double.parseDouble(section[1]);
		double z = Double.parseDouble(section[2]);

		float angleYaw = Float.parseFloat(section[3]);
		float anglePitch = Float.parseFloat(section[4]);

		CommonProxy.NETWORK.sendToServer(new TeleportPlayerPacket(x, y, z, angleYaw, anglePitch));
	}

	public static class TeleportPlayerPacket implements IMessage {

		double x;
		double y;
		double z;

		float angleYaw;
		float anglePitch;

		public TeleportPlayerPacket(double x, double y, double z, float angleYaw, float anglePitch) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.angleYaw = angleYaw;
			this.anglePitch = anglePitch;
		}

		public TeleportPlayerPacket() {
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.angleYaw = buf.readFloat();
			this.anglePitch = buf.readFloat();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);

			buf.writeFloat(angleYaw);
			buf.writeFloat(anglePitch);
		}

	}

	/**
	 * Permissionless player teleporting packet
	 * 
	 * @author ScribbleLP
	 *
	 */
	public static class TeleportPlayerPacketHandler implements IMessageHandler<TeleportPlayerPacket, IMessage> {

		@Override
		public IMessage onMessage(TeleportPlayerPacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				net.minecraft.entity.player.EntityPlayerMP player = ctx.getServerHandler().player;
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					player.rotationPitch = message.anglePitch;
					player.rotationYaw = message.angleYaw;

					player.setPositionAndUpdate(message.x, message.y, message.z);
				});
			}
			return null;
		}

	}

	// ==============================================================

	/**
	 * Clears {@link #keyboard} and {@link #mouse}
	 */
	public void unpressContainer() {
		keyboard.clear();
		mouse.clear();
	}
}
