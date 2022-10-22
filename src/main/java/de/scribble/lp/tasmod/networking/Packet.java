package de.scribble.lp.tasmod.networking;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * This is a Packet transmittable over the TASmod Custom Server.
 *
 * IMPLEMENTATION NOTICE:
 * There are no clientbound or serverbound packets as they were deemed unnecessary. This means both the server and the client can transmit any packet.
 * @author Pancake
 */
public interface Packet {

	/**
	 * Once a packet is being received it is immediately handled in this method.
	 *
	 * IMPLEMENTATION NOTICE:
	 * This process is non-blocking for the game and executed on the tasmod server thread temporarily blocking it.
	 */
	void handle(PacketSide side, EntityPlayer player);

	/**
	 * In order to transfer packets over the network connection they need to be serialized into a stream of bytes.
	 * @param buf Packet buffer to serialize to
	 * @return A serializable packet buffer
	 */
	void serialize(PacketBuffer buf);

	/**
	 * In order to receive packets over the network connection the other end serializes the packet into a stream of bytes. Therefore this end needs to deserialize the packet
	 * @param buf A deserializable packet buffer
	 */
	void deserialize(PacketBuffer buf);
	
}
