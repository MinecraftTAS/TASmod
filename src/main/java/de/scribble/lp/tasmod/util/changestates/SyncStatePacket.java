package de.scribble.lp.tasmod.util.changestates;

import de.scribble.lp.tasmod.util.TASstate;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Syncs the current state of the input recorder with the state on the server side and witht the state on all other clients
 * 
 * @author ScribbleLP
 *
 */
public class SyncStatePacket implements IMessage {

	private short state;
	private boolean verbose;

	public SyncStatePacket() {
		state = 0;
	}

	public SyncStatePacket(TASstate state) {
		verbose = true;
		this.state = (short) state.getIndex();
	}

	public SyncStatePacket(TASstate state, boolean verbose) {
		this.verbose = verbose;
		this.state = (short) state.getIndex();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		state = buf.readShort();
		verbose = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(state);
		buf.writeBoolean(verbose);
	}

	public TASstate getState() {
		return TASstate.fromIndex(state);
	}

	public boolean isVerbose() {
		return verbose;
	}
}
