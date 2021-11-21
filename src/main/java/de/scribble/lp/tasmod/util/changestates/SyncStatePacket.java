package de.scribble.lp.tasmod.util.changestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.util.TASstate;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
	
	public static class SyncStatePacketHandler implements IMessageHandler<SyncStatePacket, IMessage> {

		@Override
		public IMessage onMessage(SyncStatePacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				TASmod.containerStateServer.setState(message.getState());
			} else {
				InputContainer container = ClientProxy.virtual.getContainer();
				if (message.getState() != container.getState()) {
					String chatMessage = container.setTASState(message.getState(), message.isVerbose());
					if (!chatMessage.isEmpty()) {
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
					}
				}
			}
			return null;
		}

	}

}
