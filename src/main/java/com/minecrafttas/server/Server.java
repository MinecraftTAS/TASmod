package com.minecrafttas.server;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.var;

public class Server {
	
	private AsynchronousServerSocketChannel socket;
	
	@Getter
	private List<Client> clients;
	
	/**
	 * Create and bind socket
	 * @param port Port
	 * @throws Exception Unable to bind
	 */
	public Server(int port) throws Exception {
		// create connection
		LOGGER.info("Creating tasmod server on {}", port);
		this.socket = AsynchronousServerSocketChannel.open();
		this.socket.bind(new InetSocketAddress(port));
		
		// create connection handler
		this.clients = new ArrayList<>();
		this.socket.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

			@Override
			public void completed(AsynchronousSocketChannel clientSocket, Object attachment) {
				clients.add(new Client(clientSocket));
				socket.accept(null, this);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to accept client {}", exc);
			}
		});
		
		LOGGER.info("TASmod server created");
	}
	
	/**
	 * Write packet to all clients
	 * @param buf Packet
	 * @throws Exception Networking exception
	 */
	public void writeAll(ByteBuffer buf) throws Exception {
		for (var client : this.clients)
			client.write(buf);
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
	public Client getClient(UUID uniqueID) {
		for (var client : this.clients)
			if (client.getId().equals(uniqueID))
				return client;
		
		return null;
	}
	
}
