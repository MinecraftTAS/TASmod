package com.minecrafttas.server.interfaces;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface ClientPacketHandler extends PacketHandlerBase{
	
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID);
}
