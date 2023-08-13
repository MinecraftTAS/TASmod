package com.minecrafttas.common.server.interfaces;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.Client.Side;

public interface PacketID {
	public int getID();
	
	public CompactPacketHandler getLambda();
	
	public Side getSide();
	
	public String getName();
}
