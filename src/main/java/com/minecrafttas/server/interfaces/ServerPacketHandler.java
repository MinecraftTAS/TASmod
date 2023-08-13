package com.minecrafttas.server.interfaces;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.server.exception.PacketNotImplementedException;
import com.minecrafttas.server.exception.WrongSideException;

public interface ServerPacketHandler extends PacketHandlerBase{
	
	public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception;
}
