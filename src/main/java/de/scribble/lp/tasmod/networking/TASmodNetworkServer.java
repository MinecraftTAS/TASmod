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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.packets.IdentificationPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
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
	
	private Map<Socket, UUID> connectedPlayers = Collections.synchronizedMap(new HashMap<Socket, UUID>());
	
	private Map<UUID, BlockingQueue<Packet>> queues = Collections.synchronizedMap(new HashMap<>());
	
	private int connections = 0;
	
	public TASmodNetworkServer(Logger logger) throws IOException {
		this(logger, 3111);
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
					
					final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();

					connections++;
					createSendThread(socket, queue);
					createAcceptThread(socket, queue);
				}
				
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				logger.debug("Custom TASmod server was shutdown");
			} catch (Exception exception) {
				logger.error("Custom TASmod server was unexpectedly shutdown {}", exception);
				exception.printStackTrace();
			}
			
		});
		serverThread.setName("TASmod Network Server Main");
		serverThread.setDaemon(true);
		serverThread.start();
	}
	
	private void createSendThread(Socket socket, LinkedBlockingQueue<Packet> packetQueue) throws IOException, InterruptedException {
		Thread sendThread = new Thread(()->{
			DataOutputStream outputStream;
			try {
				outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			Packet packet;
			while (!socket.isClosed()) {
				try {
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
				} catch(Exception e) {
					logger.catching(e);
				}
			}
		});
		sendThread.setDaemon(true);
		sendThread.setName("TASmod Network Server Send #"+connections);
		sendThread.start();
	}
	
	private void createAcceptThread(Socket socket, LinkedBlockingQueue<Packet> packetQueue) throws IOException {
		Thread acceptThread = new Thread(()->{
			DataInputStream inputStream;
			try {
				inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			} catch (IOException e2) {
				e2.printStackTrace();
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
					
					if(packet instanceof IdentificationPacket) {
						handleIdentificationPacket(packet, socket, packetQueue);
					}
					
					if(connectedPlayers.containsKey(socket)) {
						UUID id = connectedPlayers.get(socket);
						EntityPlayerMP player = TASmod.getServerInstance().getPlayerList().getPlayerByUUID(id);
						
						packet.handle(PacketSide.SERVER, player);
						logger.trace("Handled a " + packet.getClass().getSimpleName() + " from the socket.");
					}
				}catch (EOFException e){
					logger.info("Client socket was shut down");
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
			UUID id = connectedPlayers.get(socket);
			connectedPlayers.remove(socket);
			queues.remove(id);
			connections--;
		});
		acceptThread.setDaemon(true);
		acceptThread.setName("TASmod Network Server Accept #"+connections);
		acceptThread.start();
	}
	
	private void handleIdentificationPacket(Packet packet, Socket socket, LinkedBlockingQueue<Packet> packetQueue) {
		if(!connectedPlayers.containsKey(socket)) {
			IdentificationPacket idPacket = (IdentificationPacket) packet;
			logger.info("Identified player with uuid: {}", idPacket.getUuid());
			connectedPlayers.put(socket, idPacket.getUuid());
			queues.put(idPacket.getUuid(), packetQueue);
			sendTo(new IdentificationPacket(), idPacket.getUuid());
		}
	}

	public void sendToAll(Packet packet) {
		if(serverThread.isAlive()) {
			queues.forEach((id, queue) -> queue.add(packet));
		}
	}
	
	public void sendTo(Packet packet, EntityPlayerMP... players) {
		if(serverThread.isAlive()) {
			queues.forEach((id, queue) -> {
				for(EntityPlayerMP player : players) {
					if(player.getUniqueID().equals(id)) {
						queue.add(packet);
					}
				}
				
			});
		}
	}
	
	public void sendTo(Packet packet, UUID... uuids) {
		if(serverThread.isAlive()) {
			queues.forEach((id, queue) -> {
				for(UUID idToSend : uuids) {
					if(idToSend.equals(id)) {
						queue.add(packet);
					}
				}
			});
		}
	}
	
	public int getConnections() {
		return connections;
	}
	
	public void close() {
		if(serverSocket==null)
			return;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connections = 0;
	}
}
