package de.scribble.lp.tasmod.savetas;

import java.io.File;
import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SaveTASPacketHandler implements IMessageHandler<SaveTASPacket, IMessage>{

	@Override
	public IMessage onMessage(SaveTASPacket message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			Minecraft.getMinecraft().addScheduledTask(()->{
				try {
					ClientProxy.serialiser.saveToFileV1(new File(ClientProxy.tasdirectory + "/"+message.getName()+".tas"), ClientProxy.virtual.getContainer());
				} catch (IOException e) {
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED+e.getMessage()));
					return;
				}
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN+"Saved inputs to "+message.getName()+".tas"));
			});
		}else {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				if(ctx.getServerHandler().player.canUseCommand(2, "savetas")) {
					CommonProxy.NETWORK.sendToAll(message);
				}
			});
		}
		return null;
	}

}
