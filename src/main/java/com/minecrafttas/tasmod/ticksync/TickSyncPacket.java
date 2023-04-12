package com.minecrafttas.tasmod.ticksync;

import java.util.UUID;

import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Packet for {@linkplain TickSyncServer}
 * @author Scribble
 *
 */
public class TickSyncPacket implements Packet {

	protected UUID uuid;
	
	public TickSyncPacket() {
		
	}
	
	public TickSyncPacket(UUID id) {
		this.uuid = id;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isServer()) {
			TickSyncServer.onPacket(this.uuid);
		}else {
			TickSyncClient.onPacket();
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		if(uuid!=null)
			buf.writeUniqueId(uuid);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		uuid = buf.readUniqueId();
	}

}
