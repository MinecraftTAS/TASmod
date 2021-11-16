package de.scribble.lp.tasmod.util.changestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncStatePacketHandler implements IMessageHandler<SyncStatePacket, IMessage> {

	@Override
	public IMessage onMessage(SyncStatePacket message, MessageContext ctx) {
		if (ctx.side.isServer()) {
			TASmod.containerStateServer.setState(message.getState());
			CommonProxy.NETWORK.sendToAll(message);
		} else {
			String chatMessage = ClientProxy.virtual.getContainer().setTASState(message.getState(), message.isVerbose());
			if (!chatMessage.isEmpty()) {
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
			}
		}
		return null;
	}

}
