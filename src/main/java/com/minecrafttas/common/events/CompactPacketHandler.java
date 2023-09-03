package com.minecrafttas.common.events;

import java.nio.ByteBuffer;

import com.minecrafttas.common.server.exception.PacketNotImplementedException;

@FunctionalInterface
public interface CompactPacketHandler {
	
	public void onPacket(ByteBuffer buf, String username) throws PacketNotImplementedException;
}
