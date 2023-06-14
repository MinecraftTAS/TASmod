package com.minecrafttas.tasmod.commands.playuntil;

import com.minecrafttas.tasmod.TASmodClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class PlayUntilPacket implements Packet {

	private int until;

	public PlayUntilPacket() {
	}

	public PlayUntilPacket(int until) {
		this.until = until;
	}
	
	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			TASmodClient.virtual.getContainer().setPlayUntil(until);
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(until);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		until = buf.readInt();
	}

}
