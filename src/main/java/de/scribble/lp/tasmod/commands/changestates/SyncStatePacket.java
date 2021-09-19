package de.scribble.lp.tasmod.commands.changestates;

import de.scribble.lp.tasmod.util.TASstate;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SyncStatePacket implements IMessage{

	private short state;
	private boolean verbose;
	
	public SyncStatePacket() {
		state=0;
	}
	
	public SyncStatePacket(TASstate state) {
		verbose=true;
		switch(state) {
		case NONE:
			this.state=0;
			break;
		case PLAYBACK:
			this.state=1;
			break;
		case RECORDING:
			this.state=2;
			break;
		}
	}
	
	public SyncStatePacket(TASstate state, boolean verbose) {
		this.verbose=verbose;
		switch(state) {
		case NONE:
			this.state=0;
			break;
		case PLAYBACK:
			this.state=1;
			break;
		case RECORDING:
			this.state=2;
			break;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		state=buf.readShort();
		verbose=buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(state);
		buf.writeBoolean(verbose);
	}

	public TASstate getState() {
		switch (state) {
		case 0:
			return TASstate.NONE;
		case 1:
			return TASstate.PLAYBACK;
		case 2:
			return TASstate.RECORDING;
		}
		return TASstate.NONE;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
}
