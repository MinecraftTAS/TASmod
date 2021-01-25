package de.scribble.lp.tasmod.recording.savestates;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RecordingSavestatePacket implements IMessage{
	private boolean mode;
	private String name;
	
	public RecordingSavestatePacket() {
	}
	/**
	 * Makes a savestate of the recording
	 * @param mode If true: Make a savestate, else load the savestate
	 * @param name Name of the savestated file
	 */
	public RecordingSavestatePacket(boolean mode,String name) {
		this.mode=mode;
		this.name=name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int length=buf.readInt();
		name=(String) buf.readCharSequence(length, Charset.defaultCharset());
		mode=buf.readBoolean();
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(name.length());
		buf.writeCharSequence(name, Charset.defaultCharset());
		buf.writeBoolean(mode);
	}
	public String getName() {
		return name;
	}
	
	public boolean getMode() {
		return mode;
	}
}
