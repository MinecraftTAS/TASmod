package com.minecrafttas.tasmod.tickratechanger;

import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Advanced game by 1 tick
 * 
 * @author Scribble
 *
 */
public class AdvanceTickratePacket implements Packet {
	/**
	 * Advanced game by 1 tick
	 */
	public AdvanceTickratePacket() {
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if (side.isServer()) {
			if (player.canUseCommand(2, "tickrate")) {
				if (TickrateChangerServer.ticksPerSecond == 0) {
					TickrateChangerServer.advanceTick();
				}
			}
		} else {
			TickrateChangerClient.advanceClientTick(); // Using advanceTick() would create an endless loop
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		
	}


}
