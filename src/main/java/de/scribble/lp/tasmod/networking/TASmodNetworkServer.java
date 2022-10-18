package de.scribble.lp.tasmod.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
public class TASmodNetworkServer {
	
	private Logger logger;
	
	private Thread serverThread;
	
	private ServerSocket serverSocket;
	
	private LinkedBlockingQueue<BlockingQueue<Packet>> queues = new LinkedBlockingQueue<>();
	
	private int connections = 0;
	
	public TASmodNetworkServer(Logger logger) throws IOException {
		this.logger = logger;
		createServer(3111);
	}
	
	public TASmodNetworkServer(Logger logger, int port) throws IOException {
		this.logger = logger;
		createServer(port);
	}
	
	private void createServer(int port) throws IOException {
		serverThread = new Thread(() -> {
			
			try(ServerSocket serverS = new ServerSocket(port)){
				this.serverSocket = serverS;
				
				while (!this.serverSocket.isClosed()) {
					Socket socket = null;
					
					socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
						
					connections++;
					final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();
					queues.add(queue);

					new Handler(socket, queue);
				}
				
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				logger.debug("Custom TASmod server was shutdown");
				exception.printStackTrace();
			} catch (Exception exception) {
				logger.error("Custom TASmod server was unexpectedly shutdown {}", exception);
				exception.printStackTrace();
			}
			
		});
		serverThread.setName("TASmod Network Server Main");
		serverThread.setDaemon(true);
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
		
		private SocketHandleAccept accept;
		
		private SocketHandleSend send;
		
		public Handler(Socket socket, LinkedBlockingQueue<Packet> queue) {
			this.accept = new SocketHandleAccept(socket);
			this.send = new SocketHandleSend(socket, queue);
			accept.start();
			send.start();
		}
		
	}
	
	private class SocketHandleAccept extends Thread{
		
		private Socket socket;
		
		public SocketHandleAccept(Socket socket) {
			this.setName(String.format("TASmod Network Server PacketAccept #%s", connections));
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
					e.printStackTrace();
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

	private class SocketHandleSend extends Thread{
		
		private Socket socket;
		
		private BlockingQueue<Packet> packetQueue;
		
		public SocketHandleSend(Socket socket, BlockingQueue<Packet> packetQueue) {
			setName(String.format("TASmod Network Server PacketSend #%s", connections));
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
				exception.printStackTrace();
				// This exception is already logged by the thread one layer above
				// therefore nothing needs to be done here.
			}
		}
	}
}
