package de.scribble.lp.tasmod.networking.packets;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.Packet;
import net.minecraft.network.PacketBuffer;

public class ClientKTRNGPacket implements Packet{

	private long seed;
	
	public ClientKTRNGPacket() {
	}
	
	public ClientKTRNGPacket(long seed) {
		this.seed = seed;
	}

	@Override
	public void handle() {
		TASmod.ktrngHandler.setGlobalSeedClient(seed);
	}

	@Override
	public PacketBuffer serialize(PacketBuffer buf) {
		buf.writeLong(seed);
		return buf;
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		seed = buf.readLong();
	}

}
