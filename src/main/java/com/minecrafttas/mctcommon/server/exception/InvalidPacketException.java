package com.minecrafttas.mctcommon.server.exception;

@SuppressWarnings("serial")
public class InvalidPacketException extends Exception {

	public InvalidPacketException() {
		super();
	}
	
	public InvalidPacketException(String msg) {
		super(msg);
	}
}
