package com.minecrafttas.tasmod.commands.folder;

import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class FolderPacket implements Packet {
	int command;
	
	public FolderPacket() {
	}
	
	/**
	 * 0: Open savestates folder
	 * 1: Open TASdir folder
	 * @param command The folder to open
	 */
	public FolderPacket(int command) {
		this.command=command;
	}
	
	
	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			switch(command) {
			case 0:
				OpenStuff.openSavestates();
				break;
			case 1:
				OpenStuff.openTASFolder();
				break;
			}
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(command);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		command=buf.readInt();
	}

}
