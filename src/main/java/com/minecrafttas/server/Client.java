package com.minecrafttas.server;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesHandler;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;
import com.minecrafttas.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import com.minecrafttas.tasmod.savestates.server.motion.ClientMotionServer;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.minecrafttas.server.SecureList.BUFFER_SIZE;
import static com.minecrafttas.tasmod.TASmod.LOGGER;

public class Client {

	private final AsynchronousSocketChannel socket;
	private final Map<Integer, Packet> packets = new HashMap<>();
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private Future<Integer> future;

	@Getter
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
					LOGGER.error("Unable to read packet!", exc);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				LOGGER.error("Unable to read packet!", exc);
			}

		});
	}
	
	/**
	 * Write packet to server
	 * @param id Buffer id
	 * @param buf Buffer
	 * @throws Exception Networking exception
	 */
	public void write(int id, ByteBuffer buf) throws Exception {
		// wait for previous buffer to send
		if (this.future != null && !this.future.isDone())
			this.future.get();
		
		// prepare buffer
		buf.flip();
		this.writeBuffer.clear();
		this.writeBuffer.putInt(buf.limit());
		this.writeBuffer.put(buf);
		this.writeBuffer.flip();

		// send buffer async
		this.future = this.socket.write(this.writeBuffer);
		SecureList.POOL.unlock(id);
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
		int id = 0;

		// move wherever you want - add client server packet handler
		for (var packet : ClientPackets.values())
			this.packets.put(packet.ordinal(), packet);
	}

	/**
	 * Register packet handlers for packets received on the server
	 */
	private void registerServersidePacketHandlers() {
		// add authentication packet
		this.packets.put(-1, () -> (pid, buf, uuid) -> {
			this.id = new UUID(buf.getLong(), buf.getLong());
			LOGGER.info("Client authenticated: " + this.id);
		});

		// move wherever you want - add server packet handlers
		for (var packet : ServerPackets.values())
			this.packets.put(packet.ordinal(), packet);
	}

	// move wherever you want
	@AllArgsConstructor
	public static enum ClientPackets implements Packet {
		TICK_CLIENT((pid, buf, id) ->
			TickSyncClient.onPacket()),
		CHANGE_CLIENT_TICKRATE((pid, buf, id) ->
			TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat())),
		ADVANCE_TICK_ON_CLIENTS((pid, buf, id) ->
			TASmodClient.tickratechanger.advanceClientTick()),
		CHANGE_TICKRATE_ON_CLIENTS((pid, buf, id) ->
			TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat())), // funny duplicate please fix
		SAVESTATE_INPUTS_CLIENT((pid, buf, id) -> {
			try {
				var nameBytes = new byte[buf.getInt()];
				buf.get(nameBytes);
				var name = new String(nameBytes);
				InputSavestatesHandler.savestate(name);
			} catch (Exception e) {
				TASmod.LOGGER.error("Exception occured during input savestate:", e);
			}
		}),
		CLOSE_GUISAVESTATESCREEN_ON_CLIENTS((pid, buf, id) -> {
			var mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiSavestateSavingScreen))
				mc.displayGuiScreen(new GuiSavestateSavingScreen());
			else
				mc.displayGuiScreen(null);
		}),
		LOADSTATE_INPUTS_CLIENT((pid, buf, id) -> {
			try {
				var nameBytes = new byte[buf.getInt()];
				buf.get(nameBytes);
				var name = new String(nameBytes);
				InputSavestatesHandler.loadstate(name);
			} catch (Exception e) {
				TASmod.LOGGER.error("Exception occured during input loadstate:", e);
			}
		}),
		UNLOAD_CHUNKS_ON_CLIENTS((pid, buf, id) ->
			Minecraft.getMinecraft().addScheduledTask(SavestatesChunkControl::unloadAllClientChunks)),
		REQUEST_CLIENT_MOTION((pid, buf, id) -> {
			var player = Minecraft.getMinecraft().player;
			if (player != null) {
				if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen))
					Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());

				try {
					// send client motion to server
					var bufIndex = SecureList.POOL.available();
					TASmodClient.client.write(bufIndex, SecureList.POOL.lock(bufIndex).putInt(ServerPackets.SEND_CLIENT_MOTION_TO_SERVER.ordinal())
							.putDouble(player.motionX).putDouble(player.motionY).putDouble(player.motionZ)
							.putFloat(player.moveForward).putFloat(player.moveVertical).putFloat(player.moveStrafing)
							.put((byte) (player.isSprinting() ? 1 : 0))
							.putFloat(player.jumpMovementFactor)
					);
				} catch (Exception e) {
					TASmod.LOGGER.error("Unable to send packet to server:", e);
				}
			}
		});

		private final PacketHandler handler;

		@Override
		public PacketHandler handler() {
			return this.handler;
		}
	}

	@AllArgsConstructor
	public static enum ServerPackets implements Packet {
		NOTIFY_SERVER_OF_TICK_PASS((pid, buf, id) ->
			TickSyncServer.onPacket(id)),
		REQUEST_TICKRATE_CHANGE((pid, buf, id) ->
			TASmod.tickratechanger.changeTickrate(buf.getFloat())),
		TICKRATE_ZERO_TOGGLE((pid, buf, id) -> {
			var state = TickrateChangerServer.State.fromShort(buf.getShort());
			if (state == TickrateChangerServer.State.PAUSE)
				TASmod.tickratechanger.pauseGame(true);
			else if (state == TickrateChangerServer.State.UNPAUSE)
				TASmod.tickratechanger.pauseGame(false);
			else if (state == TickrateChangerServer.State.TOGGLE)
				TASmod.tickratechanger.togglePause();
		}),
		REQUEST_TICK_ADVANCE((pid, buf, id) -> {
			if (TASmod.tickratechanger.ticksPerSecond == 0)
				TASmod.tickratechanger.advanceTick();
		}),
		SEND_CLIENT_MOTION_TO_SERVER((pid, buf, id) ->
			ClientMotionServer.getMotion().put(TASmod.getServerInstance().getPlayerList().getPlayerByUUID(id), new ClientMotionServer.Saver(buf.getDouble(), buf.getDouble(), buf.getDouble(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.get() == 1, buf.getFloat())));


		private final PacketHandler handler;

		@Override
		public PacketHandler handler() {
			return this.handler;
		}
	}

	/**
	 * Sends then authentication packet to the server
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	public void authenticate(UUID id) throws Exception {
		this.id = id;

		var bufIndex = SecureList.POOL.available();
		this.write(bufIndex, SecureList.POOL.lock(bufIndex).putInt(1).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()));
	}
	
	private void handle(ByteBuffer buf) {
		var id = buf.getInt();
		var packet = this.packets.get(id);
		if (packet != null)
			packet.handler().handle(packet, buf, this.id);
		else
			LOGGER.error("Received invalid packet: {}", this.id);
	}

}
