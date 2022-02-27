package de.scribble.lp.tasmod.savestates.client;

import java.io.IOException;
import java.nio.charset.Charset;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
	
	public static class InputSavestatesPacketHandler implements IMessageHandler<InputSavestatesPacket, IMessage> {

		@Override
		public IMessage onMessage(InputSavestatesPacket message, MessageContext ctx) {
			if (ctx.side.isClient()) {
				if (message.getMode() == true) {
					try {
						InputSavestatesHandler.savestate(message.getName());
					} catch (SavestateException e) {
						CommonProxy.logger.error(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						InputSavestatesHandler.loadstate(message.getName());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}
