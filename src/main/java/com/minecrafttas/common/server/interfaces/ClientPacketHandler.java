package com.minecrafttas.common.server.interfaces;

import java.nio.ByteBuffer;

import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;

public interface ClientPacketHandler extends PacketHandlerBase{
	
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception;
}
