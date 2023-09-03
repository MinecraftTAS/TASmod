package com.minecrafttas.common.server.interfaces;

import java.nio.ByteBuffer;

import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;

public interface ServerPacketHandler extends PacketHandlerBase{
	
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception;
}
