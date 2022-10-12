package de.scribble.lp.tasmod.ticksync;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TickSyncPacket implements IMessage{
	
	public TickSyncPacket() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}
	
	public static class TickSyncPacketHandler implements IMessageHandler<TickSyncPacket, IMessage> {

		@Override
		public IMessage onMessage(TickSyncPacket message, MessageContext ctx) {
			if(ctx.side.isServer()) {
				TickSyncServer.onPacket(ctx.getServerHandler().player.getUniqueID());
			} else {
				TickSyncClient.onPacket();
			}
			return null;
		}
		
	}
}
