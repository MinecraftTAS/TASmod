package de.scribble.lp.tasmod.loadtas;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class LoadTASPacket implements IMessage{
	private String name;
	
	public LoadTASPacket() {
	}

	public LoadTASPacket(String name) {
		this.name=name;
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
