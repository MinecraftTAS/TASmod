package com.minecrafttas.server.interfaces;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface ServerPacketHandler extends PacketHandlerBase{
	
	public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID);
}
