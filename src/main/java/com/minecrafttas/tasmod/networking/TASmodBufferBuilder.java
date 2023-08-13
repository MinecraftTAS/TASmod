package com.minecrafttas.tasmod.networking;

import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

public class TASmodBufferBuilder extends ByteBufferBuilder{

	public TASmodBufferBuilder(int id) {
		super(id);
	}
	
	public TASmodBufferBuilder(PacketID packet) {
		super(packet);
	}
	
	public TASmodBufferBuilder writeTASState(TASstate state) {
		this.writeShort((short)state.ordinal());
		return this;
	}
}
