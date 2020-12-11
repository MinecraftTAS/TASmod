package de.scribble.lp.tasmod.tickratechanger;

import java.io.IOException;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

/**
 * Sends tickrate information to the client
 * @author ScribbleLP
 *
 */
public class Tickrate2ServerPackage implements Packet<INetHandlerPlayServer>{
	boolean advance;
	boolean pause;
	
	public Tickrate2ServerPackage(boolean advanceTick, boolean pauseGame) {
		advance=advanceTick;
		
	}
	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		advance=buf.readBoolean();
		pause=buf.readBoolean();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeBoolean(advance);
		buf.writeBoolean(pause);
	}
	@Override
	public void processPacket(INetHandlerPlayServer handler) {
	}
	public boolean isAdvance() {
		return advance;
	}
	public boolean isPause() {
		return pause;
	}
}
