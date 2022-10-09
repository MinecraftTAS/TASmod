package de.scribble.lp.tasmod.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

/**
 * A custom packet server extending beyond the standard Minecraft network manager
 * @author Pancake, Scribble
 *
 */
public class PacketServer {
	
	private Logger logger;
	
	private Thread serverThread;
	
	private ServerSocket serverSocket;
	
	private LinkedBlockingQueue<BlockingQueue<Packet>> queues = new LinkedBlockingQueue<>();
	
	private int connections = 0;
	
	public PacketServer(Logger logger) throws IOException {
		this.logger = logger;
		createServer(3111);
	}
	
	public PacketServer(Logger logger, int port) throws IOException {
		this.logger = logger;
		createServer(port);
	}
	
	private void createServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverThread = new Thread(() -> {
			
			while (!serverSocket.isClosed()) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
				} catch (IOException e) {
					logger.error("Error creating socket");
					e.printStackTrace();
				} catch (Exception e) {
				}
				connections++;
				final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();
				queues.add(queue);

				new Handler(socket, queue);

			}
			
		});
		serverThread.setName("TASmod PacketServer");
		serverThread.start();
	}
	
	public void sendPacket(Packet packet) {
		if(serverThread.isAlive()) {
			queues.forEach(queue -> queue.add(packet));
		}
	}
	
	public int getConnections() {
		return connections;
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connections = 0;
	}
	
	private class Handler {
		
		private SocketAcceptThread accept;
		
		private SocketSendThread send;
		
		public Handler(Socket socket, LinkedBlockingQueue<Packet> queue) {
			this.accept = new SocketAcceptThread(socket);
			this.send = new SocketSendThread(socket, queue);
			accept.start();
			send.start();
		}
		
	}
	
	private class SocketAcceptThread extends Thread{
		
		private Socket socket;
		
		public SocketAcceptThread(Socket socket) {
			this.setName("ServerSocket Accept "+connections);
			setDaemon(true);
			this.socket = socket;
		}
		
		@Override
		public void run() {
			
			DataInputStream inputStream;
			
			try {
				inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			} catch (Exception e2) {
				return;
			}
			Packet packet;
			while (!socket.isClosed()) {
				// Handle the next packet. If no packet is avilable, the readInt() call will
				// hang until there is one.
				try {
					int packetSize = inputStream.readInt();
					byte[] packetData = new byte[packetSize];
					inputStream.read(packetData, 0, packetSize);

					PacketBuffer packetBuf = new PacketBuffer(Unpooled.wrappedBuffer(packetData));
					// Deserialize and run the packet
					packet = PacketSerializer.deserialize(packetBuf);
					packet.handle();
					logger.trace("Handled a " + packet.getClass().getSimpleName() + " from the socket.");
				} catch (Exception e) {
					logger.error(e.getMessage());
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			connections--;
		}
		
	}

	private class SocketSendThread extends Thread{
		
		private Socket socket;
		
		private BlockingQueue<Packet> packetQueue;
		
		public SocketSendThread(Socket socket, BlockingQueue<Packet> packetQueue) {
			setName("ServerSocket Send "+ connections);
			setDaemon(true);
			this.socket = socket;
			this.packetQueue = packetQueue;
		}
		
		@Override
		public void run() {
			try {
				DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				Packet packet;
				while (!socket.isClosed()) {
					// Try to poll another packet that wants to be sent
					packet = packetQueue.poll();
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
			} catch (Exception exception) {
				// This exception is already logged by the thread one layer above
				// therefore nothing needs to be done here.
			}
		}
	}
}
