package com.minecrafttas.server.interfaces;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.server.exception.PacketNotImplementedException;
import com.minecrafttas.server.exception.WrongSideException;

public interface ClientPacketHandler extends PacketHandlerBase{
	
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception;
}
