package de.scribble.lp.tasmod.commands.clearinputs;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClearInputsPacket implements IMessage{

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

	public static class ClearInputsPacketHandler implements IMessageHandler<ClearInputsPacket, IMessage>{

		@Override
		public IMessage onMessage(ClearInputsPacket message, MessageContext ctx) {
			if(ctx.side.isServer()) {
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
					if(ctx.getServerHandler().player.canUseCommand(2, "clearinputs")) {
						CommonProxy.NETWORK.sendToAll(message);
					}
				});
			} else {
				Minecraft.getMinecraft().addScheduledTask(()->{
					ClientProxy.virtual.getContainer().clear();
				});
			}
			return null;
		}

	}
}
