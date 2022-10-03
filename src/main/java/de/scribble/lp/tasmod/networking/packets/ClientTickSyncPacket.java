package de.scribble.lp.tasmod.networking.packets;


import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.ticksync.TickSyncClient;
import net.minecraft.network.PacketBuffer;

public class ClientTickSyncPacket implements Packet {

	public ClientTickSyncPacket() {
	}
	
	@Override
	public void handle() {
		TickSyncClient.onPacket();
	}

	@Override
	public PacketBuffer serialize(PacketBuffer buf) {
		return buf;
	}

	@Override
	public void deserialize(PacketBuffer buf) {
	}

}
