package com.minecrafttas.common.server.exception;

import com.minecrafttas.common.server.interfaces.PacketHandlerBase;
import com.minecrafttas.common.server.interfaces.PacketID;

public class PacketNotImplementedException extends Exception {

	private static final long serialVersionUID = -8089503724361521594L;
	
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
