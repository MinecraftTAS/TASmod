package de.scribble.lp.tasmod.networking.packets;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.networking.PacketSide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class ClientKTRNGPacket implements Packet{

	private long seed;
	
	public ClientKTRNGPacket() {
	}
	
	public ClientKTRNGPacket(long seed) {
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