package com.minecrafttas.tasmod.tickratechanger;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Changes the tickrate on the other side
 * 
 * @author Scribble
 *
 */
public class ChangeTickratePacket implements Packet {

	float tickrate;

	public ChangeTickratePacket() {
	}
	
	/**
	 * Changes the tickrate on the other side
	 * 
	 * @param tickrate The new tickrate
	 */
	public ChangeTickratePacket(float tickrate) {
		this.tickrate = tickrate;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if (side.isServer()) {
			if (player.canUseCommand(2, "tickrate")) {
				TASmod.tickratechanger.changeTickrate(tickrate);
			}
		} else {
			TASmodClient.tickratechanger.changeClientTickrate(tickrate);
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeFloat(tickrate);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		tickrate = buf.readFloat();
	}

}
