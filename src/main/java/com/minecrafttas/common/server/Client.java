package com.minecrafttas.common.server;

import static com.minecrafttas.common.server.SecureList.BUFFER_SIZE;
import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;

import com.minecrafttas.common.server.exception.InvalidPacketException;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.PacketID;

public class Client {

	private final AsynchronousSocketChannel socket;
	private final PacketID[] packetIDs;
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private Future<Integer> future;

	private UUID clientID;
	
	private Side side;
	
	
	public enum Side {
		CLIENT,
		SERVER;
	}
	
	/**
	 * Create and connect socket
	 * @param host Host
	 * @param port Port
	 * @throws Exception Unable to connect
	 */
	public Client(String host, int port, PacketID[] packetIDs, UUID uuid) throws Exception {
		LOGGER.info("Connecting tasmod server to {}:{}", host, port);
		this.socket = AsynchronousSocketChannel.open();
		this.socket.connect(new InetSocketAddress(host, port)).get();

		this.side = Side.CLIENT;
		this.packetIDs = packetIDs;
		
		this.createHandlers();
		Server.LOGGER.info("Connected to tasmod server");
		
		authenticate(uuid);
	}
	
	/**
	 * Fork existing socket
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
					Server.LOGGER.error("Unable to read packet!", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				Server.LOGGER.error("Unable to read packet!", exc);
			}

		});
	}
	
	/**
	 * Write packet to server
	 * @param id Buffer id
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
	 * @throws IOException Unable to close
	 */
	public void close() throws IOException {
		if (this.socket == null || !this.socket.isOpen()) {
			LOGGER.warn("Tried to close dead socket");
			return;
		}
		
		this.socket.close();
	}
	
	// move wherever you want
//	public static enum ClientPackets implements Packet {
//		TICK_CLIENT((pid, buf, id) ->
//			TickSyncClient.onPacket()),
//		CHANGE_CLIENT_TICKRATE((pid, buf, id) ->
//			TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat())),
//		ADVANCE_TICK_ON_CLIENTS((pid, buf, id) ->
//			TASmodClient.tickratechanger.advanceClientTick()),
//		CHANGE_TICKRATE_ON_CLIENTS((pid, buf, id) ->
//			TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat())), // funny duplicate please fix
//		SAVESTATE_INPUTS_CLIENT((pid, buf, id) -> {
//			try {
//				byte[] nameBytes = new byte[buf.getInt()];
//				buf.get(nameBytes);
//				String name = new String(nameBytes);
//				InputSavestatesHandler.savestate(name);
//			} catch (Exception e) {
//				TASmod.LOGGER.error("Exception occured during input savestate:", e);
//			}
//		}),
//		CLOSE_GUISAVESTATESCREEN_ON_CLIENTS((pid, buf, id) -> {
//			Minecraft mc = Minecraft.getMinecraft();
//			if (!(mc.currentScreen instanceof GuiSavestateSavingScreen))
//				mc.displayGuiScreen(new GuiSavestateSavingScreen());
//			else
//				mc.displayGuiScreen(null);
//		}),
//		LOADSTATE_INPUTS_CLIENT((pid, buf, id) -> {
//			try {
//				byte[] nameBytes = new byte[buf.getInt()];
//				buf.get(nameBytes);
//				String name = new String(nameBytes);
//				InputSavestatesHandler.loadstate(name);
//			} catch (Exception e) {
//				TASmod.LOGGER.error("Exception occured during input loadstate:", e);
//			}
//		}),
//		UNLOAD_CHUNKS_ON_CLIENTS((pid, buf, id) ->
//			Minecraft.getMinecraft().addScheduledTask(SavestatesChunkControl::unloadAllClientChunks)),
//		REQUEST_CLIENT_MOTION((pid, buf, id) -> {
//			EntityPlayerSP player = Minecraft.getMinecraft().player;
//			if (player != null) {
//				if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen))
//					Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
//
//				try {
//					// send client motion to server
//					int bufIndex = SecureList.POOL.available();
//					TASmodClient.client.write(bufIndex, SecureList.POOL.lock(bufIndex).putInt(ServerPackets.SEND_CLIENT_MOTION_TO_SERVER.ordinal())
//							.putDouble(player.motionX).putDouble(player.motionY).putDouble(player.motionZ)
//							.putFloat(player.moveForward).putFloat(player.moveVertical).putFloat(player.moveStrafing)
//							.put((byte) (player.isSprinting() ? 1 : 0))
//							.putFloat(player.jumpMovementFactor)
//					);
//				} catch (Exception e) {
//					TASmod.LOGGER.error("Unable to send packet to server:", e);
//				}
//			}
//		});
//	}

//	public static enum ServerPackets implements Packet {
//		NOTIFY_SERVER_OF_TICK_PASS((pid, buf, id) ->
//			TickSyncServer.onPacket(id)),
//		REQUEST_TICKRATE_CHANGE((pid, buf, id) ->
//			TASmod.tickratechanger.changeTickrate(buf.getFloat())),
//		TICKRATE_ZERO_TOGGLE((pid, buf, id) -> {
//			State state = TickrateChangerServer.State.fromShort(buf.getShort());
//			if (state == TickrateChangerServer.State.PAUSE)
//				TASmod.tickratechanger.pauseGame(true);
//			else if (state == TickrateChangerServer.State.UNPAUSE)
//				TASmod.tickratechanger.pauseGame(false);
//			else if (state == TickrateChangerServer.State.TOGGLE)
//				TASmod.tickratechanger.togglePause();
//		}),
//		REQUEST_TICK_ADVANCE((pid, buf, id) -> {
//			if (TASmod.tickratechanger.ticksPerSecond == 0)
//				TASmod.tickratechanger.advanceTick();
//		}),
//		SEND_CLIENT_MOTION_TO_SERVER((pid, buf, id) ->
//			ClientMotionServer.getMotion().put(TASmod.getServerInstance().getPlayerList().getPlayerByUUID(id), new ClientMotionServer.Saver(buf.getDouble(), buf.getDouble(), buf.getDouble(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.get() == 1, buf.getFloat())));
//
//
//		private final PacketHandler handler;
//
//		ServerPackets(PacketHandler handler) {
//			this.handler = handler;
//		}
//
//		@Override
//		public PacketHandler handler() {
//			return this.handler;
//		}
//	}

	/**
	 * Sends then authentication packet to the server
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	private void authenticate(UUID id) throws Exception {
		this.clientID = id;

		this.send(new ByteBufferBuilder(-1).writeUUID(id));
	}
	
	private void completeAuthentication(ByteBuffer buf) throws Exception {
		if(this.clientID!=null) {
			throw new Exception("The client tried to authenticate while being authenticated already");
		}
		
		long mostSignificant = buf.getLong();
		long leastSignificant = buf.getLong();
		
		this.clientID = new UUID(mostSignificant, leastSignificant);
	}
	
	private void handle(ByteBuffer buf) {
		int id = buf.getInt();
		try {
			if(id==-1) {
				completeAuthentication(buf);
				return;
			}
			PacketID packet = getPacketFromID(id);
			PacketHandlerRegistry.handle(side, packet, buf, this.clientID);
		} catch (PacketNotImplementedException | WrongSideException e) {
			Server.LOGGER.throwing(Level.ERROR, e);
		} catch (Exception e) {
			Server.LOGGER.throwing(Level.ERROR, e);
		}

	}
	
	public UUID getId() {
		return this.clientID;
	}

	
	private PacketID getPacketFromID(int id) throws InvalidPacketException {
		for(PacketID packet : packetIDs) {
			if(packet.getID() == id) {
				return packet;
			}
		}
		throw new InvalidPacketException(String.format("Received invalid packet with id {}", id));
	}
}
