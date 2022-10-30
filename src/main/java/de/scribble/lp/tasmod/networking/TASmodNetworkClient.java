package de.scribble.lp.tasmod.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.networking.packets.IdentificationPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

public class TASmodNetworkClient {
	
	private Logger logger;
	
	private Thread clientThread;
	
	private Socket clientSocket;
	
	private BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();
	
	public TASmodNetworkClient(Logger logger) {
		this(logger, "127.0.0.1", 3111); // Set ip for different server
	}
	
	public TASmodNetworkClient(Logger logger, String serverIP, int port) {
		this.logger = logger;
		this.logger.info("Trying to connect to {}:{}", serverIP, port);
		createClient(serverIP, port);
	}
	
	public void sendToServer(Packet packet) {
		if(clientThread == null) 
			return;
		if(!clientThread.isAlive())
			return;
		packetsToSend.add(packet);
	}
	
	private void createClient(String serverIp, int port) {
		
		clientThread = new Thread(() -> {
			try(Socket cSocket = new Socket()){
				cSocket.connect(new InetSocketAddress(serverIp, port));
				this.clientSocket = cSocket;
				
				clientSocket.setTcpNoDelay(true);
				// Prepare the in and out streams.
				DataInputStream inputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				
				sendToServer(new IdentificationPacket(Minecraft.getMinecraft().player.getUniqueID()));
				
				createSendThread();
				
				// Use the current thread to indefinitly fetch packets
				Packet packet;
				while (clientSocket.isConnected()) {
					// Handle the next packet. If no packet is avilable, the readInt() call will hang until there is one.
					int packetSize = inputStream.readInt();
					byte[] packetData = new byte[packetSize];
					inputStream.read(packetData, 0, packetSize);
					PacketBuffer packetBuf = new PacketBuffer(Unpooled.wrappedBuffer(packetData));
					// Deserialize and run the packet
					packet = PacketSerializer.deserialize(packetBuf);
					packet.handle(PacketSide.CLIENT, Minecraft.getMinecraft().player);
					logger.trace("Handled a " + packet.getClass().getSimpleName() + " from the socket.");
				}
				
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				// The custom TASmod client was closed and the end of stream was reached. The socket was shut down properly.
				logger.info("Custom TASmod client was shutdown");
			} catch (Exception exception) {
				logger.error("Custom TASmod client was unexpectedly shutdown {}", exception);
				exception.printStackTrace();
			}
		});
		clientThread.setName("TASmod Network Client Accept");
		clientThread.setDaemon(true);
		clientThread.start();
	}
	
	private void createSendThread() throws IOException {
		DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
		// Create a new thread that writes packets if available
		Thread outputThread = new Thread(() -> {
			try {
				Packet packet;
				while (!clientSocket.isClosed()) {
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
					logger.trace("Sent a " + packet.getClass().getSimpleName() + " to the socket.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				// This exception is already logged by the thread one layer above
				// therefore nothing needs to be done here.
			}
		});
		outputThread.setDaemon(true); // If daemon is set, the jvm will quit without waiting for this thread to finish
		outputThread.setName("TASmod Network Client Send");
		outputThread.start();
	}
	
	public void killClient() throws IOException {
		if(clientThread != null && clientSocket != null) {
			clientSocket.close();
		}
	}
	
	public boolean isClosed() {
		return clientSocket.isClosed();
	}
	
}
