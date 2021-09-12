package de.scribble.lp.tasmod.commands.playback;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlaybackPacket implements IMessage{
	private boolean enabled;
	public PlaybackPacket() {
	}
	
	public PlaybackPacket(boolean enabled) {
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
