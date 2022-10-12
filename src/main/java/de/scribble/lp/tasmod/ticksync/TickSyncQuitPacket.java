package de.scribble.lp.tasmod.ticksync;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TickSyncQuitPacket implements IMessage {

	public TickSyncQuitPacket() {
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}
	
	
	public static class TickSyncQuitPacketHandler implements IMessageHandler<TickSyncQuitPacket, IMessage> {

		@Override
		public IMessage onMessage(TickSyncQuitPacket message, MessageContext ctx) {
			if(ctx.side.isServer()) {
				TickSyncServer.shouldTick.set(true);
			}
			return null;
		}
		
	}
}
