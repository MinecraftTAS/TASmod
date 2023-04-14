package com.minecrafttas.tasmod.ktrng;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

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
		if(side.isClient()) {
			if(TASmod.ktrngHandler.isUpdating())
				TASmod.ktrngHandler.setGlobalSeedClient(seed);
		}else {
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
