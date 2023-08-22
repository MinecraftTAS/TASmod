package com.minecrafttas.common.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import com.minecrafttas.common.Common;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketHandlerBase;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;

public class PacketHandlerRegistry {
	private static final List<PacketHandlerBase> REGISTRY = new ArrayList<>();

	public static void register(PacketHandlerBase handler) {
		if (!REGISTRY.contains(handler)) {
			REGISTRY.add(handler);
		} else {
			Common.LOGGER.warn("Trying to register packet handler {}, but it is already registered!", handler.getClass().getName());
		}
	}

	public static void unregister(PacketHandlerBase handler) {
		if (REGISTRY.contains(handler)) {
			REGISTRY.remove(handler);
		} else {
			Common.LOGGER.warn("Trying to unregister packet handler {}, but is was not registered!", handler.getClass().getName());
		}
	}

	public static void handle(Side side, PacketID packet, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		if (side != null && side == packet.getSide()) {
			packet.getLambda().onPacket(buf, clientID);
			return;
		}

		boolean isImplemented = false;
		for (PacketHandlerBase handler : REGISTRY) {
			if (ArrayUtils.contains(handler.getAcceptedPacketIDs(), packet)) { 		// TODO Remove the third party library
				if (side == Side.CLIENT && handler instanceof ClientPacketHandler) {
					ClientPacketHandler clientHandler = (ClientPacketHandler) handler;
					clientHandler.onClientPacket(packet, buf, clientID);
					isImplemented = true;
				} else if (side == Side.SERVER && handler instanceof ServerPacketHandler) {
					ServerPacketHandler serverHandler = (ServerPacketHandler) handler;
					serverHandler.onServerPacket(packet, buf, clientID);
					isImplemented = true;
				}
			}
		}
		if(!isImplemented) {
			throw new PacketNotImplementedException(packet);
		}
	}
}
