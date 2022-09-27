package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TickSyncKTRNGPacket extends TickSyncPacket {
	
	private long seed;
	
	public TickSyncKTRNGPacket() {
	}
	
	public TickSyncKTRNGPacket(int ticks, boolean reset, long seed) {
		super(ticks, reset);
		this.seed = seed;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.ticks = buf.readInt();
		this.shouldreset = buf.readBoolean();
		this.seed = buf.readLong();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ticks);
		buf.writeBoolean(shouldreset);
		buf.writeLong(seed);
	}
	
	public static class TickSyncKTRNGPacketHandler implements IMessageHandler<TickSyncKTRNGPacket, IMessage>{

		@Override
		public IMessage onMessage(TickSyncKTRNGPacket message, MessageContext ctx) {
			if(ctx.side.isClient()) {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					if (message.shouldreset) {
						ClientProxy.ticksyncClient.resetTickCounter();
					} else {
						ClientProxy.ticksyncClient.setServerTickcounter(message.ticks);
					}

					if(TASmod.ktrngHandler.isLoaded()) {
						TASmod.ktrngHandler.addToQueue(message.seed);
					}
				});
			}
			return null;
		}
	}
}
