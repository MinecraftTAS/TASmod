package com.minecrafttas.common.server;

import static com.minecrafttas.common.server.SecureList.BUFFER_SIZE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;

import com.minecrafttas.common.Common;
import com.minecrafttas.common.server.exception.InvalidPacketException;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.PacketID;

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

	private UUID clientID;

	private Side side;

	public enum Side {
		CLIENT, SERVER;
	}

	/**
	 * Create and connect socket
	 * 
	 * @param host      Host
	 * @param port      Port
	 * @param packetIDs A list of PacketIDs which are registered
	 * @param uuid      The UUID of the client
	 * @throws Exception Unable to connect
	 */
	public Client(String host, int port, PacketID[] packetIDs, UUID uuid) throws Exception {
		Common.LOGGER.info("Connecting server to {}:{}", host, port);
		this.socket = AsynchronousSocketChannel.open();
		this.socket.connect(new InetSocketAddress(host, port)).get();

		this.side = Side.CLIENT;
		this.packetIDs = packetIDs;

		this.createHandlers();
		Common.LOGGER.info("Connected to server");

		authenticate(uuid);
	}

	/**
	 * Fork existing socket
	 * 
	 * @param socket Socket
	 */
	public Client(AsynchronousSocketChannel socket, PacketID[] packetIDs) {
		this.socket = socket;
		this.packetIDs = packetIDs;
		this.createHandlers();
		this.side = Side.SERVER;
	}

	/**
	 * Create read/write buffers and handlers for socket
	 */
	private void createHandlers() {
		// create input handler
		this.readBuffer.limit(4);
		this.socket.read(this.readBuffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
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
					Common.LOGGER.error("Unable to read packet!", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				Common.LOGGER.error("Unable to read packet!", exc);
			}

		});
	}

	/**
	 * Write packet to server
	 * 
	 * @param id  Buffer id
	 * @param buf Buffer
	 * @throws Exception Networking exception
	 */
	public void send(ByteBufferBuilder bufferBuilder) throws Exception {
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
	public void close() throws IOException {
		if (this.socket == null || !this.socket.isOpen()) {
			Common.LOGGER.warn("Tried to close dead socket");
			return;
		}

		this.socket.close();
	}

	/**
	 * Sends then authentication packet to the server
	 * 
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	private void authenticate(UUID id) throws Exception {
		this.clientID = id;
		Common.LOGGER.info("Authenticating with UUID {}", id.toString());
		this.send(new ByteBufferBuilder(-1).writeUUID(id));
	}

	private void completeAuthentication(ByteBuffer buf) throws Exception {
		if (this.clientID != null) {
			throw new Exception("The client tried to authenticate while being authenticated already");
		}

		long mostSignificant = buf.getLong();
		long leastSignificant = buf.getLong();

		this.clientID = new UUID(mostSignificant, leastSignificant);
	}

	private void handle(ByteBuffer buf) {
		int id = buf.getInt();
		try {
			if (id == -1) {
				completeAuthentication(buf);
				return;
			}
			PacketID packet = getPacketFromID(id);
			PacketHandlerRegistry.handle(side, packet, buf, this.clientID);
		} catch (PacketNotImplementedException | WrongSideException e) {
			Common.LOGGER.throwing(Level.ERROR, e);
		} catch (Exception e) {
			Common.LOGGER.throwing(Level.ERROR, e);
		}

	}

	public UUID getId() {
		return this.clientID;
	}

	private PacketID getPacketFromID(int id) throws InvalidPacketException {
		for (PacketID packet : packetIDs) {
			if (packet.getID() == id) {
				return packet;
			}
		}
		throw new InvalidPacketException(String.format("Received invalid packet with id {}", id));
	}

	public boolean isClosed() {
		return this.socket == null || !this.socket.isOpen();
	}
}
