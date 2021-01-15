package de.scribble.lp.tasmod.savestates.chunkloading;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SavestateChunkLoadingPacketHandler implements IMessageHandler<SavestateChunkLoadingPacket, IMessage>{

	@Override
	public IMessage onMessage(SavestateChunkLoadingPacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				if(message.isUnload()) {
					WorldServer[] worlds=ctx.getServerHandler().player.getServer().worlds;
					for(WorldServer world: worlds) {
						world.disableLevelSaving=false;		//Disabeling level saving for all worlds in case the auto save kicks in during world unlaod
					}
					SavestatesChunkControl.unloadAllServerChunks();
					SavestatesChunkControl.flushSaveHandler();
					SavestatesChunkControl.disconnectPlayersFromChunkMap();
				}else {
					SavestatesChunkControl.addPlayersToChunkMap();
					MinecraftServer server=ctx.getServerHandler().player.getServer();
					WorldServer[] worlds=ctx.getServerHandler().player.getServer().worlds;
					for(WorldServer world: worlds) {
						world.disableLevelSaving=true;
					}
				}
			});
		}
		return null;
	}

}
