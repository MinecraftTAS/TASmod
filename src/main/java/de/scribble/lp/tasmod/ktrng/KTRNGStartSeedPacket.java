package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.tasmod.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KTRNGStartSeedPacket implements IMessage{

	private long seed;
	
	/**
	 * Only used by forge, do not use!
	 */
	@Deprecated
	public KTRNGStartSeedPacket() {
	}
	
	/**
	 * Set's the start seed of the client
	 * @param seed
	 */
	public KTRNGStartSeedPacket(long seed) {
		this.seed = seed;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		seed = buf.readLong();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(seed);
	}

	public static class KTRNGStartSeedPacketHandler implements IMessageHandler<KTRNGStartSeedPacket, IMessage> {

		@Override
		public IMessage onMessage(KTRNGStartSeedPacket message, MessageContext ctx) {
			if(ctx.side.isClient()){
				ClientProxy.virtual.getContainer().setStartSeed(message.seed);
			}
			return null;
		}
	}
}
