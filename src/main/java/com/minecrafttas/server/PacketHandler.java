package com.minecrafttas.server;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface PacketHandler {
    void handle(Packet pid, ByteBuffer buf, UUID id);
}
