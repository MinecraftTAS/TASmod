package de.scribble.lp.tasmod.savestates.server;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class LoadstatePacket implements IMessage{
	
	public int index;
	
	/**
	 * Load a savestate at the current index
	 */
	public LoadstatePacket() {
		index=-1;
	}
	
	/**
	 * Load the savestate at the specified index
	 * @param index The index to load the savestate
	 */
	public LoadstatePacket(int index) {
		this.index = index;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		index=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(index);
	}
}
