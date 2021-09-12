package de.scribble.lp.tasmod.savestates.client;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class InputSavestatesPacket implements IMessage{
	private boolean mode;
	private String name;
	
	public InputSavestatesPacket() {
	}
	/**
	 * Makes a savestate of the recording on the <u>Client</u> 
	 * @param mode If true: Make a savestate, else load the savestate
	 * @param name Name of the savestated file
	 */
	public InputSavestatesPacket(boolean mode,String name) {
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
