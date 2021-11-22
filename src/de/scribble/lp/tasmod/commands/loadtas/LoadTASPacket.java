package de.scribble.lp.tasmod.commands.loadtas;

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

public class LoadTASPacket implements IMessage{
	private String name;
	
	public LoadTASPacket() {
	}

	public LoadTASPacket(String name) {
		this.name=name;
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
	
	public static class LoadTASPacketHandler implements IMessageHandler<LoadTASPacket, IMessage> {

		@Override
		public IMessage onMessage(LoadTASPacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					if (ctx.getServerHandler().player.canUseCommand(2, "loadtas")) {
						CommonProxy.NETWORK.sendToAll(message);
					}
				});
			} else {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					try {
						ClientProxy.virtual.setContainer(ClientProxy.serialiser.fromEntireFileV1(new File(ClientProxy.tasdirectory + "/" + message.getName() + ".tas")));
						ClientProxy.virtual.getContainer().fixTicks();
					} catch (IOException e) {
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
						return;
					}
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Loaded inputs from " + message.getName() + ".tas"));
				});
			}
			return null;
		}

	}
}
