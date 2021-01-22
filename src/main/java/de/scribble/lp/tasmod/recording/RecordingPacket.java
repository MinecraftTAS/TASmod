package de.scribble.lp.tasmod.recording;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RecordingPacket implements IMessage{
	String name;
	
	public RecordingPacket() {
	}
	public RecordingPacket(String name) {
		this.name=name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int length=buf.readInt();
		name=(String) buf.readCharSequence(length, Charset.defaultCharset());
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(name.length());
		buf.writeCharSequence(name, Charset.defaultCharset());
	}
	public String getName() {
		return name;
	}
}
