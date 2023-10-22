package com.minecrafttas.common.server;

import static com.minecrafttas.common.Common.Client;
import static com.minecrafttas.common.Common.LOGGER;
import static com.minecrafttas.common.Common.Server;
import static com.minecrafttas.common.server.SecureList.BUFFER_SIZE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

import com.minecrafttas.common.Common;
import com.minecrafttas.common.events.EventClient.EventDisconnectClient;
import com.minecrafttas.common.events.EventServer.EventClientCompleteAuthentication;
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

	private String username;
	private String ip;
	private int port;

	private Side side;
	
	/*=Timeout checking=*/
	private long timeout;
	private TimerTask timertask;
	/**
	 * Timestamp of the last packet that was received
	 */
	private long timeAtLastPacket;
	private ClientCallback callback;
	
	private static Timer timeoutTimer = new Timer("Timeout Timer", true);

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
	 * @param timout	Time since last packet when the client should disconnect
	 * @throws Exception Unable to connect
	 */
	public Client(String host, int port, PacketID[] packetIDs, String name, long timeout) throws Exception {
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
		
		this.timeout = timeout;
		this.callback = (client)-> {
			EventDisconnectClient.fireDisconnectClient(client);
		};
		this.registerTimeoutTask(100);
		LOGGER.info(Client, "Connected to server");

		username = name;
		
		authenticate(name);
	}

	/**
	 * Fork existing socket
	 * 
	 * @param socket Socket
	 */
	public Client(AsynchronousSocketChannel socket, PacketID[] packetIDs, long timeout, ClientCallback callback) {
		this.socket = socket;
		this.callback = callback;
		this.timeout = timeout;
		try {
			this.socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			this.socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (IOException e) {
			LOGGER.error(Server, "Unable to set socket options", e);
		}
		this.packetIDs = packetIDs;
		this.createHandlers();
		this.registerTimeoutTask(100);
		this.side = Side.SERVER;
	}

	/**
	 * Disconnecting and closing the socket. Sends a disconnect packet to the other side
	 */
	public void disconnect() {
		
		if(isClosed()) {
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
		if(socket == null || !socket.isOpen()) {
			LOGGER.info(getLoggerMarker(), "Connection was closed");
			return;
		}
		this.socket.read(this.readBuffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				if(result == -1) {
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
					LOGGER.error("Unable to read packet!", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				if(exc instanceof AsynchronousCloseException || exc instanceof IOException) {
					LOGGER.warn(getLoggerMarker(), "Connection was closed!");
				} else {
					LOGGER.error(getLoggerMarker(), "Something went wrong!", exc);
				}
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
	private void close() throws IOException {
		if (this.socket == null || !this.socket.isOpen()) {
			Common.LOGGER.warn(getLoggerMarker(), "Tried to close dead socket");
			return;
		}
		this.future=null;
		
		//Running the callback
		if(callback!=null) {
			callback.onClose(this);
		}
		
		this.timertask.cancel();
		this.socket.close();
	}

	private Marker getLoggerMarker() {
		return side==Side.CLIENT? Client:Server;
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
		EventClientCompleteAuthentication.fireClientCompleteAuthentication(username);
	}

	private void handle(ByteBuffer buf) {
		int id = buf.getInt();
		try {
			if (id == -1) {
				completeAuthentication(buf);
				return;
			}else if (id == -2){
				LOGGER.debug(getLoggerMarker(), "Disconnected by the {}", side.name(), (side==Side.CLIENT?Side.CLIENT:Side.SERVER).name());
				close();
				return;
			}
			timeAtLastPacket = System.currentTimeMillis();
			PacketID packet = getPacketFromID(id);
			PacketHandlerRegistry.handle(side, packet, buf, this.username);
		} catch (PacketNotImplementedException | WrongSideException e) {
			Common.LOGGER.throwing(Level.ERROR, e);
		} catch (Exception e) {
			Common.LOGGER.throwing(Level.ERROR, e);
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
		throw new InvalidPacketException(String.format("Received invalid packet with id {}", id));
	}

	public boolean isClosed() {
		return this.socket == null || !this.socket.isOpen();
	}
	
	public String getRemote() throws IOException {
		return ip+":"+port;
	}
	
	/**
	 * Registers a task for the timer, that checks if this socket is timed out.
	 * 
	 * <p>The interval is 1 second
	 * 
	 */
	private void registerTimeoutTask(long delay) {
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				if(checkTimeout()) {
					disconnect();
				}
			}

		};
		this.timertask = task;
		timeAtLastPacket = System.currentTimeMillis();
		timeoutTimer.scheduleAtFixedRate(task, delay, 1000);
	}
	
	private boolean checkTimeout() {
		long timeSinceLastPacket = System.currentTimeMillis() - timeAtLastPacket;
		
		if(timeSinceLastPacket > timeout) {
			LOGGER.warn(getLoggerMarker(), "Client {} timed out after {}ms", getId(), timeSinceLastPacket);
			return true;
		}else if(timeSinceLastPacket < 0) {
			LOGGER.error(getLoggerMarker(), "Time ran backwards? Timing out client {}: {}ms", getId(), timeSinceLastPacket);
			return true;
		}
		
		return false;
	}
	
	public void setTimeoutTime(long timeout) {
		this.timeAtLastPacket = System.currentTimeMillis();
		this.timeout = timeout;
	}
	
	public long getTimeoutTime() {
		return this.timeout;
	}
	
	@FunctionalInterface
	public interface ClientCallback{
		public void onClose(Client client); 
	}
}
