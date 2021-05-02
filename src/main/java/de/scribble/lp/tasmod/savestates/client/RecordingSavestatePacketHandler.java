package de.scribble.lp.tasmod.savestates.client;

import java.io.IOException;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RecordingSavestatePacketHandler implements IMessageHandler<RecordingSavestatePacket, IMessage> {

	@Override
	public IMessage onMessage(RecordingSavestatePacket message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			if (message.getMode() == true) {
				try {
					RecordingSavestateHandler.savestateRecording(message.getName());
				} catch (SavestateException e) {
					CommonProxy.logger.error(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					RecordingSavestateHandler.loadRecording(message.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
