package com.minecrafttas.tasmod.ktrng;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class KTRNGStartSeedPacket implements Packet{

	private long seed;
	
	/**
	 * Do not use!
	 */
	@Deprecated
	public KTRNGStartSeedPacket() {
	}
	
	/**
	 * Set's the start seed of the client
	 * @param seed
	 */
	public KTRNGStartSeedPacket(long seed) {
		this.seed = seed;
	}
	
	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			TASmodClient.virtual.getContainer().setStartSeed(seed);
		} else {
			TASmod.tickSchedulerServer.add(()->{
				TASmod.ktrngHandler.setGlobalSeedServer(seed);
			});
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
