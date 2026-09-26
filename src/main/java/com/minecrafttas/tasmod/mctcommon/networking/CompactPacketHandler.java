package com.minecrafttas.tasmod.mctcommon.networking;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;

@FunctionalInterface
public interface CompactPacketHandler {

	public void onPacket(ByteBuffer buf, String username) throws PacketNotImplementedException;
}
