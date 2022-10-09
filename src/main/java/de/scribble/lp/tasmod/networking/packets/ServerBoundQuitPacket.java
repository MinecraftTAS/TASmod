package de.scribble.lp.tasmod.networking.packets;

import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.network.PacketBuffer;

public class ServerBoundQuitPacket implements Packet {

	@Override
	public void handle() {
		TickSyncServer.clearList();
	}

	@Override
	public PacketBuffer serialize(PacketBuffer buf) {
		return buf;
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		// TODO Auto-generated method stub

	}

}
