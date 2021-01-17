package de.scribble.lp.tasmod.savestates;

import de.scribble.lp.tasmod.misc.SavestateException;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SavestatePacketHandler implements IMessageHandler<SavestatePacket, IMessage>{

	@Override
	public IMessage onMessage(SavestatePacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				if (!ctx.getServerHandler().player.canUseCommand(2, "tickrate")) {
					return;
				}
				try {
					SavestateHandler.saveState();
				} catch (SavestateException e) {
					e.printStackTrace();
				}
			});
		}
		return null;
	}

}
