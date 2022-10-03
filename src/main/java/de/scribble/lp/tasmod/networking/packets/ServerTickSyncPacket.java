package de.scribble.lp.tasmod.networking.packets;

import java.util.UUID;

import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.network.PacketBuffer;

public class ServerTickSyncPacket implements Packet {

	protected UUID uuid;
	
	public ServerTickSyncPacket() {
		
	}
	
	public ServerTickSyncPacket(UUID id) {
		this.uuid = id;
	}

	@Override
	public void handle() {
		TickSyncServer.onPacket(this.uuid);
	}

	@Override
	public PacketBuffer serialize(PacketBuffer buf) {
		buf.writeUniqueId(uuid);
		return buf;
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		uuid = buf.readUniqueId();
	}

}
