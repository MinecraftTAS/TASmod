package com.minecrafttas.tasmod.networking;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.commands.CommandFolder;
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
	 * 
	 * <p>This is used to notify the other to start ticking and shouldn't be used otherwise.
	 * 
	 * <p>SIDE: Both<br>
	 * ARGS: None
	 */
	TICKSYNC,
	/**
	 * <p>Sets the tickrate/gamespeed
	 * 
	 * <p>SIDE: Both<br>
	 * ARGS: int tickrate
	 */
	TICKRATE_CHANGE,
	/**
	 * <p>Sets the tickrate to 0, pausing the game. Also unpauses the game
	 * 
	 * <p>SIDE: Both<br>
	 * ARGS: {@link TickratePauseState} state The paused state
	 */
	TICKRATE_ZERO,
	/**
	 * <p>While in tickrate 0, advances the game by one tick
	 * 
	 * <p>SIDE: Both<br>
	 * ARGS: None
	 */
	TICKRATE_ADVANCE,
	/**
	 * <p>Creates a savestate
	 * 
	 * <p>SIDE: Both<br>
	 * ARGS: <br>
	 * <strong>Client->Server:</strong> int The index of the savestate that should be created. -1 to create the latest savestate, might overwrite existing savestates.<br>
	 * <strong>Server->Client:</strong> String The name of the savestate that is created for the clientside
	 */
	SAVESTATE_SAVE,
	/**
	 * <p>Loads a savestate
	 * 
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
	SAVESTATE_REQUEST_MOTION,
	SAVESTATE_UNLOAD_CHUNKS,
	CLEAR_INNPUTS,
	PLAYBACK_FULLRECORD,
	PLAYBACK_FULLPLAY,
	PLAYBACK_RESTARTANDPLAY,
	PLAYBACK_SAVE,
	PLAYBACK_LOAD,
	PLAYBACK_PLAYUNTIL,
	PLAYBACK_TELEPORT,
	STATESYNC_INITIAL,
	STATESYNC,
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
	KILLTHERNG_SEED,
	KILLTHERNG_STARTSEED;

	private Side side;
	private CompactPacketHandler lambda;
	
	private TASmodPackets() {
	}
	
	private TASmodPackets(Side side, CompactPacketHandler lambda) {
		this.side = side;
		this.lambda = lambda;
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
	
}
