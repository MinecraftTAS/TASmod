package com.minecrafttas.tasmod.tickratechanger;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
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
				if (TASmod.tickratechanger.ticksPerSecond == 0) {
					TASmod.tickratechanger.advanceTick();
				}
			}
		} else {
			TASmodClient.tickratechanger.advanceClientTick(); // Using advanceTick() would create an endless loop
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		
	}


}
