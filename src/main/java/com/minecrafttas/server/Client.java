package com.minecrafttas.server;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesHandler;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;
import com.minecrafttas.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import com.minecrafttas.tasmod.savestates.server.motion.ClientMotionServer;
import com.minecrafttas.tasmod.savestates.server.motion.ClientMotionServer.Saver;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.State;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
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
import java.util.function.Consumer;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

public class Client {

	private static final int BUFFER_SIZE = 1024*1024;

	private final AsynchronousSocketChannel socket;
	private final Map<Integer, Consumer<ByteBuffer>> handlers = new HashMap<>();
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
		this.writeBuffer.put(buf.position(0));
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
		// packet 2: tick client
		this.handlers.put(2, buf -> TickSyncClient.onPacket());
		
		// packet 5: change client tickrate
		this.handlers.put(5, buf -> TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat()));
	
		// packet 8: advance tick on clients
		this.handlers.put(8, buf -> TASmodClient.tickratechanger.advanceClientTick());
	
		// packet 9: change tickrate on client
		this.handlers.put(9, buf -> TASmodClient.tickratechanger.changeClientTickrate(buf.getFloat()));
	
		// packet 10: savestate inputs client
		this.handlers.put(10, buf -> {
			try {
				var nameBytes = new byte[buf.getInt()];
				buf.get(nameBytes);
				var name = new String(nameBytes);
				InputSavestatesHandler.savestate(name);
			} catch (Exception e) {
				TASmod.LOGGER.error("Exception occured during input savestate:", e);
			}
		});
		
		// packet 11: close GuiSavestateScreen on client
		this.handlers.put(11, buf -> 
			Minecraft.getMinecraft().addScheduledTask(() -> {
				var mc = Minecraft.getMinecraft();
				if (!(mc.currentScreen instanceof GuiSavestateSavingScreen))
					mc.displayGuiScreen(new GuiSavestateSavingScreen());
				else
					mc.displayGuiScreen(null);
			})
		);
		
		// packet 12: loadstate inputs client
		this.handlers.put(12, buf -> {
			try {
				var nameBytes = new byte[buf.getInt()];
				buf.get(nameBytes);
				var name = new String(nameBytes);
				InputSavestatesHandler.loadstate(name);
			} catch (Exception e) {
				TASmod.LOGGER.error("Exception occured during input loadstate:", e);
			}
		});
		
		// packet 13: unload chunks on client
		this.handlers.put(13, buf -> Minecraft.getMinecraft().addScheduledTask(SavestatesChunkControl::unloadAllClientChunks));
		
		// packet 14: request client motion
		this.handlers.put(14, buf -> {
			var player = Minecraft.getMinecraft().player;
			if (player != null) {
				if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen))
					Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
				
				try {
					// packet 15: send client motion to server
					TASmodClient.client.write(ByteBuffer.allocate(4 + 8+8+8 + 4+4+4 + 1 + 4).putInt(15)
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
	}
	
	/**
	 * Register packet handlers for packets received on the server
	 */
	private void registerServersidePacketHandlers() {
		// packet 1: authentication packet
		this.handlers.put(1, buf -> {
			this.id = new UUID(buf.getLong(), buf.getLong());
			LOGGER.info("Client authenticated: " + this.id);
		});
		
		// packet 3: notify server of tick pass
		this.handlers.put(3, buf -> TickSyncServer.onPacket(this.id));
		
		// packet 4: request tickrate change
		this.handlers.put(4, buf -> TASmod.tickratechanger.changeTickrate(buf.getFloat()));
		
		// packet 6: tickrate zero toggle
		this.handlers.put(6, buf -> {
			var state = State.fromShort(buf.getShort());
			if (state == State.PAUSE)
				TASmod.tickratechanger.pauseGame(true);
			else if (state == State.UNPAUSE)
				TASmod.tickratechanger.pauseGame(false);
			else if (state == State.TOGGLE)
				TASmod.tickratechanger.togglePause();
		});
	
		// packet 7: request tick advance
		this.handlers.put(7, buf -> {
			if (TASmod.tickratechanger.ticksPerSecond == 0)
				TASmod.tickratechanger.advanceTick();
		});
		
		// packet 15: send client motion to server
		this.handlers.put(15, buf -> ClientMotionServer.getMotion().put(TASmod.getServerInstance().getPlayerList().getPlayerByUUID(this.id), new Saver(buf.getDouble(), buf.getDouble(), buf.getDouble(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.get() == 1, buf.getFloat())));
	}
	
	/**
	 * Sends then authentication packet to the server
	 * @param id Unique ID
	 * @throws Exception Unable to send packet
	 */
	public void authenticate(UUID id) throws Exception {
		this.id = id;

		ByteBuffer buf = ByteBuffer.allocate(4+8+8);
		buf.putInt(1);
		buf.putLong(id.getMostSignificantBits());
		buf.putLong(id.getLeastSignificantBits());
		this.write(buf);
	}
	
	private void handle(ByteBuffer buf) {
		var id = buf.getInt();
		this.handlers.getOrDefault(id, _buf -> LOGGER.error("Received invalid packet: {}", id)).accept(buf);
	}

}
