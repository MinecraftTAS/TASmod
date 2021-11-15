package de.scribble.lp.tasmod.savestates.server;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import de.scribble.lp.tasmod.savestates.server.exceptions.LoadstateException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LoadstatePacketHandler implements IMessageHandler<LoadstatePacket, IMessage>{

	@Override
	public IMessage onMessage(LoadstatePacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				EntityPlayerMP player=ctx.getServerHandler().player;
				if(!player.canUseCommand(2, "tickrate")) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"You don't have permission to do that"));
					return;
				}
				try {
					TASmod.savestateHandler.loadState(message.index, true);
				} catch (LoadstateException e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getMessage()));
				} catch (Exception e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getCause().toString()));
					e.printStackTrace();
				} finally {
					TASmod.savestateHandler.state=SavestateState.NONE;
				}
			});
		}else {
			Minecraft.getMinecraft().addScheduledTask(()->{
				SavestatesChunkControl.unloadAllClientChunks();
			});
		}
		return null;
	}

}
