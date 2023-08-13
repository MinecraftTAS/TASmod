package com.minecrafttas.server.exception;

import com.minecrafttas.server.Client;
import com.minecrafttas.server.interfaces.PacketID;

public class WrongSideException extends Exception {

	private static final long serialVersionUID = 1439028694540465537L;
	
	public WrongSideException(PacketID packet, Client.Side side) {
		super(String.format("The packet %s is sent to the wrong side: %s", packet.getName(), side.name()));
	}
	
	public WrongSideException(String msg) {
		super(msg);
	}
	
}
