package de.scribble.lp.tasmod.folder;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FolderPacketHandler implements IMessageHandler<FolderPacket, IMessage>{

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
