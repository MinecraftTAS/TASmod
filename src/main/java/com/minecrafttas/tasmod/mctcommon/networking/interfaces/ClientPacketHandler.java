package com.minecrafttas.tasmod.mctcommon.networking.interfaces;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;

public interface ClientPacketHandler extends PacketHandlerBase {

	/**
	 * Called when a packet reaches the client
	 * @param id The packet id.
	 * @param buf The buffer with data from the server side
	 * @param username The username of the current player
	 * @throws PacketNotImplementedException If a packet is not implemented
	 * @throws WrongSideException If the packet is sent to the wrong client
	 * @throws Exception Anything else
	 */
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception;
}
