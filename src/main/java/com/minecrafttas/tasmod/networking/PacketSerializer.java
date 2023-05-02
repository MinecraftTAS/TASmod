package com.minecrafttas.tasmod.networking;

import java.util.ArrayList;

import com.minecrafttas.tasmod.TASmod;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

/**
 * This class helps serializing and deserializing packets
 * @author Pancake
 */
public class PacketSerializer {
	
	private static ArrayList<Class<? extends Packet>> REGISTRY = new ArrayList<>();

	/**
	 * Deserialize a TASmod packet from a packet buffer. The packet class is prefixed with an id and read here.
	 *
	 * @param buf Serialized byte buffer with id prefix
	 * @return Deserialized packet
	 */
	public static Packet deserialize(PacketBuffer buf) {
		// Read packet id and deserialize the correct packet
		int packetId = buf.readInt();
		
		Packet packet=null;
		try {
			packet = REGISTRY.get(packetId).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if(packet == null) {
			TASmod.logger.warn("Unregistered packet received! Packet Id: " + packetId);
			return null;
		}
		
		packet.deserialize(buf);
		return packet;
	}

	/**
	 * Serialize a TASmod packet to a packet buffer. The packet class is read and a id prefixed packet buffer is returned
	 *
	 * @param packet Non-serialized packet
	 * @return Serialized packet buffer with id prefix
	 */
	public static PacketBuffer serialize(Packet packet) {
		// Figure out packet class and prefix the correct id
		
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		int packetID = REGISTRY.indexOf(packet.getClass());
		
		if(packetID == -1) {
			TASmod.logger.warn("Unregistered packet was trying to be serialized! Packet Class: " + packet.getClass().getSimpleName());
			return null;
		}
		
		buf.writeInt(packetID);
		
		packet.serialize(buf);
		return buf;
	}
	
	public static void registerPacket(Class<? extends Packet> packet) {
		if(REGISTRY.contains(packet)) {
			TASmod.logger.warn("Trying to register packet which already exists: "+packet.getClass().getSimpleName());
		}
		REGISTRY.add(packet);
	}

	public static void unregisterPacket(Class<? extends Packet> packet) {
		if(REGISTRY.contains(packet)) {
			TASmod.logger.warn("Trying to unregister packet which doesn't exist in the registry: "+packet.getClass().getSimpleName());
		}
		REGISTRY.remove(packet);
	}
	
}
