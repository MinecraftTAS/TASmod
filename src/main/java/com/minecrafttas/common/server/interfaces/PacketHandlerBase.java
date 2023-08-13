package com.minecrafttas.common.server.interfaces;

public interface PacketHandlerBase {
	/**
	 * Declares all packet types that get routed into the {@link ClientPacketHandler#onClientPacket(PacketID, java.nio.ByteBuffer, java.util.UUID)} <br>
	 * <br>
	 * or {@link ServerPacketHandler#onServerPacket(PacketID, java.nio.ByteBuffer, java.util.UUID)} methods.
	 */
    public PacketID[] getAcceptedPacketIDs();
    
}
