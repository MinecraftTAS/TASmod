package com.minecrafttas.server.interfaces;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.server.Client.Side;

public interface PacketID {
	public int getID();
	
	public CompactPacketHandler getLambda();
	
	public Side getSide();
	
	public String getName();
}
