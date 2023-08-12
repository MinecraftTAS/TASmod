package com.minecrafttas.tasmod;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.server.Client.Side;
import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;

import net.minecraft.client.Minecraft;

public enum TASmodPackets implements PacketID {
	/**
	 * <p>Ticksync is a system to sync the tick execution between client and server.
	 * Both can tick independent from each other causing issues with playback.
	 * 
	 * <p>This is used to notify the other to start ticking and shouldn't be used otherwise.
	 */
	TICKSYNC,
	TICKRATE_SET,
	TICKRATE_ADVANCE,
	SAVESTATE_LOAD,
	SAVESTATE_SAVE,
	/**
	 * <p>CLIENT ONLY.
	 * <p>Opens or closes the savestate screen on the client
	 */
	SAVESTATE_SCREEN(Side.CLIENT, (buf, clientID) -> {
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.currentScreen instanceof GuiSavestateSavingScreen))
			mc.displayGuiScreen(new GuiSavestateSavingScreen());
		else
			mc.displayGuiScreen(null);
	});

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

	
	
}
