package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.networking.PacketSide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class KTRNGStartSeedPacket implements Packet{

	private long seed;
	
	/**
	 * Do not use!
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
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			ClientProxy.virtual.getContainer().setStartSeed(seed);
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeLong(seed);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		seed = buf.readLong();
	}
}
