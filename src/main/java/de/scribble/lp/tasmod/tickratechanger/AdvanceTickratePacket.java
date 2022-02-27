package de.scribble.lp.tasmod.tickratechanger;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Advanced game by 1 tick
 * 
 * @author ScribbleLP
 *
 */
public class AdvanceTickratePacket implements IMessage {
	/**
	 * Advanced game by 1 tick
	 */
	public AdvanceTickratePacket() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}

	public static class AdvanceTickratePacketHandler implements IMessageHandler<AdvanceTickratePacket, IMessage> {

		@Override
		public IMessage onMessage(AdvanceTickratePacket message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				if (ctx.getServerHandler().player.canUseCommand(2, "tickrate")) {
					if (TickrateChangerServer.ticksPerSecond == 0) {
						TickrateChangerServer.advanceTick();
					}
				}
			} else {
				TickrateChangerClient.advanceClientTick(); // Using advanceTick() would create an endless loop
			}
			return null;
		}

	}
}
