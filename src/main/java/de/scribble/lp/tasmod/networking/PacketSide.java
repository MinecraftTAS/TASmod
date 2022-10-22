package de.scribble.lp.tasmod.networking;

public enum PacketSide {
	CLIENT,
	SERVER;
	
	public boolean isClient() {
		return this == CLIENT;
	}
	
	public boolean isServer() {
		return this == SERVER;
	}
}
