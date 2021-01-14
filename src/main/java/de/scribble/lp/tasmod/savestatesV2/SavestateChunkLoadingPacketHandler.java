package de.scribble.lp.tasmod.savestatesV2;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SavestateChunkLoadingPacketHandler implements IMessageHandler<SavestateChunkLoadingPacket, IMessage>{

	@Override
	public IMessage onMessage(SavestateChunkLoadingPacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				if(message.isUnload()) {
					SavestatesChunkControl.unloadAllServerChunks();
					SavestatesChunkControl.flushSaveHandler();
					SavestatesChunkControl.disconnectPlayersFromChunkMap();
				}else {
					SavestatesChunkControl.addPlayersToChunkMap();
				}
			});
		}
		return null;
	}

}
