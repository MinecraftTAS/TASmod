package com.minecrafttas.common.server.exception;

public class InvalidPacketException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5253939281926562204L;

	public InvalidPacketException() {
		super();
	}
	
	public InvalidPacketException(String msg) {
		super(msg);
	}
}
