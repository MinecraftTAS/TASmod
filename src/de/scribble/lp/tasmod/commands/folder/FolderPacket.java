package de.scribble.lp.tasmod.commands.folder;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FolderPacket implements IMessage{
	int command;
	public FolderPacket() {
	}
	
	public FolderPacket(int command) {
		this.command=command;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		command=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(command);
	}
	
	public int getCommand() {
		return command;
	}
	
	public static class FolderPacketHandler implements IMessageHandler<FolderPacket, IMessage>{

		@Override
		public IMessage onMessage(FolderPacket message, MessageContext ctx) {
			if(ctx.side.isClient()) {
				switch(message.command) {
				case 0:
					OpenStuff.openSavestates();
					break;
				case 1:
					OpenStuff.openTASFolder();
					break;
				}
			}
			return null;
		}
		
	}
}
