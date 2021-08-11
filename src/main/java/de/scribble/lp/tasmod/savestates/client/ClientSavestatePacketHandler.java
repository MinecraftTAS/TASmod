package de.scribble.lp.tasmod.savestates.client;

import java.io.IOException;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientSavestatePacketHandler implements IMessageHandler<ClientSavestatePacket, IMessage> {

	@Override
	public IMessage onMessage(ClientSavestatePacket message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			if (message.getMode() == true) {
				try {
					ClientSavestateHandler.savestate(message.getName());
				} catch (SavestateException e) {
					CommonProxy.logger.error(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					ClientSavestateHandler.loadstate(message.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
