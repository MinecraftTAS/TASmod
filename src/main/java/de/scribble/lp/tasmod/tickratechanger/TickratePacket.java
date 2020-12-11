package de.scribble.lp.tasmod.tickratechanger;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Sends tickrate information to the client
 * @author ScribbleLP
 *
 */
public class TickratePacket implements IMessage{

	float tickrate;
	boolean advance;
	private boolean pause;
	
	public TickratePacket() {
	}
	public TickratePacket(boolean advanceTick, float tickrate, boolean pauseGame) {
		this.tickrate=tickrate;
		this.advance=advanceTick;
		pause=pauseGame;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		tickrate=buf.readFloat();
		advance=buf.readBoolean();
		pause=buf.readBoolean();
	}
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(tickrate);
		buf.writeBoolean(advance);
		buf.writeBoolean(pause);
	}
	public float getTickrate() {
		return tickrate;
	}
	public boolean isAdvance() {
		return advance;
	}
	public boolean isPause() {
		return pause;
	}
}
