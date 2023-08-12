package com.minecrafttas.server;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minecrafttas.server.interfaces.PacketID;

public class Server {

	private final AsynchronousServerSocketChannel socket;
	private final List<Client> clients;
	
	/**
	 * Create and bind socket
	 * @param port Port
	 * @throws Exception Unable to bind
	 */
	public Server(int port, PacketID[] packetIDs) throws Exception {
		// create connection
		LOGGER.info("Creating tasmod server on {}", port);
		this.socket = AsynchronousServerSocketChannel.open();
		this.socket.bind(new InetSocketAddress(port));
		
		// create connection handler
		this.clients = new ArrayList<>();
		this.socket.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

			@Override
			public void completed(AsynchronousSocketChannel clientSocket, Object attachment) {
				clients.add(new Client(clientSocket, packetIDs));
				socket.accept(null, this);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to accept client!", exc);
			}
		});
		
		LOGGER.info("TASmod server created");
	}
	
	/**
	 * Write packet to all clients
	 * @param id Buffer id
	 * @param buf Buffer
	 * @throws Exception Networking exception
	 */
	public void sendToAll(ByteBufferBuilder builder) throws Exception {
		for (Client client : this.clients) {
			client.send(builder.clone());
		}
		builder.close();
	}
	
	public void sendTo(UUID uuid, ByteBufferBuilder builder) throws Exception{
		Client client = getClient(uuid);
		client.send(builder);
	}
	
	/**
	 * Try to close socket
	 * @throws IOException Unable to close
	 */
	public void close() throws IOException {
		if (this.socket == null || !this.socket.isOpen()) {
			LOGGER.warn("Tried to close dead socket");
			return;
		}
		
		this.socket.close();
	}

	/**
	 * Get client from UUID
	 * @param uniqueID UUID
	 */
	private Client getClient(UUID uniqueID) {
		for (Client client : this.clients)
			if (client.getId().equals(uniqueID))
				return client;
		
		return null;
	}
	
	public List<Client> getClients(){
		return this.clients;
	}
	
	public AsynchronousServerSocketChannel getAsynchronousSocketChannel() {
		return this.socket;
	}
	
}
