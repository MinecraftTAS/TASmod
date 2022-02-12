package de.scribble.lp.tasmod.commands.restartandplay;

import java.nio.charset.Charset;

import de.scribble.lp.tasmod.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RestartAndPlayPacket implements IMessage{
	private String name;
	
	public RestartAndPlayPacket() {
	}

	public RestartAndPlayPacket(String name) {
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

	public static class RestartAndPlayPacketHandler implements IMessageHandler<RestartAndPlayPacket, IMessage>{

		@Override
		public IMessage onMessage(RestartAndPlayPacket message, MessageContext ctx) {
			if(ctx.side.isClient()) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Minecraft.getMinecraft().addScheduledTask(() -> {
					ClientProxy.config.get("General", "fileToLoad", "").set(message.name);
					ClientProxy.config.save();
					FMLCommonHandler.instance().exitJava(0, false);
				});
			}
			return null;
		}
		
	}
}
