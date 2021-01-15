package de.scribble.lp.tasmod.savestates.playerloading;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SavestatePlayerLoadingPacketHandler implements IMessageHandler<SavestatePlayerLoadingPacket, IMessage>{

	@Override
	public IMessage onMessage(SavestatePlayerLoadingPacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				SavestateWorldLoading.loadWorldInfoFromFile();
				SavestatePlayerLoading.loadPlayersFromFile();
			});
		}else {
			Minecraft.getMinecraft().addScheduledTask(()->{
				Minecraft.getMinecraft().player.readFromNBT(message.getNbtTagCompound());
			});
		}
		return null;
	}

}
