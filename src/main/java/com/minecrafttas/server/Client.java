package com.minecrafttas.server;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import lombok.RequiredArgsConstructor;
import lombok.var;

@RequiredArgsConstructor
public class Client {

	private final String host;
	private final int port;

	private AsynchronousSocketChannel socket;
	private ByteBuffer writePacketHeader;
	
	public static void main(String[] args) throws Exception {
		Client c = new Client("127.0.0.1", 5555);
		c.connect();
		c.write(ByteBuffer.allocate(4));
	}
	
	/**
	 * Try to connect socket
	 * @throws Exception Unable to connect
	 */
	public void connect() throws Exception {
		if (this.isAlive()) {
			LOGGER.warn("Tried to connect alive socket");
			return;
		}
		
		// create connection
		LOGGER.info("Connecting tasmod server to {}:{}", this.host, this.port);
		this.socket = AsynchronousSocketChannel.open();
		this.socket.connect(new InetSocketAddress(this.host, this.port)).get();
		
		// create buffers
		this.writePacketHeader = ByteBuffer.allocate(4);
		var readPacketHeader = ByteBuffer.allocate(4);
		
		// create input handler
		this.socket.read(readPacketHeader, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				try {
					ByteBuffer data = ByteBuffer.allocate(readPacketHeader.getInt());
					socket.read(data).get();

					data.position(0);
					handle(data);
					
					socket.read(readPacketHeader, null, this); // read packet header again
				} catch (Throwable exc) {
					LOGGER.error("Unable to read packet from server {}", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to read packet from server {}", exc);
			}
			
		});
		
		LOGGER.info("Connected to tasmod server");
	}
	
	/**
	 * Write packet to server
	 * @param buf Packet
	 */
	public void write(ByteBuffer buf) {
		this.writePacketHeader.clear();
		this.writePacketHeader.putInt(buf.capacity());
		this.socket.write(this.writePacketHeader, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				try {
					buf.position(0);
					socket.write(buf).get();
				} catch (Throwable exc) {
					LOGGER.error("Unable to send packet to server {}", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to send packet to server {}", exc);
			}
			
		});
	}
	
	private void handle(ByteBuffer buf) {
		System.out.println("hello buf, " + buf.getDouble());
	}
	
	/**
	 * Try to close socket
	 * @throws IOException Unable to close
	 */
	public void close() throws IOException {
		if (!this.isAlive()) {
			LOGGER.warn("Tried to close dead socket");
			return;
		}
		
		this.socket.close();
	}
	
	public boolean isAlive() {
		return this.socket != null && this.socket.isOpen();
	}
	
}
