package com.minecrafttas.mctcommon.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.minecrafttas.mctcommon.MCTCommon;
import com.minecrafttas.mctcommon.server.Client.Side;
import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;
import com.minecrafttas.mctcommon.server.interfaces.ClientPacketHandler;
import com.minecrafttas.mctcommon.server.interfaces.PacketHandlerBase;
import com.minecrafttas.mctcommon.server.interfaces.PacketID;
import com.minecrafttas.mctcommon.server.interfaces.ServerPacketHandler;

public class PacketHandlerRegistry {
	private static final List<PacketHandlerBase> REGISTRY = new ArrayList<>();

	public static void register(PacketHandlerBase handler) {
		if(handler==null) {
			throw new NullPointerException("Tried to register a handler with value null");
		}
		
		if(containsClass(handler)) {
			MCTCommon.LOGGER.warn("Trying to register packet handler {}, but another instance of this class is already registered!", handler.getClass().getName());
			return;
		}
		
		if (!REGISTRY.contains(handler)) {
			REGISTRY.add(handler);
		} else {
			MCTCommon.LOGGER.warn("Trying to register packet handler {}, but it is already registered!", handler.getClass().getName());
		}
	}

	public static void unregister(PacketHandlerBase handler) {
		if(handler==null) {
			throw new NullPointerException("Tried to unregister a handler with value null");
		}
		if (REGISTRY.contains(handler)) {
			REGISTRY.remove(handler);
		} else {
			MCTCommon.LOGGER.warn("Trying to unregister packet handler {}, but it was not registered!", handler.getClass().getName());
		}
	}

	public static void handle(Side side, PacketID packet, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		if (side != null && side == packet.getSide()) {
			packet.getLambda().onPacket(buf, username);
			return;
		}

		boolean isImplemented = false;
		for (PacketHandlerBase handler : REGISTRY) {
			if (Arrays.stream(handler.getAcceptedPacketIDs()).anyMatch(packet::equals)) {
				if (side == Side.CLIENT && handler instanceof ClientPacketHandler) {
					ClientPacketHandler clientHandler = (ClientPacketHandler) handler;
					clientHandler.onClientPacket(packet, buf, username);
					isImplemented = true;
				} else if (side == Side.SERVER && handler instanceof ServerPacketHandler) {
					ServerPacketHandler serverHandler = (ServerPacketHandler) handler;
					serverHandler.onServerPacket(packet, buf, username);
					isImplemented = true;
				}
			}
		}
		if(!isImplemented) {
			throw new PacketNotImplementedException(packet, side);
		}
	}
	
	private static boolean containsClass(PacketHandlerBase handler) {
		for(PacketHandlerBase packethandler : REGISTRY) {
			if(packethandler.getClass().equals(handler.getClass())) {
				return true;
			}
		}
		return false;
	}
}
