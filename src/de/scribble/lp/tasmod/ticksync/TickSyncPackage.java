package de.scribble.lp.tasmod.ticksync;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TickSyncPackage implements IMessage {
	private int ticks;
	private boolean shouldreset;
	private boolean shouldstop;

	public TickSyncPackage() {
		ticks = 0;
		shouldreset = false;
		shouldstop = false;
	}

	public TickSyncPackage(int ticks, boolean reset, boolean stop) {
		this.ticks = ticks;
		this.shouldreset = reset;
		this.shouldstop = stop;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.ticks = buf.readInt();
		this.shouldreset = buf.readBoolean();
		this.shouldstop = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ticks);
		buf.writeBoolean(shouldreset);
		buf.writeBoolean(shouldstop);
	}

	public int getTicks() {
		return ticks;
	}

	public boolean isShouldreset() {
		return shouldreset;
	}

	public boolean isShouldstop() {
		return shouldstop;
	}
	
	public static class TickSyncPacketHandler implements IMessageHandler<TickSyncPackage, IMessage> {

		@Override
		public IMessage onMessage(TickSyncPackage message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					if (message.isShouldreset()) {
						TickSync.resetTickCounter();
					} else {
						TickSync.setServerTickcounter(message.getTicks());
					}

					if (TickSync.isEnabled() != message.isShouldstop()) {
						TickSync.sync(message.isShouldstop());
					}
				});
			}
			return null;
		}
	}
}
