package de.scribble.lp.tasmod.commands.changestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestStatePacketHandler implements IMessageHandler<RequestStatePacket, IMessage>{

	@Override
	public IMessage onMessage(RequestStatePacket message, MessageContext ctx) {
		if(ctx.side.isClient()) {
			CommonProxy.NETWORK.sendToServer(new SyncStatePacket(ClientProxy.virtual.getContainer().getState(), false));
		}
		return null;
	}

}
