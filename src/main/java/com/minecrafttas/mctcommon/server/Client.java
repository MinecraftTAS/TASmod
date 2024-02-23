package com.minecrafttas.mctcommon.server;

import static com.minecrafttas.mctcommon.MCTCommon.Client;
import static com.minecrafttas.mctcommon.MCTCommon.LOGGER;
import static com.minecrafttas.mctcommon.MCTCommon.Server;
import static com.minecrafttas.mctcommon.server.SecureList.BUFFER_SIZE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

import com.minecrafttas.mctcommon.MCTCommon;
import com.minecrafttas.mctcommon.events.EventClient.EventDisconnectClient;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.events.EventServer.EventClientCompleteAuthentication;
import com.minecrafttas.mctcommon.server.exception.InvalidPacketException;
import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;
import com.minecrafttas.mctcommon.server.interfaces.PacketID;

/**
 * A custom asynchronous client
 * 
 * @author Pancake
 */
public class Client {

	private final AsynchronousSocketChannel socket;
	private final PacketID[] packetIDs;
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private Future<Integer> future;

	private String username;
	private String ip;
	private int port;

	private Side side;

	private ClientCallback callback;
	/**
	 * True, if the client is connected to a local "integrated" server. Special
	 * conditions may apply
	 */
	private boolean local = false;


	public enum Side {
		CLIENT, SERVER;
	}

	/**
	 * Create and connect socket
	 * 
	 * @param host      Host
	 * @param port      Port
	 * @param packetIDs A list of PacketIDs which are registered
	 * @param local     Property to check, if the server is a local server. If yes,
	 *                  special conditions may apply
	 * @throws Exception Unable to connect
	 */
	public Client(String host, int port, PacketID[] packetIDs, String name, boolean local) throws Exception {
		LOGGER.info(Client, "Connecting server to {}:{}", host, port);
		this.socket = AsynchronousSocketChannel.open();
		this.socket.connect(new InetSocketAddress(host, port)).get();
		this.socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		this.socket.setOption(StandardSocketOptions.TCP_NODELAY, true);

		ip = host;
		this.port = port;

		this.side = Side.CLIENT;
		this.packetIDs = packetIDs;

		this.createHandlers();

		this.callback = (client) -> {
			EventListenerRegistry.fireEvent(EventDisconnectClient.class, client);
		};

		this.local = local;

		LOGGER.info(Client, "Connected to server");

		username = name;

		authenticate(name);
	}

	/**
	 * Fork existing socket
	 * 
	 * @param socket Socket
	 */
	public Client(AsynchronousSocketChannel socket, PacketID[] packetIDs, ClientCallback callback) {
		this.socket = socket;
		this.callback = callback;
		try {
			this.socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			this.socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (IOException e) {
			LOGGER.error(Server, "Unable to set socket options", e);
		}
		this.packetIDs = packetIDs;
		this.createHandlers();
		this.side = Side.SERVER;
	}

	/**
	 * Disconnecting and closing the socket. Sends a disconnect packet to the other
	 * side
	 */
	public void disconnect() {

		if (isClosed()) {
			LOGGER.warn(getLoggerMarker(), "Tried to disconnect, but client {} is already closed", getId());
			return;
		}

		// Sending the disconnect packet
		try {
			send(new ByteBufferBuilder(-2));
		} catch (Exception e) {
			LOGGER.error(getLoggerMarker(), "Tried to send disconnect packet, but failed", e);
		}

		try {
			this.close();
		} catch (IOException e) {
			LOGGER.error(getLoggerMarker(), "Tried to close socket, but failed", e);
		}
	}

	/**
	 * Create read/write buffers and handlers for socket
	 */
	private void createHandlers() {
		// create input handler
		this.readBuffer.limit(4);
		if (socket == null || !socket.isOpen()) {
			LOGGER.info(getLoggerMarker(), "Connection was closed");
			return;
		}
		this.socket.read(this.readBuffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				if (result == -1) {
					LOGGER.info(getLoggerMarker(), "Stream was closed");
					return;
				}
				try {
					// read rest of packet
					readBuffer.flip();
					int lim = readBuffer.getInt();
					readBuffer.clear().limit(lim);
					socket.read(readBuffer).get();

					// handle packet
					readBuffer.position(0);
					handle(readBuffer);

					// read packet header again
					readBuffer.clear().limit(4);
					socket.read(readBuffer, null, this);
				} catch (Throwable exc) {
					if(exc instanceof ExecutionException && !isClosed()) {
						LOGGER.debug(getLoggerMarker(), "{} terminated the connection!", getOppositeSide().name());
						try {
							close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
					LOGGER.error(getLoggerMarker(), "Unable to read packet!", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				if (exc instanceof AsynchronousCloseException || exc instanceof IOException) {
					if(isClosed()) {
						return;
					}
					LOGGER.debug(getLoggerMarker(), "{} terminated the connection!", getOppositeSide().name());
					try {
						close();
					} catch (IOException e) {
						LOGGER.error(getLoggerMarker(), "Attempted to close connection but failed", e);
					}
				} else {
					if(isClosed()) {
						return;
					}
					LOGGER.error(getLoggerMarker(), "Something went wrong, terminating connection!", exc);
					try {
						close();
					} catch (IOException e) {
						LOGGER.error(getLoggerMarker(), "Attempted to close connection but failed", e);
					}
				}
			}

		});
	}

	/**
	 * Sends a packet to the server
	 * 
	 * @param bufferBuilder The bufferbuilder to use for sending a packet
	 * @throws Exception Networking exception
	 */
	public void send(ByteBufferBuilder bufferBuilder) throws Exception {
		if(bufferBuilder.getPacketID() != null && bufferBuilder.getPacketID().shouldTrace())
			LOGGER.trace(getLoggerMarker(), "Sending a {} packet to the {} with content:\n{}", bufferBuilder.getPacketID(), getOppositeSide(), bufferBuilder.getPacketContent());
		// wait for previous buffer to send
		if (this.future != null && !this.future.isDone())
			this.future.get();

		ByteBuffer buf = bufferBuilder.build();

		// prepare buffer
		buf.flip();
		this.writeBuffer.clear();
		this.writeBuffer.putInt(buf.limit());
		this.writeBuffer.put(buf);
		this.writeBuffer.flip();

		// send buffer async
		this.future = this.socket.write(this.writeBuffer);
		bufferBuilder.close();
	}

	/**
	 * Try to close socket
	 * 
	 * @throws IOException Unable to close
	 */
	private void close() throws IOException {
		if (this.socket == null || !this.socket.isOpen()) {
			MCTCommon.LOGGER.warn(getLoggerMarker(), "Tried to close dead socket");
			return;
		}
		this.future = null;

		// Running the callback
		if (callback != null) {
			callback.onClose(this);
		}

		this.socket.close();
	}

	private Marker getLoggerMarker() {
		return side == Side.CLIENT ? Client : Server;
	}
	
	private Side getOppositeSide() {
		return side == Side.CLIENT ? Side.SERVER : Side.CLIENT;
	}

	/**
	 * Sends then authentication packet to the server
	 * 
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	private void authenticate(String id) throws Exception {
		this.username = id;
		LOGGER.debug(getLoggerMarker(), "Authenticating with UUID {}", id.toString());
		this.send(new ByteBufferBuilder(-1).writeString(id));
	}

	private void completeAuthentication(ByteBuffer buf) throws Exception {
		if (this.username != null) {
			throw new Exception("The client tried to authenticate while being authenticated already");
		}

		this.username = ByteBufferBuilder.readString(buf);
		LOGGER.debug(getLoggerMarker(), "Completing authentication for user {}", username);
		EventListenerRegistry.fireEvent(EventClientCompleteAuthentication.class, username);
	}

	private void handle(ByteBuffer buf) {
		int id = buf.getInt();
		try {
			if (id == -1) {
				completeAuthentication(buf);
				return;
			} else if (id == -2) {
				LOGGER.info(getLoggerMarker(), "Disconnected by the {}", getOppositeSide().name());
				close();
				return;
			}
			PacketID packet = getPacketFromID(id);
			PacketHandlerRegistry.handle(side, packet, buf, this.username);
		} catch (PacketNotImplementedException | WrongSideException e) {
			MCTCommon.LOGGER.throwing(Level.ERROR, e);
		} catch (Exception e) {
			MCTCommon.LOGGER.throwing(Level.ERROR, e);
		}

	}

	public String getId() {
		return this.username;
	}

	private PacketID getPacketFromID(int id) throws InvalidPacketException {
		for (PacketID packet : packetIDs) {
			if (packet.getID() == id) {
				return packet;
			}
		}
		throw new InvalidPacketException(String.format("Received invalid packet with id %s", id));
	}

	public boolean isClosed() {
		return this.socket == null || !this.socket.isOpen();
	}

	public String getRemote() throws IOException {
		return ip + ":" + port;
	}

	public boolean isLocal() {
		return this.local;
	}

	@FunctionalInterface
	public interface ClientCallback {
		public void onClose(Client client);
	}
}
