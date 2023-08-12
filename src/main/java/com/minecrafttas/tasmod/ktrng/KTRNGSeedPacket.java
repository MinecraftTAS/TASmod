package com.minecrafttas.tasmod.ktrng;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class KTRNGSeedPacket implements PacketID {

	private long seed;

	public KTRNGSeedPacket() {
	}

	public KTRNGSeedPacket(long seed) {
		this.seed = seed;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if (side.isClient()) {
			TASmod.ktrngHandler.setGlobalSeedClient(seed);
		} else {
			TASmod.ktrngHandler.setGlobalSeedServer(seed);
		}
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
