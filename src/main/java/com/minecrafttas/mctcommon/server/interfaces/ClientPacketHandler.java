package com.minecrafttas.mctcommon.server.interfaces;

import java.nio.ByteBuffer;

import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;

public interface ClientPacketHandler extends PacketHandlerBase{
	
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception;
}
