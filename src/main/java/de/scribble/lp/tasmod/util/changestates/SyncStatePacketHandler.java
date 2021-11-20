package de.scribble.lp.tasmod.util.changestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
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
		} else {
			InputContainer container = ClientProxy.virtual.getContainer();
			if (message.getState() != container.getState()) {
				String chatMessage = container.setTASState(message.getState(), message.isVerbose());
				if (!chatMessage.isEmpty()) {
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
				}
			}
		}
		return null;
	}

}
