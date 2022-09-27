package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TickSyncPacket implements IMessage {
	
	protected int ticks;
	protected boolean shouldreset;

	public TickSyncPacket() {
		ticks = 0;
		shouldreset = false;
	}

	public TickSyncPacket(int ticks, boolean reset) {
		this.ticks = ticks;
		this.shouldreset = reset;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.ticks = buf.readInt();
		this.shouldreset = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ticks);
		buf.writeBoolean(shouldreset);
	}

	
	public static class TickSyncPacketHandler implements IMessageHandler<TickSyncPacket, IMessage> {

		@Override
		public IMessage onMessage(TickSyncPacket message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					if (message.shouldreset) {
						ClientProxy.ticksyncClient.resetTickCounter();
					} else {
						ClientProxy.ticksyncClient.setServerTickcounter(message.ticks);
					}
				});
			}
			return null;
		}
	}
}
