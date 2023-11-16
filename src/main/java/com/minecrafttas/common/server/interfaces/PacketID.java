package com.minecrafttas.common.server.interfaces;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.Client.Side;

public interface PacketID {
	/**
	 * @return The numerical ID of the packet
	 */
	public int getID();
	/**
	 * Only used in combination with {@link #getLambda()}
	 * @return The side of the packet this is registered to
	 */
	public Side getSide();
	/**
	 * Used for compact small lambda packet handlers
	 * @return The lamda to run when receiving a packet
	 */
	public CompactPacketHandler getLambda();
	/**
	 * @return  The name of the packet
	 */
	public String getName();
	
	/**
	 * @return Whether the packet should be used in trace messages
	 */
	public boolean shouldTrace();
}
