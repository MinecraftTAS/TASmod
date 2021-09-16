package de.scribble.lp.tasmod.commands.recording;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RecordingPacketHandler implements IMessageHandler<RecordingPacket, IMessage> {

	@Override
	public IMessage onMessage(RecordingPacket message, MessageContext ctx) {
		if (ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
				if (ctx.getServerHandler().player.canUseCommand(2, "record")) {
					CommonProxy.NETWORK.sendToAll(message);
				}
			});
		} else {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				String chatMessage = ClientProxy.virtual.getContainer().setTASState(message.enabled? TASstate.RECORDING: TASstate.NONE);
//				String chatMessage = ClientProxy.virtual.getContainer().setRecording(message.isEnabled());
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
			});
		}
		return null;
	}

}
