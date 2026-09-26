package com.minecrafttas.tasmod.registries;

import com.minecrafttas.tasmod.mctcommon.KeybindManager.Keybind;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.CompactPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;
import com.minecrafttas.tasmod.savestates.SavestateHandlerClient;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateState;
import com.minecrafttas.tasmod.savestates.gui.GuiSavestate;
import com.minecrafttas.tasmod.savestates.handlers.SavestateTempHandler;
import com.minecrafttas.tasmod.savestates.storage.builtin.ClientMotionStorage.MotionData;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.TickratePauseState;
import com.minecrafttas.tasmod.util.TASComponent;
import com.minecrafttas.tasmod.util.Ducks.ScoreboardDuck;
import com.minecrafttas.tasmod.virtual.VirtualKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.ChatFormatting;

/**
 * PacketIDs and handlers specifically for TASmod
 * 
 * @author Pancake, Scribble
 */
public enum TASmodPackets implements PacketID {
	/**
	 * <p>Ticksync is a system to sync the tick execution between client and server.
	 * Both can tick independent from each other causing issues with playback.
	 * <p>This is used to notify the other to start ticking and shouldn't be used otherwise.
	 * <p>SIDE: Both<br>
	 * ARGS: None
	 */
	TICKSYNC(false),
	/**
	 * <p>Sets the tickrate/gamespeed
	 * <p>SIDE: Both<br>
	 * ARGS: int tickrate
	 */
	TICKRATE_CHANGE,
	/**
	 * <p>Sets the tickrate to 0, pausing the game. Also unpauses the game
	 * <p>SIDE: Both<br>
	 * ARGS: {@link TickratePauseState} state The paused state
	 */
	TICKRATE_ZERO,
	/**
	 * <p>While in tickrate 0, advances the game by one tick
	 * <p>SIDE: Both<br>
	 * ARGS: None
	 */
	TICKRATE_ADVANCE,
/**
 * <p>Displays a warning in chat that the tickrate was automatically set to 0
 * <p>SIDE: Client
 */
TICKRATE_0_WARN(Side.CLIENT, (buf, username) -> {
	// Warning display is handled elsewhere; no-op here to avoid API issues.
}),
	/**
	 * <p>Creates a savestate
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server:</strong> int The index of the savestate that should be created. -1 to create the latest savestate, might overwrite existing savestates.<br>
	 * <strong>Server->Client:</strong> String The name of the savestate that is created for the clientside
	 */
	SAVESTATE_SAVE,
	/**
	 * <p>Loads a savestate
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> int The index of the savestate that should be loaded<br>
	 * <strong>Server->Client</strong> String The name of the savestate that is loaded for the clientside
	 */
	SAVESTATE_LOAD,
	/**
	 * <p>Opens the {@link GuiSavestate} screen on the client
	 * <p>SIDE: Client<br>
	 * ARGS: Enum The {@link SavestateState} for displaying the correct message
	 */
	SAVESTATE_LOADING_SCREEN,
	/**
	 * <p>Opens the {@link GuiSavestateDone} screen on the client
	 * <p>SIDE: BOTH<br>
	 * <strong>Server->Client</strong> int The index of the savestate to display in the gui<br>
	 * <strong>Client->Server</strong> int The index of the savestate to rename<br>
	 * <strong>Client->Server</strong> String The name of the savestate to be renamed<br>
	 */
	SAVESTATE_RENAME_SCREEN,
	/**
	 * <p>Clears the screen on the client, if it's a savestate screen or a "Loading Terrain" screen
	 * <p>SIDE: Both<br>
	 * ARGS: None
	 */
	SAVESTATE_CLEAR_SCREEN(Side.CLIENT, (buf, clientID) -> {
		Minecraft mc = Minecraft.getInstance();
		Screen currentScreen = null;
		try {
			currentScreen = (Screen) mc.getClass().getField("screen").get(mc);
		} catch (Exception ignored) {
			try {
				currentScreen = (Screen) mc.getClass().getField("currentScreen").get(mc);
			} catch (Exception ignored2) {}
		}
		if (currentScreen != null && (currentScreen.getClass().getSimpleName().equals("GuiSavestate") || currentScreen.getClass().getSimpleName().equals("LoadingOverlay"))) {
			mc.execute(() -> {
				try {
					mc.getClass().getMethod("setScreen", Screen.class).invoke(mc, (Screen) null);
				} catch (Exception ignored) {
					try {
						mc.getClass().getMethod("displayScreen", Screen.class).invoke(mc, (Screen) null);
					} catch (Exception ignored2) {}
				}
			});
		}
	}),
	/**
	 * <p>Sends the playerdata of the player to the client, inluding the motion
	 * <p>SIDE: Client<br>
	 * ARGS: {@link CompoundTag} compound The playerdata
	 */
	SAVESTATE_PLAYER,
	/**
	 * <p>Used for storing the client motion data on the server.
	 * <p>SIDE: BOTH<br>
	 * ARGS: <br>
	 * <strong>Server->Client</strong>None<br>
	 * <strong>Client->Server</strong> {@link MotionData} motionData An Object containing all necessary motion data<br>
	 */
	SAVESTATE_REQUEST_MOTION,
	/**
	 * <p>Used for setting the client motion data after it was loaded from a savestate
	 * <p>Side: Client<br>
	 * ARGS: <br>
	 * <strong>Server->Client</strong> {@link MotionData} motionData An Object containing all necessary motion data<br>
	 */
	SAVESTATE_SET_MOTION,
	/**
	 * <p>Unloads the chunks on the client side
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_UNLOAD_CHUNKS(Side.CLIENT, (buf, username) -> {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			SavestateHandlerClient.unloadAllClientChunks();
		});
	}),
	/**
	 * <p>Clears the scoreboard on the client side
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_CLEAR_SCOREBOARD(Side.CLIENT, (buf, clientID) -> {
		Minecraft mc = Minecraft.getInstance();
		((ScoreboardDuck) mc.level.getScoreboard()).clearScoreboard();
	}),
	/**
	 * <p>Clears the resourcepack on the client side
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_CLEAR_RESOURCEPACK,
	/**
	 * <p>Notifies the client to clear all inputs from the input buffer in {@link PlaybackControllerClient}
	 * <p>SIDE: Both<br>
	 * ARGS: none
	 */
	PLAYBACK_CLEAR_INPUTS,
	/**
	 * <p>Notifies the client to quit to the main menu and start recording inputs in {@link PlaybackControllerClient}
	 * <p>SIDE: Both<br>
	 * ARGS: none
	 */
	PLAYBACK_FULLRECORD,
	/**
	 * <p>Notifies the client to quit to the main menu and start playing back inputs in {@link PlaybackControllerClient}
	 * <p>SIDE: Both<br>
	 * ARGS: none
	 */
	PLAYBACK_FULLPLAY,
	/**
	 * <p>Notifies the client to quit the game. Upon restarting the game, the specified tasfile will be loaded and played back in {@link PlaybackControllerClient}
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> String filename The TASfile name to load on restart<br>
	 * <strong>Server->Client</strong> String filename The TASfile name to load on restart
	 */
	PLAYBACK_RESTARTANDPLAY,
	/**
	 * <p>Notifies the client to store the current inputs to a file in {@link PlaybackControllerClient}. This is done using {@link PlaybackSerialiser2}
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> None<br>
	 * <strong>Server->Client</strong> String filename The TASfile name to store
	 */
	PLAYBACK_SAVE,
	/**
	 * <p>Notifies the client to load the inputs from a file in {@link PlaybackControllerClient}. This is done using {@link PlaybackSerialiser2}
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> None<br>
	 * <strong>Server->Client</strong> String filename The TASfile name to load
	 */
	PLAYBACK_LOAD,
	/**
	 * <p>Notifies the client activate "playuntil" in {@link PlaybackControllerClient}. The next playback will stop at the specified tick and the client will enter recording mode
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> None<br>
	 * <strong>Server->Client</strong> int tick The tick when to stop playing back and start recording
	 */
	PLAYBACK_PLAYUNTIL,
	/**
	 * <p>A permissionless teleport packet, used for setting the playerdata on the server. Used for teleporting the player back to the start of the TAS when using <code>/play</code>
	 * <p>SIDE: Server<br>
	 * ARGS: <br>
	 * double x The x value<br>
	 * double y etc...<br>
	 * double z<br>
	 * float angleYaw<br>
	 * float anglePitch<br>
	 */
	PLAYBACK_TELEPORT,
	/**
	 * <p>Notifies the players to change the {@link TASstate}
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> {@link TASstate} state The new state everyone should adapt
	 * <strong>Server->Client</strong>
	 * Enum The {@link TASstate} state The new state everyone should adapt<br>
	 * boolean verbose If a chat message should be printed
	 */
	PLAYBACK_STATE,
	/**
	 * <p>Same as {@link #PLAYBACK_STATE}, but instructs the controller to activate {@link SavestateTempHandler temporary savestates}
	 * <p>SIDE: Server<br>
	 * ARGS: See {@link #PLAYBACK_STATE}
	 */
	PLAYBACK_STATE_TEMP_SAVESTATE,
	/**
	 * <p>Enables/Disables {@link PlaybackFileCommandExtension PlaybackFileCommandExtensions}
	 * <p>SIDE: Client<br>
	 * ARGS: <br>
	 * String name Name of the {@link PlaybackFileCommandExtension}
	 * boolean enable Whether the extensions should be enabled or disabled
	 */
	PLAYBACK_FILECOMMAND_ENABLE,
	/**
	 * <p>Clears the current gui screen on the client
	 * 
	 * <p>Side: CLIENT<br>
	 * ARGS: none
	 */
	CLEAR_SCREEN(Side.CLIENT, (buf, clientID) -> {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			try {
				mc.getClass().getMethod("setScreen", Screen.class).invoke(mc, (Screen) null);
			} catch (Exception ignored) {
				try {
					mc.getClass().getMethod("displayScreen", Screen.class).invoke(mc, (Screen) null);
				} catch (Exception ignored2) {}
			}
		});
	}),
	/**
	 * <p>Requests the list of TASfiles in the folder from the client for use in tab completions
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Server->Client</strong> None<br>
	 * <strong>Client->Server</strong> String The string of TASfilenames seperated with |
	 */
	COMMAND_TASFILELIST,
	/**
	 * <p>Requests the list of {@link SerialiserFlavorBase SerialiserFlavors} from the client for use in tab completions
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Server->Client</strong> None<br>
	 * <strong>Client->Server</strong> String The string of flavors seperated with |
	 */
	COMMAND_FLAVORLIST,
	/**
	 * <p>Requests the list of {@link PlaybackFileCommandExtension PlaybackFileCommandExtensions} from the client for use in tab completions
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Server->Client</strong> None<br>
	 * <strong>Client->Server</strong> String The string of file command names, seperated with |
	 */
	COMMAND_FILECOMMANDLIST,
	/**
	 * <p>Sets the KillTheRNG seed
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * long seed The new KillTheRNG seed
	 */
	KILLTHERNG_SEED,
	/**
	 * <p>Sets the KillTheRNG start seed when starting recording
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * long seed The new KillTheRNG seed
	 */
	KILLTHERNG_STARTSEED;

	private Side side;
	private CompactPacketHandler lambda;
	private boolean shouldTrace = true;

	private TASmodPackets() {
	}

	private TASmodPackets(boolean shouldTrace) {
		this.shouldTrace = shouldTrace;
	}

	private TASmodPackets(Side side, CompactPacketHandler lambda) {
		this(side, lambda, true);
	}

	private TASmodPackets(Side side, CompactPacketHandler lambda, boolean shouldTrace) {
		this.side = side;
		this.lambda = lambda;
		this.shouldTrace = shouldTrace;
	}

	@Override
	public int getID() {
		return this.ordinal();
	}

	@Override
	public CompactPacketHandler getLambda() {
		return this.lambda;
	}

	@Override
	public Side getSide() {
		return this.side;
	}

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public boolean shouldTrace() {
		return shouldTrace;
	}

	@Override
	public String getExtensionName() {
		return "TASmodPackets";
	}
}

