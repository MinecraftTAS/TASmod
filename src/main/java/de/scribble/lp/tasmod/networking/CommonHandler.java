package de.scribble.lp.tasmod.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import de.scribble.lp.tasmod.TASmod;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

/**
 * This handler processes input packets and sends output packets to a given socket. It's common code meaning it works on both the server and client side.
 * @author Pancake
 */
public class CommonHandler {

	/**
	 * Handles a socket by processing input packets and sending output packets
	 * @param socket Socket to handle
	 * @param packetsToSend Queue of packets to send
	 * @throws Exception Unexpected issues
	 */
	public static void handleSocket(Socket socket, BlockingQueue<Packet> packetsToSend) throws Exception {
		socket.setTcpNoDelay(true);
		// Prepare the in and out streams.
		DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		// Create a new thread that writes packets if available
		Thread outputThread = new Thread(() -> {
			try {
				Packet packet;
				while (true) {
					// Try to poll another packet that wants to be sent
					packet = packetsToSend.poll();
					if (packet == null) {
						Thread.sleep(1); // If nothing has to be done, let the cpu rest by waiting
						continue;
					}
					// A packet was found: Serialize then send it.
					byte[] packetData = PacketSerializer.serialize(packet).array();
					outputStream.writeInt(packetData.length);
					outputStream.write(packetData);
					outputStream.flush();
					TASmod.logger.trace("Sent a " + packet.getClass().getSimpleName() + " to the socket.");
				}
			} catch (Exception exception) {
				// This exception is already logged by the thread one layer above
				// therefore nothing needs to be done here.
			}
		});
		outputThread.setDaemon(true); // If daemon is set, the jvm will quit without waiting for this thread to finish
		outputThread.start();
		// Use the current thread to indefinitly fetch packets
		Packet packet;
		while (socket.isConnected()) {
			// Handle the next packet. If no packet is avilable, the readInt() call will hang until there is one.
			int packetSize = inputStream.readInt();
			byte[] packetData = new byte[packetSize];
			inputStream.read(packetData, 0, packetSize);
			PacketBuffer packetBuf = new PacketBuffer(Unpooled.wrappedBuffer(packetData));
			// Deserialize and run the packet
			packet = PacketSerializer.deserialize(packetBuf);
			packet.handle();
			TASmod.logger.trace("Handled a " + packet.getClass().getSimpleName() + " from the socket.");
		}
	}

}
