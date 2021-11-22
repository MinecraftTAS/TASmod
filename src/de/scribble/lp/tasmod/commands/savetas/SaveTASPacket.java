package de.scribble.lp.tasmod.commands.savetas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SaveTASPacket implements IMessage {
	String name;

	public SaveTASPacket() {
	}

	public SaveTASPacket(String recordingName) {
		name = recordingName;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int length = buf.readInt();
		name = (String) buf.readCharSequence(length, Charset.defaultCharset());

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(name.getBytes().length);
		buf.writeCharSequence(name, Charset.defaultCharset());
	}

	public String getName() {
		return name;
	}

	public static class SaveTASPacketHandler implements IMessageHandler<SaveTASPacket, IMessage> {
		
		public SaveTASPacketHandler() {
		}
		
		@Override
		public IMessage onMessage(SaveTASPacket message, MessageContext ctx) {
			if (ctx.side.isClient()) {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					ClientProxy.createTASDir();
					try {
						ClientProxy.serialiser.saveToFileV1(new File(ClientProxy.tasdirectory + "/" + message.getName() + ".tas"), ClientProxy.virtual.getContainer());
					} catch (IOException e) {
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
						return;
					}
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Saved inputs to " + message.getName() + ".tas"));
				});
			} else {
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					if (ctx.getServerHandler().player.canUseCommand(2, "savetas")) {
						CommonProxy.NETWORK.sendToAll(message);
					}
				});
			}
			return null;
		}

	}
}
