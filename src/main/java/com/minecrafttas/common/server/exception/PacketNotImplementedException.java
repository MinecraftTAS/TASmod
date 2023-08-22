package com.minecrafttas.common.server.exception;

import com.minecrafttas.common.server.interfaces.PacketHandlerBase;
import com.minecrafttas.common.server.interfaces.PacketID;

@SuppressWarnings("serial")
public class PacketNotImplementedException extends Exception {

	public PacketNotImplementedException(String msg) {
		super(msg);
	}
	
	public PacketNotImplementedException(PacketID packet, Class<? extends PacketHandlerBase> clazz) {
		super(String.format("The packet %s is not implemented in %s", packet.getName(), clazz.getCanonicalName()));
	}
	
	public PacketNotImplementedException(PacketID packet) {
		super(String.format("The packet %s is not implemented or not registered in getAssociatedPacketIDs", packet.getName()));
	}
	
}
