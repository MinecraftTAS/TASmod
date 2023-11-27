package com.minecrafttas.mctcommon.server.interfaces;

import java.nio.ByteBuffer;

import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;

public interface ServerPacketHandler extends PacketHandlerBase{
	
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception;
}
