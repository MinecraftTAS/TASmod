package de.scribble.lp.tasmod.recording;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RecordingPacketHandler implements IMessageHandler<RecordingPacket, IMessage>{

	@Override
	public IMessage onMessage(RecordingPacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			System.out.println(message.getName());
		}
		return null;
	}

}
