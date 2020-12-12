package de.scribble.lp.tasmod.ticksync;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TickSyncPackage implements IMessage{
	private int ticks;
	private boolean shouldreset;
	private boolean shouldstop;
	
	public TickSyncPackage() {
		ticks=0;
		shouldreset=false;
		shouldstop=false;
	}
	
	public TickSyncPackage(int ticks, boolean reset, boolean stop) {
		this.ticks=ticks;
		this.shouldreset=reset;
		this.shouldstop=stop;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.ticks=buf.readInt();
		this.shouldreset=buf.readBoolean();
		this.shouldstop=buf.readBoolean();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ticks);
		buf.writeBoolean(shouldreset);
		buf.writeBoolean(shouldstop);
	}
	
	public int getTicks() {
		return ticks;
	}
	
	public boolean isShouldreset() {
		return shouldreset;
	}
	
	public boolean isShouldstop() {
		return shouldstop;
	}
}
