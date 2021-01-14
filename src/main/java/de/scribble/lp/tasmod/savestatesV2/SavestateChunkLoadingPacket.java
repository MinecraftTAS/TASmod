package de.scribble.lp.tasmod.savestatesV2;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SavestateChunkLoadingPacket implements IMessage{
	private boolean unload;
	public SavestateChunkLoadingPacket() {
		unload=true;
	}
	public SavestateChunkLoadingPacket(boolean unload) {
		this.unload=unload;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		unload=buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(unload);
	}
	public boolean isUnload() {
		return unload;
	}
}
