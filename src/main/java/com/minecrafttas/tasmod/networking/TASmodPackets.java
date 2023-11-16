package com.minecrafttas.tasmod.networking;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.commands.CommandFolder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.PlaybackSerialiser;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.PlayerHandler.MotionData;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.TickratePauseState;

import net.minecraft.nbt.NBTTagCompound;

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
	 * <p>Opens or closes the savestate screen on the client
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_SCREEN,
	/**
	 * <p>Sends the playerdata of the player to the client, inluding the motion
	 * <p>SIDE: Client<br>
	 * ARGS: {@link NBTTagCompound} compound The playerdata
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
	 * <p>Unloads the chunks on the client side
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_UNLOAD_CHUNKS,
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
	 * <strong>Client->Server</strong> None<br>
	 * <strong>Server->Client</strong> String filename The TASfile name to load on restart
	 */
	PLAYBACK_RESTARTANDPLAY,
	/**
	 * <p>Notifies the client to store the current inputs to a file in {@link PlaybackControllerClient}. This is done using {@link PlaybackSerialiser}
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server</strong> None<br>
	 * <strong>Server->Client</strong> String filename The TASfile name to store
	 */
	PLAYBACK_SAVE,
	/**
	 * <p>Notifies the client to load the inputs from a file in {@link PlaybackControllerClient}. This is done using {@link PlaybackSerialiser}
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
	 * {@link TASstate} state The new state everyone should adapt<br>
	 * boolean verbose If a chat message should be printed
	 */
	PLAYBACK_STATE,
	/**
	 * <p>Opens a TASmod related folder on the file system
	 * <p>The action describes which folder to open:
	 * <ol start=0>
	 * <li>Savestate-Folder</li>
	 * <li>TASFiles-Folder</li>
	 * </ol>
	 * 
	 * <p>Side: CLIENT<br>
	 * ARGS: short action
	 */
	OPEN_FOLDER(Side.CLIENT, (buf, clientID) -> {
		short action = buf.getShort();
		switch (action) {
		case 0:
			CommandFolder.openSavestates();
			break;
		case 1:
			CommandFolder.openTASFolder();
		default:
			break;
		}
	}),
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
}
