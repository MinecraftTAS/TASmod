package de.scribble.lp.tasmod.tickratechanger;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TickratePacketHandler implements IMessageHandler<TickratePacket, IMessage>{

	@Override
	public IMessage onMessage(TickratePacket message, MessageContext ctx) {
		if(ctx.side==Side.SERVER) {
			EntityPlayerMP player=ctx.getServerHandler().player;
			if (player.canUseCommand(2, "tickrate")) {
				if (message.isAdvance()) {
					if (TickrateChangerServer.TICKS_PER_SECOND == 0) {
						TickrateChangerServer.advanceTick();
					}
				}else if(message.isPause()) {
					TickrateChangerServer.togglePause();
				}
			}
		}else if(ctx.side==Side.CLIENT) {
			if(message.isAdvance()) {
				TickrateChangerClient.advanceClientTick();
			}else {
				TickrateChangerClient.changeClientTickrate(message.getTickrate());
			}
		}
		return null;
	}
}
