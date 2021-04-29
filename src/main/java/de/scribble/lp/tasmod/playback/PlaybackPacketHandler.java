package de.scribble.lp.tasmod.playback;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlaybackPacketHandler implements IMessageHandler<PlaybackPacket, IMessage> {

	@Override
	public IMessage onMessage(PlaybackPacket message, MessageContext ctx) {
		if (ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
				if (ctx.getServerHandler().player.canUseCommand(2, "play")) {
					CommonProxy.NETWORK.sendToAll(message);
				}
			});
		} else {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				String chatMessage = ClientProxy.virtual.getContainer().setPlayback(message.isEnabled());
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
			});
		}
		return null;
	}
}
