package com.minecrafttas.tasmod.networking;

import java.util.UUID;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Used to identify the socket. This enables the server to send packets to a specific player
 * @author Scribble
 */
public class IdentificationPacket implements Packet {

	private UUID uuid;

	public IdentificationPacket() {

	}

	public IdentificationPacket(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if (side.isServer()) {
			TickSyncServer.onPacket(this.uuid);
		} else {
			TASmodClient.packetClient.setReady();
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		if (uuid != null) {
			buf.writeUniqueId(uuid);
		}
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		if (buf.capacity() > 0) {
			this.uuid = buf.readUniqueId();
		}
	}

	public UUID getUuid() {
		return uuid;
	}
}
