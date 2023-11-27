package com.minecrafttas.mctcommon.events;

import java.nio.ByteBuffer;

import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;

@FunctionalInterface
public interface CompactPacketHandler {
	
	public void onPacket(ByteBuffer buf, String username) throws PacketNotImplementedException;
}
