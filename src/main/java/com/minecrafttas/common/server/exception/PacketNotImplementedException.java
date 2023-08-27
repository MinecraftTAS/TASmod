package com.minecrafttas.common.server.exception;

import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.interfaces.PacketHandlerBase;
import com.minecrafttas.common.server.interfaces.PacketID;

@SuppressWarnings("serial")
public class PacketNotImplementedException extends Exception {

	public PacketNotImplementedException(String msg) {
		super(msg);
	}
	
	public PacketNotImplementedException(PacketID packet, Class<? extends PacketHandlerBase> clazz, Side side) {
		super(String.format("The packet %s is not implemented in %s on the %s-Side", packet.getName(), clazz.getCanonicalName(), side));
	}
	
	public PacketNotImplementedException(PacketID packet, Side side) {
		super(String.format("The packet %s is not implemented or not registered in getAssociatedPacketIDs on the %s-Side", packet.getName(), side));
	}
	
}
