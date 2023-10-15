package com.minecrafttas.common.server;

import static com.minecrafttas.common.Common.LOGGER;
import static com.minecrafttas.common.Common.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.common.Common;
import com.minecrafttas.common.server.interfaces.PacketID;

import net.minecraft.entity.player.EntityPlayer;

/**
 * A custom asynchronous server
 * 
 * @author Pancake
 */
public class Server {

	private final AsynchronousServerSocketChannel serverSocket;
	private final List<Client> clients;

	/**
	 * Create and bind socket
	 * 
	 * @param port Port
	 * @throws Exception Unable to bind
	 */
	public Server(int port, PacketID[] packetIDs) throws Exception {
		// create connection
		LOGGER.info(Server, "Creating server on port {}", port);
		this.serverSocket = AsynchronousServerSocketChannel.open();
		this.serverSocket.bind(new InetSocketAddress(port));

		// create connection handler
		this.clients = new ArrayList<>();
		this.serverSocket.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

			@Override
			public void completed(AsynchronousSocketChannel clientSocket, Object attachment) {
				cleanClients();
				clients.add(new Client(clientSocket, packetIDs));
				serverSocket.accept(null, this);
			}

			private void cleanClients() {
				for(Client client : clients) {
					if(client.isClosed()) {
						clients.remove(client);
					}
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				if(exc instanceof AsynchronousCloseException) {
					LOGGER.info(Server, "Connection to the player was closed!");
				} else {
					LOGGER.error(Server, "Unable to accept client!", exc);
				}
			}
		});

		LOGGER.info(Server, "Server created");
	}

	/**
	 * Write packet to all clients
	 * 
	 * @param builder The packet contents
	 * @throws Exception Networking exception
	 */
	public void sendToAll(ByteBufferBuilder builder) throws Exception {
		for (Client client : this.clients) {
			client.send(builder.clone());
		}
		builder.close();
	}

	/**
	 * Send a packet to the specified username
	 * 
	 * @param username The username to send the packet to
	 * @param builder The packet contents
	 * @throws Exception Networking exception
	 */
	public void sendTo(String username, ByteBufferBuilder builder) throws Exception {
		Client client = getClient(username);
		if(client != null && !client.isClosed()) {
			client.send(builder);
		} else {
			Common.LOGGER.warn(Server, "Buffer with id {} could not be sent to the client {}: The client is closed", builder.getId(), username);
		}
	}

	/**
	 * Send a packet to a specified player
	 * 
	 * Similar to {@link #sendTo(String, ByteBufferBuilder)}
	 * 
	 * @param player  The player to send to
	 * @param builder The packet contents
	 * @throws Exception Networking exception
	 */
	public void sendTo(EntityPlayer player, ByteBufferBuilder builder) throws Exception {
		sendTo(player.getName(), builder);
	}

	/**
	 * Try to close socket
	 * 
	 * @throws IOException Unable to close
	 */
	public void close() throws IOException {
		if (this.serverSocket == null || !this.serverSocket.isOpen()) {
			Common.LOGGER.warn(Server, "Tried to close dead socket on server");
			return;
		}

		this.serverSocket.close();
	}

	public boolean isClosed() {
		return this.serverSocket == null || !this.serverSocket.isOpen();
	}

	/**
	 * Get client from username
	 * 
	 * @param name Username
	 */
	private Client getClient(String name) {
		for (Client client : this.clients)
			if (client.getId().equals(name))
				return client;

		return null;
	}

	public List<Client> getClients() {
		return this.clients;
	}

	public AsynchronousServerSocketChannel getAsynchronousSocketChannel() {
		return this.serverSocket;
	}

}
