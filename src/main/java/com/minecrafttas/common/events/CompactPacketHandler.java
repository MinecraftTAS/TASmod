package com.minecrafttas.common.events;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.server.exception.PacketNotImplementedException;

@FunctionalInterface
public interface CompactPacketHandler {
	
	public void onPacket(ByteBuffer buf, UUID clientID) throws PacketNotImplementedException;
}
