package de.scribble.lp.tasmod.playback;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlaybackPacket implements IMessage{
	String filename;

	public PlaybackPacket() {
	}
	public PlaybackPacket(String filename) {
		this.filename=filename;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
	}

}
