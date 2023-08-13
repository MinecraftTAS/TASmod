package com.minecrafttas.tasmod;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.server.Client.Side;
import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.commands.CommandFolder;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;

import net.minecraft.client.Minecraft;

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
	TICKRATE_SET,
	/**
	 * <p>Sets the tickrate to 0, pausing the game. Also unpauses the game
	 * 
	 */
	TICKRATE_ZERO,
	TICKRATE_ADVANCE,
	SAVESTATE_LOAD,
	SAVESTATE_SAVE,
	/**
	 * <p>Opens or closes the savestate screen on the client
	 * <p>SIDE: Client<br>
	 * ARGS: none
	 */
	SAVESTATE_SCREEN(Side.CLIENT, (buf, clientID) -> {
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.currentScreen instanceof GuiSavestateSavingScreen))
			mc.displayGuiScreen(new GuiSavestateSavingScreen());
		else
			mc.displayGuiScreen(null);
	}),
	SAVESTATE_MOTION,
	CLEAR_INNPUTS,
	PLAYBACK_RECORD,
	PLAYBACK_PLAY,
	PLAYBACK_FULLRECORD,
	PLAYBACK_FULLPLAY,
	PLAYBACK_RESTARTANDPLAY,
	PLAYBACK_SAVE,
	PLAYBACK_LOAD,
	PLAYBACK_PLAYUNTIL,
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
