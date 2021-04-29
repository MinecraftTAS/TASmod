package de.scribble.lp.tasmod.savetas;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SaveTASPacket implements IMessage {
	String name;

	public SaveTASPacket() {
	}

	public SaveTASPacket(String recordingName) {
		name = recordingName;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int length = buf.readInt();
		name = (String) buf.readCharSequence(length, Charset.defaultCharset());

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
