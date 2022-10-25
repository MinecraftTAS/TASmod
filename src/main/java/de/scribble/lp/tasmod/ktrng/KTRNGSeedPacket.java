package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.networking.PacketSide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class KTRNGSeedPacket implements Packet{

	private long seed;
	
	public KTRNGSeedPacket() {
	}
	
	public KTRNGSeedPacket(long seed) {
		this.seed = seed;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		TASmod.ktrngHandler.setGlobalSeedClient(seed);
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
