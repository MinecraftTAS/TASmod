package com.minecrafttas.tasmod.mctcommon.networking.interfaces;

import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.CompactPacketHandler;
import com.minecrafttas.tasmod.mctcommon.registry.Registerable;

public interface PacketID extends Registerable {
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
