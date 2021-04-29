package de.scribble.lp.tasmod.recording;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RecordingPacket implements IMessage{
	boolean enabled;
	
	public RecordingPacket() {
	}
	public RecordingPacket(boolean enabled) {
		this.enabled=enabled;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		enabled=buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(enabled);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
