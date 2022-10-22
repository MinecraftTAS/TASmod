package de.scribble.lp.tasmod.networking.packets;


import de.scribble.lp.tasmod.networking.Packet;
import de.scribble.lp.tasmod.networking.PacketSide;
import de.scribble.lp.tasmod.ticksync.TickSyncClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class ClientTickSyncPacket implements Packet {

	public ClientTickSyncPacket() {
	}
	
	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		TickSyncClient.onPacket();
	}

	@Override
	public void serialize(PacketBuffer buf) {
	}

	@Override
	public void deserialize(PacketBuffer buf) {
	}

}
