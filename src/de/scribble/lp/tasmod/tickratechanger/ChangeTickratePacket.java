package de.scribble.lp.tasmod.tickratechanger;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Changes the tickrate on the other side
 * 
 * @author ScribbleLP
 *
 */
public class ChangeTickratePacket implements IMessage {

	float tickrate;

	public ChangeTickratePacket() {
	}
	
	/**
	 * Changes the tickrate on the other side
	 * 
	 * @param tickrate The new tickrate
	 */
	public ChangeTickratePacket(float tickrate) {
		this.tickrate = tickrate;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tickrate = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(tickrate);
	}

	public static class ChangeTickratePacketHandler implements IMessageHandler<ChangeTickratePacket, IMessage> {

		public ChangeTickratePacketHandler() {
		}

		@Override
		public IMessage onMessage(ChangeTickratePacket message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayerMP player = ctx.getServerHandler().player;
				if (player.canUseCommand(2, "tickrate")) {
					TickrateChangerServer.changeTickrate(message.tickrate);
				}
			} else if (ctx.side == Side.CLIENT) {
				TickrateChangerClient.changeClientTickrate(message.tickrate);
			}
			return null;
		}
	}

}
