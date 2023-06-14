package com.minecrafttas.server;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import lombok.var;

public class Client {
	
	private AsynchronousSocketChannel socket;
	private ByteBuffer writeBuffer;
	private ByteBuffer readBuffer;
	private Future<Integer> future;
	private Map<Integer, Consumer<ByteBuffer>> handlers = new HashMap<>();
	private UUID id;
	
	/**
	 * Create and connect socket
	 * @param host Host
	 * @param port Port
	 * @throws Exception Unable to connect
	 */
	public Client(String host, int port) throws Exception {
		LOGGER.info("Connecting tasmod server to {}:{}", host, port);
		this.socket = AsynchronousSocketChannel.open();
		this.socket.connect(new InetSocketAddress(host, port)).get();
		this.createHandlers();
		this.registerClientsidePacketHandlers();
		LOGGER.info("Connected to tasmod server");
	}
	
	/**
	 * Fork existing socket
	 * @param socket Socket
	 */
	public Client(AsynchronousSocketChannel socket) {
		this.socket = socket;
		this.createHandlers();
		this.registerServersidePacketHandlers();
	}
	
	/**
	 * Create read/write buffers and handlers for socket
	 */
	private void createHandlers() {
		// create buffers
		this.writeBuffer = ByteBuffer.allocate(1024*1024);
		this.readBuffer = ByteBuffer.allocate(1024*1024);

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
					LOGGER.error("Unable to read packet {}", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to read packet {}", exc);
			}

		});
	}
	
	/**
	 * Write packet to server
	 * @param buf Packet
	 * @throws Exception Networking exception
	 */
	public void write(ByteBuffer buf) throws Exception {
		// wait for previous buffer to send
		if (this.future != null && !this.future.isDone())
			this.future.get();
		
		// prepare buffer
		this.writeBuffer.clear();
		this.writeBuffer.putInt(buf.capacity());
		this.writeBuffer.put((ByteBuffer) buf.position(0));
		this.writeBuffer.flip();
		
		// send buffer async
		this.future = this.socket.write(this.writeBuffer);
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
	 * Register packet handlers for packets received on the client
	 */
	private void registerClientsidePacketHandlers() {
		
	}
	
	/**
	 * Register packet handlers for packets received on the server
	 */
	private void registerServersidePacketHandlers() {
		this.handlers.put(1, buf -> {
			this.id = new UUID(buf.getLong(), buf.getLong());
			LOGGER.info("Client authenticated: " + this.id);
		});
	}
	
	/**
	 * Sends then authentication packet to the server
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	public void authenticate(UUID id) throws Exception {
		ByteBuffer buf = ByteBuffer.allocate(4+8+8);
		buf.putInt(1);
		buf.putLong(id.getMostSignificantBits());
		buf.putLong(id.getLeastSignificantBits());
		this.write(buf);
	}
	
	private void handle(ByteBuffer buf) {
		var id = buf.getInt();
		this.handlers.getOrDefault(id, _buf -> {
			LOGGER.error("Received invalid packet: {}", id);
		}).accept(buf);
	}

}
